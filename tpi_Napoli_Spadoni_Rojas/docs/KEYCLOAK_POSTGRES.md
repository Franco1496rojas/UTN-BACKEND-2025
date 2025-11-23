# Infraestructura de Autenticación y Base de Datos

Este documento explica, paso a paso y en lenguaje sencillo, cómo funciona **Keycloak** (para autenticación/autorización) y **PostgreSQL** (base de datos relacional) en este proyecto. Está pensado para alguien que parte desde cero.

---
## 1. Visión General
- **Keycloak** emite tokens JWT con roles y atributos (claims) que representan quién eres y qué puedes hacer.
- **PostgreSQL** almacena todos los datos del negocio (clientes, camiones, rutas, depósitos, etc.).
- El **API Gateway** recibe las peticiones externas y pasa el JWT a los microservicios internos, que validan roles/claims para permitir o denegar acciones.

---
## 2. ¿Qué es Keycloak y por qué lo usamos?
Keycloak es un servidor de identidad. Centraliza:
- Registro y administración de usuarios.
- Asignación de roles (permisos de negocio).
- Emisión de tokens estándar (OpenID Connect / OAuth2).
- Manejo de credenciales y claims personalizados.

Beneficio: No duplicamos lógica de autenticación en cada microservicio. Cada servicio confía en los tokens emitidos por Keycloak.

---
## 3. Realm `tpi`
Un **Realm** agrupa usuarios, roles y clientes. Aquí el archivo `tpi_realm.json` (importado automáticamente al arrancar Keycloak) define:
- Nombre: `tpi`
- Estado: habilitado
- Roles de negocio: `ADMIN`, `TRANSPORTISTA`, `CLIENTE`

---
## 4. Roles de Negocio
Los roles indican el tipo de usuario y permiten futura autorización.
- `ADMIN`: Gestión total del sistema.
- `TRANSPORTISTA`: Operaciones relacionadas con flota, camiones, movimiento.
- `CLIENTE`: Solicitudes, seguimiento, interacción con sus propios contenedores/envíos.

Los roles viajan dentro del token JWT como un claim `roles`.

---
## 5. Cliente `api-gateway`
En Keycloak, un **Client** representa una aplicación que se autentica y solicita tokens.
- `clientId`: `api-gateway`
- Tipo: confidencial (no es público) → tiene `secret`.
- `secret`: `gateway-secret-change` (debería cambiarse en producción por seguridad).
- Flujos habilitados: `standardFlow` (autorización con código) y `directAccessGrants` (password grant para pruebas rápidas).
- `redirectUris`: `http://localhost:8080/*` (dónde Keycloak puede redirigir después de login).
- `webOrigins`: `*` (acepta cualquier origen; en producción conviene restringir).

---
## 6. Mappers y Claims Personalizados
Los **Protocol Mappers** añaden información al token. Definidos en el realm:
- `realm roles`: agrega listado de roles en claim `roles`.
- `clienteId`: trae atributo del usuario y lo pone en claim `clienteId`.
- `transportistaId`: claim `transportistaId`.
- `adminId`: claim `adminId`.

Así un microservicio puede saber, por ejemplo, qué `clienteId` usar para filtrar datos.

---
## 7. Usuarios de Ejemplo
Del archivo `tpi_realm.json`:
| Usuario        | Rol            | Atributo Claim        | Password    |
|----------------|----------------|-----------------------|-------------|
| `admin-napo`   | `ADMIN`        | `adminId = 1`         | `admin123`  |
| `transp-cabe`  | `TRANSPORTISTA`| `transportistaId = 1` | `transp123` |
| `franco`       | `CLIENTE`      | `clienteId = 1`       | `cliente123`|

Cada uno tiene un atributo personalizado que se convierte en claim dentro del token.

---
## 8. Flujo Básico de Autenticación (Paso a Paso)
1. Usuario envía credenciales al endpoint de token de Keycloak.
2. Keycloak valida usuario y rol asignado.
3. Keycloak genera un **Access Token (JWT)** con roles y claims (`adminId`, `clienteId`, etc.).
4. El cliente (API Gateway o aplicación externa) guarda el token temporalmente.
5. Al llamar a un microservicio, incluye el encabezado: `Authorization: Bearer <token>`.
6. El microservicio verifica la firma y emisor (`issuer`) del token y extrae roles/claims.
7. Basado en rol/claims se permite o deniega la operación.

---
## 9. Obtener un Token (Prueba Rápida)
Usando el flujo Direct Access (password grant) desde consola CMD:
```cmd
curl -X POST http://localhost:8085/realms/tpi/protocol/openid-connect/token ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=password" ^
  -d "client_id=api-gateway" ^
  -d "client_secret=gateway-secret-change" ^
  -d "username=admin-napo" ^
  -d "password=admin123"
```
Respuesta (parcial):
```json
{
  "access_token": "eyJhbGciOi...",
  "expires_in": 300,
  "token_type": "Bearer",
  ...
}
```
Usa el valor de `access_token` en llamadas:
```cmd
curl -H "Authorization: Bearer TOKEN_AQUI" http://localhost:8080/api/ejemplo
```

---
## 10. Uso en Microservicios (Spring Boot)
Cada microservicio se configura como **Resource Server** apuntando al `issuer`: `http://keycloak:8080/realms/tpi` (ver variables de entorno en `docker-compose.yml`). Ejemplo conceptual:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/tpi
```
Autorización de métodos (posible futura implementación):
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> crearRecurso(...) { ... }
```
O lectura de claims personalizados:
```java
String clienteId = jwt.getClaim("clienteId");
```
(En el código actual no se ven anotaciones, pero esta sería la forma de aplicar políticas específicas.)

---
## 11. ¿Qué es PostgreSQL en este entorno?
PostgreSQL es el motor relacional que guarda la información persistente del dominio: entidades como clientes, camiones, rutas, solicitudes, etc. Se levanta como contenedor Docker.

---
## 12. Volúmenes Usados
En el `docker-compose.yml` raíz:
- `./postgres-compose/pgdata:/var/lib/postgresql/data` → Carpeta física donde PostgreSQL guarda sus archivos internos (persistencia real). No hace falta entender cada archivo; simplemente, “ahí viven los datos”.
- `./postgres-compose/initdb:/docker-entrypoint-initdb.d` → Carpeta con scripts `.sql` que se ejecutan automáticamente SOLO la primera vez que se inicializa el contenedor (cuando `pgdata` está vacío).

---
## 13. Scripts de Inicialización (`initdb`)
Orden de ejecución al crear la base:
1. `01_schema.sql`: Crea tablas base (clientes, contenedores, transportistas, administradores, camiones, depósitos, tarifas, parámetros, solicitudes, rutas, tramos, historial). Define el modelo relacional inicial.
2. `02_seed_data.sql`: Inserta datos de prueba para que el sistema tenga información desde el primer momento (provincias, ciudades, clientes, contenedores, etc.). Facilita pruebas sin carga manual.
3. `03_migracion_columnas.sql`: Ajusta la estructura agregando columnas faltantes (costos reales, tiempos reales, normalización de estados). Representa una evolución del modelo.
4. `03_migration_depositos.sql`: Agrega columnas de capacidad y ocupación a depósitos para seguimiento de espacio disponible.
5. `04_migration_estadia_tramos.sql`: Añade días y costo de estadía en tramos (para calcular costos adicionales cuando un contenedor permanece en depósito temporalmente).

Idea central: “Los scripts generan el modelo relacional y datos iniciales para que los microservicios puedan construir respuestas sin depender de carga manual.”

---
## 14. ¿Cómo se Ejecutan los Scripts?
Docker arranca el contenedor de Postgres. Si la carpeta `pgdata` está vacía (primera vez), el entrypoint de la imagen busca `.sql` dentro de `/docker-entrypoint-initdb.d` y los ejecuta en orden alfabético. Después la base queda lista con estructura + datos.

---
## 15. Levantar Todo el Entorno
Desde la raíz del proyecto:
```cmd
docker compose up --build
```
Esto:
- Construye imágenes de los microservicios y del gateway.
- Arranca Keycloak (puerto 8085 externo → 8080 interno) importando el realm.
- Arranca PostgreSQL ejecutando scripts si es primera vez.
- Arranca microservicios y el API Gateway.

Para bajar:
```cmd
docker compose down
```
Para reiniciar solo Postgres (si cambiaste scripts y borraste datos):
```cmd
docker compose rm -f postgres
rd /s /q postgres-compose\pgdata
docker compose up postgres
```
(Advertencia: esto elimina todos los datos.)

---
## 16. Verificar Datos en PostgreSQL
Instala un cliente `psql` (o usa otro contenedor) y conecta:
```cmd
psql -h localhost -U postgres -d tpi_db
```
Consulta rápida de tablas (ejemplo):
```sql
\dt
SELECT * FROM clientes LIMIT 5;
SELECT * FROM camiones LIMIT 5;
```
Salir: `\q`

---
## 17. ¿Por qué Pre-Cargar Datos?
- Permite que las APIs respondan inmediatamente (ej.: listar clientes, tarifas) sin cargar manualmente.
- Facilita pruebas automatizadas y demostraciones.
- Reduce errores humanos al tener una configuración consistente.

---
## 18. Problemas Comunes
| Problema | Causa | Solución |
|----------|-------|----------|
| No se importan scripts | `pgdata` ya existe | Borrar `pgdata` si quieres re-ejecutar init (pierdes datos). |
| Token inválido | Caducó (expira en minutos) | Solicita uno nuevo (ver sección 9). |
| Microservicio rechaza token | `issuer` mal configurado | Verifica `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`. |
| Rol no aplicado | Falta mapper o rol en usuario | Revisar realm y usuario en Keycloak. |

---
## 19. Próximos Pasos Sugeridos
- Agregar autorización granular (anotaciones `@PreAuthorize`, políticas por endpoint).
- Rotar y proteger el `client_secret` del gateway.
- Crear migraciones versionadas (Flyway/Liquibase) en lugar de scripts sueltos.
- Documentar modelo de datos con diagrama ER.
- Añadir tests automatizados que validen presencia de datos semilla.

---
## 20. Resumen Rápido (TL;DR)
- Keycloak realm `tpi` → usuarios con roles y claims (`adminId`, `clienteId`, etc.).
- Cliente `api-gateway` obtiene tokens y los distribuye.
- PostgreSQL se inicializa con scripts que crean modelo + datos base.
- Microservicios validan tokens para asegurar identidad y permisos.

Si entiendes este resumen, ya captaste la arquitectura básica de autenticación + datos.

---
## 21. Glosario
- **Realm**: Espacio lógico de identidad en Keycloak.
- **Claim**: Atributo incluido en el token (ej. `clienteId`).
- **JWT**: Token firmado con información del usuario.
- **Mapper**: Configuración que transforma/añade datos al token.
- **Volumen**: Carpeta del host montada en el contenedor.
- **Seed Data**: Datos iniciales de prueba.

---
¿Necesitas una ampliación sobre algún punto específico? Pídelo y lo detallamos.
