# Relaciones Entre Microservicios y Configuraciones

Documento para entender cómo se comunican los microservicios internamente y cómo se aplican perfiles y variables de entorno en este proyecto.

---
## 1. Panorama General de la Arquitectura
- **API Gateway**: Punto único de entrada. Define rutas y reenvía tráfico a cada microservicio interno por nombre de host Docker.
- **Microservicios**: `ms-clientes`, `ms-flota`, `ms-operaciones`, `ms-geoapi` (y Keycloak + Postgres como servicios de soporte).
- **Base de datos**: Todos (excepto geoapi) leen/escriben en la misma instancia Postgres.
- **Security / Tokens**: El Gateway espera tokens JWT emitidos por Keycloak. Después del Gateway, los microservicios confían en que la petición ya fue autenticada (segundo nivel de validación directa no evidenciado en YAML de cada servicio).

---
## 2. Comunicación Interna
- Tipo: **HTTP** sobre red Docker `backend-net` (driver bridge).
- Cada servicio se resuelve por su nombre de contenedor: `http://ms-flota:8082`, `http://ms-clientes:8081`, etc.
- En `ms-operaciones/application.yml` se ven propiedades como:
  ```yaml
  clientes.service.url: http://ms-clientes:8081/api
  geoapi.service.url: http://ms-geoapi:8084/api/distancia
  flota.base.url: http://ms-flota:8082
  ```
  Estas URL internas permiten hacer llamadas REST para obtener datos necesarios (camiones, tarifas, distancia, clientes).
- El API Gateway enruta usando reglas de `spring.cloud.gateway.routes` (ver `api-gateway/application.yml`) que mapean patrones de path a URIs internas.
- Predicados y filtros: `Path`, `Query`, `StripPrefix`, `RewritePath` para swagger.

---
## 3. Seguridad y Tokens
- En el Gateway: configuración de resource server (issuer Keycloak) valida JWT.
  ```yaml
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/tpi
  ```
- Microservicios no muestran configuración de `resourceserver.jwt` en sus `application.yml` → Se asume delegación de autenticación al Gateway.
- Posible evolución: añadir en cada servicio `spring.security.oauth2.resourceserver.jwt.issuer-uri` para defensa en profundidad (validación adicional de firma y claims).
- Claims útiles (desde realm): `roles`, `adminId`, `clienteId`, `transportistaId`.

---
## 4. Flujo de Caso de Uso: Solicitud de Transporte
### Paso a Paso
1. **Cliente crea solicitud**: `POST /api/solicitudes` (Gateway → `ms-operaciones`).
2. **ms-operaciones valida y en estado BORRADOR**: Persiste entidad solicitud y registra primer estado en la tabla historial.
3. **Cálculo de distancia**: ms-operaciones llama a `ms-geoapi` (`geoapi.service.url`) para obtener distancia estimada.
4. **Datos del contenedor y cliente**: ms-operaciones llama a `ms-clientes` para verificar contenedor/cliente (o crea nuevo si endpoint compuesto `registrar`).
5. **Tarifas y parámetros**: Solicita a `ms-flota` lista de tarifas (`/api/tarifas`) y parámetros (`/api/parametros-tarifa`) para calcular costo estimado.
6. **Creación de ruta y tramos**: Según tipo (directa/indirecta) genera uno o más tramos (ORIGEN_DESTINO o pasando por depósito más cercano).
7. **Estados**: BORRADOR → PROGRAMADA (cuando se asigna ruta) → ASIGNADA (cuando todos los tramos tienen camión) → EN_TRANSITO (cuando inicia primer tramo) → COMPLETADA (cuando finaliza último tramo) o CANCELADA.
8. **Asignación de camión**: ms-operaciones consulta camiones disponibles en `ms-flota` comparando peso/volumen de contenedor.
9. **Inicio/Fin de tramo**: Transportista inicia y finaliza → se registran tiempos, distancia real, costos parciales.
10. **Cálculo costos reales**: Usa tarifas, consumo combustible (camión), parámetros globales y estadía en depósitos (si tramo pasa por depósito).
11. **Actualización final**: Al cerrar último tramo se agregan costos acumulados, tiempo total, distancia real; solicitud marca COMPLETADA.

### Tablas involucradas (a partir de scripts de migración):
- `solicitudes` (datos base + estado actual + costo estimado/real).
- `rutas` y `tramos` (estructura logística). 
- `cambios_estado_solicitud` (historial de transiciones de estado). 
- `depositos` (para estadía y capacidad). 
- `parametros_tarifa`, `tarifas` (factor costo). 

### Puntos de Interacción Externa:
- Cliente final sólo ve endpoints expuestos por Gateway.
- Transportista administra tramos asignados.
- ADMIN supervisa y modifica entidades soporte (camiones, depósitos, tarifas). 

---
## 5. Dependencias Clave de `ms-operaciones`
| Necesidad | Servicio consultado | Razón |
|-----------|--------------------|-------|
| Distancia | `ms-geoapi` | Costo y tiempo estimado. |
| Tarifa/Parámetros | `ms-flota` | Cálculo costo estimado y real (combustible, cargo fijo). |
| Camiones disponibles | `ms-flota` | Asignación y verificación de capacidad. |
| Datos cliente/contenedor | `ms-clientes` | Validar existencia o crear recursos empaquetados. |
| Depósitos | `ms-flota` | Rutas indirectas y cálculo de estadía. |

---
## 6. Estados y Historial
- Cada transición se registra (tabla historial) con fecha y observaciones.
- Permite reconstruir el ciclo de vida completo de una solicitud para auditoría.
- Facilidad de reporting: conteo de solicitudes por estado, tiempo promedio entre fases.

---
## 7. Configuraciones: Perfiles y Variables de Entorno
### Observado en YAML
- Todos los servicios (excepto geoapi) definen:
  ```yaml
  spring:
    profiles:
      active: dev
  ```
- Existe archivo `application-dev.yml` para cada uno con diferencias principalmente en URLs apuntando a `localhost` en lugar de nombres Docker.
- En `docker-compose.yml` se usa (ejemplo) `SPRING_PROFILES_ACTIVE=prod` (según fragmento compartido en pedido previo) — pero **NO hay** `application-prod.yml` en el repositorio.

### Implicaciones
1. Si se establece `SPRING_PROFILES_ACTIVE=prod` y no existe `application-prod.yml`, Spring cargará solo `application.yml` (y no aplicará overrides dev). 
2. Variables de entorno como `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, etc., sobreescriben valores del `application.yml` porque Spring Boot prioriza environment properties.
3. Falta de archivo `application-prod.yml` podría ser intencional (usar mismos settings que dev) o pendiente de creación (para desactivar `show-sql`, ajustar log level, etc.).

### Recomendación
Crear `application-prod.yml` con:
```yaml
spring:
  jpa:
    show-sql: false
logging:
  level:
    org.hibernate.SQL: INFO
```
Y mover configuraciones sensibles (credenciales) a variables de entorno con `.env` o secretos.

---
## 8. Prioridad de Configuraciones (Orden Resuelto por Spring)
1. Variables de entorno / parámetros de JVM (`-D...`).
2. `application-{profile}.yml` (si perfil activo coincide).
3. `application.yml` (base).
4. Valores por defecto en código.

En este proyecto: Docker Compose inyecta environment → anula la URL del datasource y perfil activo; si ese perfil no tiene archivo específico, se cae al base.

---
## 9. Patrón de Multi-servicio en Propiedades
- `ms-operaciones` diferencia propiedades externas para facilitar refactor (si cambia host o puerto se ajusta en un único lugar).
- Propiedades para timeouts `geoapi.connect.timeout` y `geoapi.read.timeout` → controlan resiliencia ante llamadas a distancia.
- Falta observabilidad explícita (no se ve configuración Actuator en los microservicios aparte del Gateway). Se puede añadir para health checks profundos.

---
## 10. Posibles Riesgos / Mejoras
| Tema | Riesgo Actual | Mejora Propuesta |
|------|---------------|------------------|
| Validación JWT sólo en Gateway | Acceso interno directo (si red expuesta) | Añadir `resourceserver` en cada servicio. |
| Perfil `prod` inexistente | Config igual a dev (logs verbosos, SQL visible) | Crear `application-prod.yml`. |
| Credenciales en YAML | Exposición accidental del repo | Usar variables de entorno + gestor secretos. |
| Timeouts limitados | Retrasos prolongados en geoapi | Implementar circuit breaker (Resilience4j). |
| Falta versionado de migraciones | Scripts manuales sucesivos | Adoptar Flyway/Liquibase. |
| Dependencias fuertes | Cascada de fallos | Añadir fallback de distancia (valor aproximado). |

---
## 11. Resumen Rápido (TL;DR)
- Comunicación interna: HTTP vía nombres Docker (`ms-flota`, `ms-clientes`, etc.).
- Gateway enruta y valida JWT; servicios confían en autenticación previa.
- Flujo solicitud: crear → calcular distancia/costo → generar ruta/tramos → asignar camión → iniciar/finalizar → costos reales y estado COMPLETADA.
- Perfiles: `dev` activo en YAML; `prod` forzado en Compose pero sin archivo dedicado (usa base). Variables de entorno sobrescriben configuración.
- Mejoras: validar JWT en cada servicio, agregar perfil prod real, externalizar secretos, observabilidad y migraciones versionadas.

---
## 12. Glosario
- **Gateway**: Servicio frontal que enruta y aplica seguridad.
- **Tramo**: Segmento individual de una ruta logística.
- **Estado**: Fase del ciclo de vida de una solicitud (BORRADOR, PROGRAMADA, ASIGNADA, EN_TRANSITO, COMPLETADA, CANCELADA).
- **Timeout**: Tiempo máximo de espera antes de abortar llamada externa.
- **Profiles**: Conjuntos de configuraciones activables (dev, prod, etc.).
- **Claims**: Atributos en el token (roles, clienteId...).

---
¿Deseas un diagrama ASCII del flujo o ejemplos de cómo agregar `resourceserver` a cada microservicio? Pídelo y lo incorporo.
