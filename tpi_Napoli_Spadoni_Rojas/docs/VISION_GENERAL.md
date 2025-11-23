# Visión General del Sistema Logístico

## 1. Resumen Ejecutivo
Este proyecto implementa una plataforma de logística basada en arquitectura de microservicios. Cada responsabilidad de negocio se separa en un servicio independiente y todos se exponen al exterior a través de un **API Gateway** único. La seguridad y gestión de identidades se maneja con **Keycloak** (OIDC/JWT) y la persistencia de datos se centraliza en **PostgreSQL**, inicializada mediante scripts SQL versionados en el repositorio.

### Microservicios Principales
- **api-gateway**: Punto de entrada unificado. Enruta, aplica filtros globales y concentra documentación (Swagger) de los demás servicios.
- **ms-clientes**: Gestión de clientes, contenedores, provincias y ciudades.
- **ms-flota**: Gestión de transportistas, administradores, camiones, depósitos, tarifas y parámetros de costos.
- **ms-operaciones**: Orquestación logística: solicitudes, rutas, tramos, cálculos de costos y estados.
- **ms-geoapi**: Adaptador externo para cálculo de distancias/tiempos via Google Maps Distance Matrix.

### Tecnologías Clave
- **Spring Boot / Spring Cloud**: Base del desarrollo (REST, Gateway, WebFlux, configuración centralizada de dependencias).
- **Keycloak**: Autenticación y autorización con tokens JWT incluyendo roles y atributos de negocio (clienteId, transportistaId, adminId).
- **PostgreSQL**: Base de datos relacional central con creación y migraciones declaradas en SQL.
- **Docker / Docker Compose**: Orquestación local de todos los componentes (servicios + infraestructura).
- **Springdoc (OpenAPI)**: Generación automática de documentación interactiva (Swagger UI) por microservicio.

## 2. Flujo Típico de una Petición
1. El usuario (cliente, transportista o administrador) obtiene un token JWT desde Keycloak (login/credentials).
2. El cliente realiza una llamada HTTP al **API Gateway** (por ejemplo `GET /api/solicitudes`).
3. El Gateway aplica filtros globales (logging, CORS, reescrituras) y determina la ruta destino según el `Path` y/o `Query`.
4. El Gateway reenvía la petición al microservicio correspondiente (p.ej. `ms-operaciones`).
5. El microservicio puede:
   - Consultar la **base de datos** (PostgreSQL).
   - Llamar a otros microservicios internos (por nombre de servicio en la red Docker: `http://ms-flota:8082`, `http://ms-clientes:8081`, etc.).
   - Llamar a ms-geoapi para obtener distancia/tiempo y calcular costos.
6. Keycloak valida el token (desde el punto de vista del Gateway y/o microservicios que actúen como Resource Servers) y los roles determinan la autorización.
7. Se retorna la respuesta al cliente a través del Gateway.

## 3. Estructura de la Raíz del Proyecto
```
docker-compose.yml
pom.xml
api-gateway/
ms-clientes/
ms-flota/
ms-geoapi/
ms-operaciones/
keycloak/
postgres-compose/
test/
```

### Archivos y Carpetas Clave
- **pom.xml (raíz)**: POM padre (packaging `pom`), define `modules`, versiones alineadas (Spring Boot, Spring Cloud, springdoc). Evita duplicaciones y garantiza compatibilidad entre servicios.
- **docker-compose.yml**: Declara contenedores de infraestructura (Keycloak, PostgreSQL) y de cada microservicio. Configura variables de entorno (puertos, credenciales DB, issuer JWT, API keys) y dependencias (orden de arranque con `depends_on`).
- **.gitignore / .dockerignore**: Excluyen artefactos temporales, bins y metadata de build para mantener el repositorio limpio y reducir el contexto de Docker.
- **api-gateway/**: Código y configuración del Gateway (routes, filtros globales, reescrituras Swagger, CORS, validación de JWT).
- **ms-clientes/**: Lógica y modelos relacionados con clientes y contenedores (entidades JPA, repositorios, controladores REST).
- **ms-flota/**: Administración de flota (camiones, transportistas, depósitos, tarifas). Provee datos esenciales para cálculos logísticos.
- **ms-geoapi/**: Servicio sin persistencia que encapsula integración con Google Maps (clave API en env vars). Devuelve distancias/tiempos para planificación.
- **ms-operaciones/**: Orquestador de procesos logísticos (solicitudes → rutas → tramos). Usa datos de flota, clientes y distancias para estimar costos y tiempos. Gestiona historial de estados y actualizaciones.
- **keycloak/**: Export (`realm-export/tpi_realm.json`) con realm, roles (`ADMIN`, `TRANSPORTISTA`, `CLIENTE`), users demo y mapeo de atributos personalizados en tokens.
- **postgres-compose/**:
  - `pgdata/`: Persistencia física del motor (archivos internos de PostgreSQL). No se modifica manualmente.
  - `initdb/`: Scripts de creación y migración ejecutados automáticamente al inicializar el contenedor por primera vez.
    - `01_schema.sql`: Define el schema base (todas las tablas de clientes, flota, operaciones, parámetros y relaciones).
    - `02_seed_data.sql`: Inserta datos iniciales (provincias, ciudades, clientes, contenedores, transportistas, tarifas, solicitudes, rutas y tramos).
    - `03_migracion_columnas.sql`: Añade columnas para costos reales, tiempos, estado por defecto, y normaliza tablas.
    - `03_migration_depositos.sql`: Agrega campos de capacidad a depósitos.
    - `04_migration_estadia_tramos.sql`: Agrega columnas para estadía (días y costo) en tramos.
- **test/**: Peticiones REST (archivos `.rest`) para probar endpoints manualmente y `AUTHORIZATION_MATRIX.md` (matriz de roles vs endpoints, soporte de verificación de permisos).

## 4. API Gateway en Detalle
- Declara rutas con **predicados** (`Path`, `Query`) que deciden a qué microservicio va la petición.
- **Filtros** (`StripPrefix`, `RewritePath`) ajustan el path antes de reenviar.
- Exposición unificada de Swagger de cada microservicio con prefijos (`/clientes`, `/flota`, `/operaciones`, `/geoapi`).
- CORS global configurado para permitir frontends locales (puertos típicos: 5173, 4200, 3000).
- Seguridad: Usa `issuer-uri` de Keycloak para validar tokens JWT (firma y claims).
- `LoggingFilter.java`: Filtro global simple para registrar método, URI y status de cada request (apoya trazabilidad y depuración inicial).

## 5. Seguridad con Keycloak
- **Roles**: Diferencian capacidades operativas (ADMIN: gestión avanzada; TRANSPORTISTA: operaciones de flota; CLIENTE: solicitudes y seguimiento).
- **Attributes Mappers**: Incluyen IDs específicos (clienteId, transportistaId, adminId) en el token para filtrar recursos sin consultas extra.
- **Flujos de Autenticación**: Usuarios demuestran credenciales → obtienen token → el Gateway/microservicios validan firma y roles.
- Se puede ampliar con políticas más granulares (Role-Based Access Control y reglas por endpoint) mediante anotaciones en código (no visibles en estos archivos).

## 6. Base de Datos y Migraciones
- Modelo relacional unificado simplifica queries entre dominios (clientes ↔ solicitudes ↔ rutas ↔ tramos ↔ camiones ↔ depósitos).
- Scripts SQL versionados garantizan reproducibilidad y reducen dependencia de `ddl-auto` (solo algunos servicios usan `update`).
- Migraciones separadas permiten evolucionar el schema sin perder datos (añadir columnas de costo real, estadías, etc.).
- Datos seed facilitan pruebas funcionales y demostraciones (Swagger / REST Client) sin esfuerzo de carga manual.

## 7. Interacción Entre Microservicios
| Servicio        | Consume de                | Provee a                    | Propósito Principal                               |
|-----------------|---------------------------|-----------------------------|---------------------------------------------------|
| api-gateway     | Keycloak (tokens)         | Cliente externo             | Entrada única, enrutamiento, CORS, Swagger proxy  |
| ms-clientes     | PostgreSQL                | ms-operaciones (datos cliente y contenedor) | Gestión de datos de clientes y contenedores |
| ms-flota        | PostgreSQL                | ms-operaciones (tarifas, camiones, depósitos) | Gestión de recursos de transporte y costos |
| ms-operaciones  | ms-clientes, ms-flota, ms-geoapi, PostgreSQL | api-gateway | Orquestación logística y cálculo de costos |
| ms-geoapi       | Google Maps API externa   | ms-operaciones              | Distancias y tiempos para estimaciones           |

### Ejemplo de Flujo (Creación de Solicitud)
1. Cliente envia `POST /api/solicitudes` al Gateway.
2. Gateway enruta a `ms-operaciones`.
3. `ms-operaciones` valida cliente y contenedor (llamando a `ms-clientes`).
4. Obtiene tarifas y disponibilidad (llamando a `ms-flota`).
5. Obtiene distancia (llamando a `ms-geoapi`).
6. Calcula costo estimado y crea registros `solicitud`, `ruta`, `tramos` en PostgreSQL.
7. Respuesta via Gateway al cliente.

## 8. Documentación y Testing Manual
- Swagger UI por microservicio (`/swagger-ui.html`) y OpenAPI JSON (`/api-docs`).
- Acceso unificado desde el Gateway usando prefijos /clientes, /flota, /operaciones, /geoapi.
- Archivos `.rest` permiten pruebas rápidas (VS Code REST Client) reproduciendo llamadas sin herramientas externas.
- La matriz de autorización apoya validación de seguridad y roles en endpoints críticos.

## 9. Consideraciones de Perfiles y Entorno
- Variables de entorno en `docker-compose.yml` pueden declarar `SPRING_PROFILES_ACTIVE=prod` incluso si no existe un `application-prod.yml` (se tomará el `application.yml` base). Recomendación: crear archivos específicos para producción si se requiere tuning.
- Diferencias entre `application.yml` y `application-dev.yml`: URL a Postgres (nombre de contenedor vs `localhost`), configuración de `ddl-auto`, potenciales tiempos de conexión.
- Centralizar configuración sensible (API keys, secrets de Keycloak) en variables de entorno evita exponer credenciales en el repositorio.

## 10. Mejoras Potenciales (Roadmap)
- Sustituir `System.out.println` por SLF4J + Logback y estructurar logs (JSON) para observabilidad.
- Añadir tracing distribuido (OpenTelemetry + Jaeger/Zipkin) para ver llamadas entre microservicios.
- Implementar Circuit Breakers / Retries (Resilience4j) para tolerancia a fallos de ms-geoapi o flota.
- Endpoints de autorización más finos (anotaciones @PreAuthorize usando roles del token).
- Externalizar configuración (Config Server) si el número de propiedades crece.
- Segmentar la base de datos en schemas o instancias separadas si se requiere desacoplamiento fuerte.

## 11. Diagrama Conceptual (ASCII)
```
              +-----------------+
              |     Usuario     |
              +--------+--------+
                       |
                       v (JWT)
               +---------------+
               |  API Gateway  |  (Rutas, filtros, CORS, Swagger)
               +--+-----+---+--+
                  |     |   |
        +---------+     |   +--------------+
        |               |                  |
        v               v                  v
 +-----------+   +-------------+     +-------------+
 | ms-clientes|  |  ms-flota   |     |  ms-geoapi  |
 +-----+------+  +------+------+     +------+------+
       |                |                   |
       |                |                   | (Google Maps)
       |                |                   v
       |                |             +-----------+
       |                |             |  Externo  |
       v                v
   +---------------------------+
   |       ms-operaciones      | (Orquestación rutas/tramos)
   +-------------+-------------+
                 |
                 v
          +-------------+
          | PostgreSQL  | (Schema + Seeds + Migraciones)
          +-------------+

          +-------------+
          |  Keycloak   | (Realm, Roles, Users, JWT)
          +-------------+
```

## 12. Resumen para Presentación Oral
“El sistema está dividido en microservicios para separar dominios: clientes, flota, operaciones y cálculo de distancias. El API Gateway centraliza el acceso y documentación y valida tokens de Keycloak que incluyen roles y atributos. Los servicios interactúan entre sí por HTTP interno usando nombres de contenedor y comparten una base de datos PostgreSQL que se inicializa con scripts SQL (schema, datos y migraciones). ms-operaciones orquesta el flujo desde la solicitud hasta los tramos y rutas, apoyándose en datos de flota, clientes y distancias externas. Esta arquitectura facilita escalabilidad, trazabilidad y evolución incremental.”

---
Última actualización: Generado automáticamente.
