# Logging, Observabilidad, Swagger y Autorización (Keycloak)

Guía breve para entender el estado actual y mejoras futuras en monitoreo, documentación y seguridad de roles.

---
## 1. Logging Actual
### En los microservicios
- Configuración en `application.yml` (ej.: `org.hibernate.SQL: DEBUG`) muestra todas las sentencias SQL generadas por Hibernate.
- Nivel `springframework.web: INFO` para trazas básicas HTTP.
- Ventaja: facilita depurar consultas y comportamiento JPA.
- Riesgo en producción: verbosidad elevada, posible impacto de rendimiento y exposición de estructura interna.

### En el API Gateway
- Filtro global `LoggingFilter` registra entrada y salida de cada petición:
  ```java
  System.out.println("--> [" + reqId + "] METHOD URI");
  System.out.println("<-- [" + reqId + "] STATUS");
  ```
- Usa cabecera opcional `X-Request-Id` para correlación manual.
- Simple (stdout) sin niveles estructurados.

### Mejora Propuesta
| Área | Mejora | Beneficio |
|------|--------|-----------|
| Formato | Usar SLF4J + Logback JSON (logstash encoder) | Ingesta fácil en ELK / Loki. |
| Filtro Gateway | Añadir tiempo de respuesta y tamaño payload | Métricas rápidas de rendimiento. |
| Niveles | Reducir Hibernate a WARN en prod | Menos ruido y gasto I/O. |
| Correlación | Generar `traceId` si falta `X-Request-Id` | Seguimiento completo de flujos. |

---
## 2. Observabilidad Actual
- Actuator habilitado en Gateway (`/actuator/health`, `/actuator/info`, `/actuator/gateway`).
- No se observa Actuator configurado explícitamente en otros servicios en los YAML leídos.

### Extensiones Futuras
| Componente | Herramienta | Propósito |
|------------|------------|-----------|
| Métricas | Micrometer + Prometheus endpoint | Recolección de métricas de negocio y sistema. |
| Trazas | OpenTelemetry (OTel) + Collector (Jaeger/Zipkin) | Trazado distribuido de solicitudes end-to-end. |
| Logs | Centralización (ELK / Loki + Grafana) | Análisis y alertas de eventos. |
| Salud | Actuator en todos los servicios | Detección proactiva de fallos. |
| Circuit Breaker | Resilience4j | Evitar cascada ante fallos de ms-geoapi. |
| Dashboards | Grafana | Visualizar KPIs (solicitudes por estado, latencia de rutas). |

### Pasos Sugeridos (Orden)
1. Activar Actuator en cada microservicio (`management.endpoints.web.exposure.include=health,info,prometheus`).
2. Añadir dependencia Prometheus + configurar scrape en `docker-compose`.
3. Integrar OpenTelemetry (auto-instrumentación Spring Boot, export gRPC/HTTP). 
4. Definir panel Grafana: latencia promedio por endpoint, throughput, errores 4xx/5xx.

---
## 3. Swagger / Documentación de APIs
### Estado Actual
- Cada servicio expone:
  - `/swagger-ui.html`
  - `/api-docs`
- Gateway reescribe paths para acceder vía prefijos agregados (ej.: `/flota/swagger-ui.html` → reescrito internamente a ms-flota).

### Justificación
- Permite probar y navegar documentación desde un único puerto (`8080`) sin exponer puertos internos.
- Reduce fricción de acceso en entornos de despliegue.

### Mejora Propuesta
| Acción | Beneficio |
|--------|-----------|
| Unificar especificaciones con agregador (OpenAPI merge) | Vista consolidada del dominio. |
| Versionar APIs (`/v1`, `/v2`) en rutas y specs | Evolución segura sin romper clientes. |
| Añadir ejemplos y descripciones extensas | Mejora onboarding de nuevos desarrolladores. |
| Seguridad en Swagger (Authorize button con OAuth2) | Pruebas autenticadas directas desde UI. |

---
## 4. Roles y Autorización (Keycloak)
### Idea Central
- Keycloak es el origen de identidad: usuarios y roles (`ADMIN`, `TRANSPORTISTA`, `CLIENTE`).
- El **JWT** emitido incluye:
  - Claim `roles` (lista de roles asignados).
  - Claims específicos: `clienteId`, `transportistaId`, `adminId`.

### Uso Práctico
- `clienteId` permite filtrar solicitudes pertenecientes al cliente autenticado sin enviar explícitamente el ID en cada petición.
- `transportistaId` permite recuperar tramos y asignaciones propias.
- `adminId` reservado para operaciones administrativas / auditoría.

### Validación
- El API Gateway valida firma y emisor (`issuer-uri` Keycloak), después enruta.
- Microservicios confían en que la petición ya está autenticada (no se observa configuración `resourceserver` interna). 

### Mejora Propuesta
| Acción | Beneficio |
|--------|-----------|
| Validar JWT en cada microservicio (`spring.security.oauth2.resourceserver.jwt`) | Defense in depth. |
| Mapear roles a autoridades (`ROLE_ADMIN`, etc.) | Uso directo en `@PreAuthorize`. |
| Políticas finas (atributos vs roles) | Control granular (ej.: acceso sólo a sus solicitudes). |
| Rotación de `client_secret` y uso de vault | Seguridad de credenciales. |
| Agregar refresh tokens y expiración corta access token | Mitigación de uso indebido. |

### Ejemplo Configuración a Añadir (Microservicio)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/tpi
```
Y en código:
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> crearTarifa(...) { ... }
```

---
## 5. Roadmap Integrado
| Fase | Objetivo | Entregables |
|------|----------|-------------|
| 1 | Actuator + métricas básicas | health, info, prometheus endpoints |
| 2 | Centralizar logging | Formato JSON + traceId |
| 3 | Trazas distribuidas | OTel SDK + Collector + Jaeger |
| 4 | Autorización granular | Anotaciones + validación JWT interna |
| 5 | Dashboards negocio | KPI solicitudes, tiempos, costos reales |
| 6 | Hardening seguridad | Rotación secretos, perfiles prod ajustados |

---
## 6. Resumen Rápido (TL;DR)
- Logging: Verboso (SQL DEBUG) + filtro simple en Gateway. 
- Observabilidad: Actuator sólo en Gateway; falta métricas y trazas.
- Swagger: Disponible en cada servicio con acceso centralizado vía Gateway rewrite.
- Autorización: Roles y claims en JWT; Gateway valida. Mejora: validar también en cada servicio y aplicar políticas finas.
- Próximo paso clave: Añadir métricas + perfil prod real + validación JWT interna.

---
## 7. Glosario
- **Actuator**: Endpoints de administración (health, info, metrics). 
- **Prometheus**: Sistema de recolección de métricas. 
- **OpenTelemetry (OTel)**: Estándar para trazas, métricas y logs. 
- **Swagger / OpenAPI**: Especificación y UI para explorar endpoints. 
- **Claim**: Dato dentro del JWT (roles, clienteId). 
- **Correlación**: Vincular logs de múltiples servicios para un mismo flujo.

---
¿Necesitas ejemplos concretos de configuración Prometheus + OTel, o un diagrama ASCII de flujo con trazas? Pídelo y lo agregamos.
