# Guía de Tests (.rest) y Dockerfiles

Este documento explica para principiantes cómo usar los archivos de pruebas `.rest` y cómo interpretar los `Dockerfile` de cada microservicio y el gateway.

---
## 1. ¿Qué son los archivos `.rest`?
Son archivos de texto con peticiones HTTP pre-armadas que se pueden ejecutar directamente desde VS Code usando la extensión **REST Client** (nombre del marketplace: `humao.rest-client`). Permiten probar APIs rápido sin herramientas externas.

### ¿Para qué sirven aquí?
- Probar creación y validación de entidades (clientes, camiones, depósitos, solicitudes, tramos, etc.).
- Simular flujos completos: desde registrar una solicitud hasta finalizar tramos y calcular costos.
- Verificar reglas de negocio y transiciones de estado.
- Probar seguridad y autorización según rol (ADMIN, TRANSPORTISTA, CLIENTE).

---
## 2. Archivos `.rest` presentes
| Archivo | Propósito principal |
|---------|---------------------|
| `test.rest` | Plan amplio de pruebas CRUD y flujos (muy detallado, cubre casi todo el dominio). |
| `test_complete.rest` | Escenarios organizados por rol y microservicio, incluye logins y flujo completo referenciado paso a paso. |
| `test_finalizar_tramo.rest` | Guía específica para finalizar un tramo, mostrando cálculos automáticos y casos de error. |
| `AUTHORIZATION_MATRIX.md` | Documento de referencia: qué rol puede acceder a cada endpoint (para validar seguridad). |

---
## 3. Cómo ejecutar una petición
1. Instala extensión REST Client en VS Code.
2. Abre `test_complete.rest` (por ejemplo).
3. Haz clic en el texto `GET http://...` o `POST http://...` que aparece encima de cada petición. Aparecerá un botón `Send Request`.
4. Presiona `Send Request`. La respuesta se abrirá en un panel lateral.

Si la petición requiere autenticación (`Authorization: Bearer TOKEN_AQUI`):
1. Ejecuta primero uno de los bloques de login (ADMIN, TRANSPORTISTA o CLIENTE).
2. Copia el valor completo de `access_token` de la respuesta JSON.
3. Sustituye `TOKEN_AQUI` (o `TOKEN_ADMIN_AQUI`, etc.) por el token real.
4. Vuelve a enviar la petición protegida.

---
## 4. Estructura típica de una petición `.rest`
```http
### Comentarios descriptivos
POST http://localhost:8080/api/solicitudes/registrar
Content-Type: application/json
Authorization: Bearer TOKEN_AQUI

{
  "clienteDni": "12345",
  "clienteEmail": "nuevo.cliente@example.com",
  "clienteNombre": "Cliente Nuevo",
  "contenedorPeso": 2400.0,
  "contenedorVolumen": 3.2,
  "contenedorCodigo": "CONT-NUEVO-001",
  "origen": "Buenos Aires",
  "destino": "Córdoba"
}
```
- Línea 1: Comentario (ignorado por el cliente HTTP).
- Línea 2: Método + URL.
- Cabeceras: `Content-Type`, `Authorization` si corresponde.
- Cuerpo JSON: datos de la entidad o comando.

---
## 5. Flujos representados
### Ejemplos clave en `test.rest`:
- Creación y validación de tarifas (incluye errores por solapamiento, rangos inválidos, valores negativos).
- Gestión de depósitos con capacidad y ocupación (incrementar/decrementar, validaciones de límites).
- Asignación de ruta directa/indirecta a una solicitud y generación de tramos.
- Asignación de camión con múltiples validaciones (disponibilidad, capacidades, estado del tramo, etc.).
- Inicio y finalización de tramos con cálculo de costos y estadía.
- Seguimiento de contenedores y cálculo de costo total al completar solicitud.

### `test_complete.rest` añade:
- Segmentación por rol: qué puede hacer ADMIN vs CLIENTE vs TRANSPORTISTA.
- Variables reutilizables (`@gateway = http://localhost:8080`).
- Un flujo final compacto (login cliente → registrar → asignar ruta → asignar camión → iniciar → finalizar → verificar solicitud).

### `test_finalizar_tramo.rest`:
- Explica internamente qué cálculos se hacen al finalizar un tramo.
- Muestra ejemplos de respuestas esperadas y errores comunes (tramo no iniciado, ID inexistente, etc.).

---
## 6. Autorización y Roles
El archivo `AUTHORIZATION_MATRIX.md` resume qué rol (ADMIN, CLIENTE, TRANSPORTISTA) puede acceder a cada endpoint.
- Uso práctico: antes de ejecutar una petición que falla con `403 Forbidden`, revisa si tu rol realmente tiene permiso.
- Ayuda a construir pruebas de seguridad sistemáticas (verificar que roles no autorizados reciben 403).

---
## 7. Buenas prácticas al usar los tests
- Mantén separados tokens por rol (podés abrir cada login en distinta pestaña de respuesta y copiar según necesidad).
- Empieza probando GET simples antes de mutaciones (POST/PUT/PATCH/DELETE).
- Si cambias datos de semilla o migraciones, algunos IDs pueden dejar de existir; ajusta las rutas.
- Cuando una petición falla, lee el cuerpo de respuesta: muchas validaciones devuelven mensajes descriptivos.

---
## 8. ¿Qué son los Dockerfiles?
Definen cómo se construye la imagen Docker (ejecutable empaquetado) de cada microservicio. Aquí todos siguen un patrón **multi-stage build**:
1. Etapa `builder` (usa imagen Maven + JDK completo) compila y empaqueta el módulo específico.
2. Etapa final (runtime) usa una imagen liviana con **JRE** para ejecutar el `.jar` empaquetado.

Ventajas del multi-stage:
- Imágenes finales más pequeñas (sin fuentes ni caché de Maven).
- Aisla dependencias de build versus ejecución.
- Reduce superficie de ataque en producción.

---
## 9. Patrón común de los Dockerfiles
Ejemplo (simplificado):
```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn -B -DskipTests clean package spring-boot:repackage -pl ms-flota -am

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/ms-flota/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
```
### Claves:
- `-pl ms-flota -am`: construye solo el módulo `ms-flota` y trae sus dependencias (reactor Maven).
- `-DskipTests`: acelera build (los tests no se ejecutan dentro del contenedor de build).
- `spring-boot:repackage`: empaca el JAR ejecutable (fat jar con dependencias).
- `EXPOSE <puerto>`: documentación del puerto interno que el servicio usa (el mapeo real se define en `docker-compose.yml`).
- `ENTRYPOINT`: comando que siempre se ejecuta al iniciar el contenedor.

---
## 10. Puertos por servicio
| Servicio | Puerto expuesto en Dockerfile | Puerto externo (según compose) |
|----------|------------------------------|----------------------------------|
| api-gateway | 8080 | `8080:8080` |
| ms-clientes | 8081 | Interno en red (`backend-net`) |
| ms-flota | 8082 | Interno en red (`backend-net`) |
| ms-operaciones | 8083 | Interno en red (`backend-net`) |
| ms-geoapi | 8084 | `8084:8084` |

Nota: Los microservicios (excepto gateway y geoapi) no publican puerto directo a host; se accede vía gateway.

---
## 11. Relación con `docker-compose.yml`
En el compose principal:
```yaml
ms-flota:
  build:
    context: .
    dockerfile: ms-flota/Dockerfile
  environment:
    - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/tpi
  depends_on:
    postgres:
      condition: service_healthy
```
- `build` usa el Dockerfile para crear la imagen local.
- Variables de entorno inyectan configuración de DB y seguridad (issuer de Keycloak).
- `depends_on` asegura que Postgres esté saludable antes de iniciar.
- Todos los servicios comparten la red `backend-net` → permite comunicarse usando nombres de contenedor (`postgres`, `keycloak`, etc.).

---
## 12. Pasos para construir y levantar todo
```cmd
docker compose up --build
```
Esto:
- Construye imágenes con cada Dockerfile.
- Inicia Keycloak (importa realm).
- Inicializa Postgres con scripts si es primera vez.
- Levanta microservicios y gateway.

Para reconstruir solo un servicio (ejemplo ms-flota) después de cambios:
```cmd
docker compose build ms-flota
docker compose up ms-flota
```

---
## 13. Comprobaciones rápidas post-levante
```cmd
curl http://localhost:8080/actuator/health
curl http://localhost:8084/actuator/health
```
Si los endpoints Actuator no están habilitados, prueba un GET simple:
```cmd
curl http://localhost:8080/api/provincias
```

---
## 14. Errores frecuentes y solución
| Situación | Causa | Solución |
|----------|-------|----------|
| 403 Forbidden en endpoint | Token de rol incorrecto | Usar login correcto y reemplazar token. |
| 404 Not Found | ID inexistente | Confirmar que el dato se creó; revisar secuencia de pruebas. |
| Falla asignar camión | Camión no disponible o capacidad menor | Usar `/api/camiones/disponibles` con filtros. |
| Tramo no finaliza | No está en estado INICIADO | Ejecutar primero `POST /api/tramos/{id}/iniciar`. |
| Scripts Postgres no corren | Volumen `pgdata` ya tiene datos | Borrar volumen si necesitas reinicializar (pierdes datos). |
| Build lento | Descarga dependencias cada vez | Activar cache de build (dejar `COPY pom.xml` antes, optimización futura). |

---
## 15. Mejoras futuras sugeridas
- Agregar ejemplo de autorización por anotaciones en código (`@PreAuthorize`).
- Separar tests por microservicio para enfoque modular.
- Usar variables globales para tokens (`@token_admin`, etc.).
- Añadir scripts de carga de Postman/Insomnia para usuarios no VS Code.
- Optimizar Dockerfiles con cache de dependencias Maven.
- Activar Actuator + Prometheus/Grafana para monitoreo.

---
## 16. Resumen rápido (TL;DR)
- Archivos `.rest` = colecciones ejecutables de pruebas manuales.
- `test.rest` = cobertura masiva CRUD + flujos.
- `test_complete.rest` = pruebas por rol + flujo completo.
- `test_finalizar_tramo.rest` = detalle de cierre de tramo y cálculos.
- Dockerfiles usan multi-stage build para empaquetar cada microservicio en su propia imagen liviana.
- `docker-compose.yml` orquesta todo y define red, dependencias y configuración de entorno.

---
## 17. Glosario
- **REST Client**: Extensión VS Code para ejecutar peticiones HTTP desde archivos de texto.
- **Multi-stage build**: Técnica Docker que genera imágenes más pequeñas separando build y runtime.
- **Bearer Token**: Token JWT enviado en encabezado `Authorization`.
- **Flujo**: Secuencia ordenada de pasos que representan un caso real (registro, asignación, transporte, finalización).
- **Issuer**: URL que identifica al emisor del token JWT (Keycloak realm).

---
¿Necesitas que prepare una versión simplificada para una presentación o un diagrama visual del flujo? Pídelo y lo agregamos.
