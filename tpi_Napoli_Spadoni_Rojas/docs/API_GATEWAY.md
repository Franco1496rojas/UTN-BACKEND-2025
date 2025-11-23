# API Gateway - Documentación Técnica

## 1. Propósito
El **API Gateway** es el punto único de entrada al ecosistema de microservicios. Centraliza:
- Enrutamiento hacia los servicios internos (`ms-clientes`, `ms-flota`, `ms-operaciones`, `ms-geoapi`).
- Exposición unificada de documentación Swagger de cada microservicio.
- Aplicación de filtros transversales (logging, CORS, reescrituras de paths).
- Validación de tokens JWT emitidos por Keycloak (cuando actúa como Resource Server).
- Monitoreo básico vía Actuator.

## 2. Dependencias (pom.xml)
Fragmento relevante del `pom.xml`:
```xml
<dependencies>
    <!-- Enrutamiento reactivo usando Spring Cloud Gateway (WebFlux) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
    </dependency>

    <!-- Actuator: salud, info y endpoint de gateway -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Validación de JWT (Keycloak como proveedor OIDC) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Documentación OpenAPI/Swagger para aplicaciones WebFlux -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        <version>2.6.0</version>
    </dependency>
</dependencies>
```
Estas dependencias permiten:
- Procesar peticiones de forma reactiva (alto throughput / backpressure).
- Exponer documentación unificada y endpoints de salud.
- Validar tokens sin necesidad de escribir lógica custom (configuración declarativa).

## 3. Configuración de Rutas (application.yml)
El archivo `application.yml` define un conjunto de rutas bajo `spring.cloud.gateway.routes`. Cada ruta incluye:
- `id`: Identificador único.
- `uri`: Destino (microservicio interno por hostname de contenedor y puerto).
- `predicates`: Condiciones que debe cumplir la petición (Path, Query, etc.).
- `filters`: Transformaciones sobre la petición o respuesta (StripPrefix, RewritePath).

### Ejemplo de Ruta Simple
```yaml
- id: ms-clientes
  uri: http://ms-clientes:8081
  predicates:
    - Path=/api/clientes/**, /api/contenedores/**, /api/ciudades/**, /api/provincias/**
  filters:
    - StripPrefix=0
```
Explicación:
- El predicate `Path` hace matching sobre varios patrones (separados por coma).
- `StripPrefix=0` mantiene el path original al reenviar.

### Ejemplo con Query Predicate y Prioridad
```yaml
- id: ms-operaciones-contenedores-estado
  uri: http://ms-operaciones:8083
  predicates:
    - Path=/api/contenedores/**
    - Query=estado, .*
  filters:
    - StripPrefix=0
  order: -1
```
Explicación:
- La ruta solo aplica si el path coincide y existe un parámetro `estado` en la query string.
- `order: -1` altera el orden de evaluación para que esta ruta tenga prioridad sobre otras potencialmente solapadas.

### Rutas para Swagger de Microservicios
Se emplea `RewritePath` para transformar prefijos externos a rutas internas del microservicio:
```yaml
- id: ms-flota-swagger
  uri: http://ms-flota:8082
  predicates:
    - Path=/flota/swagger-ui.html, /flota/swagger-ui/**, /flota/api-docs/**
  filters:
    - RewritePath=/flota/(?<segment>.*), /${segment}
```
Regex `(?<segment>.*)` captura el resto del path luego de `/flota/` y lo reescribe eliminando el prefijo.

### CORS Global
```yaml
globalcors:
  cors-configurations:
    "[/**]":
      allowedorigins: "http://localhost:5173, http://localhost:4200, http://localhost:3000"
      allowedmethods: "*"
      allowedheaders: "*"
      allowcredentials: true
```
Permite frontends locales en distintos puertos sin errores de política de mismo origen.

### Seguridad JWT
```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        issuer-uri: http://keycloak:8080/realms/tpi
```
El Gateway valida la firma y claims del token emitido por el realm `tpi` en Keycloak. Esto permite aplicar reglas de autorización en filtros o controladores (si se agregan).

### Actuator
```yaml
management:
  endpoints:
    web:
      base-path: /actuator
      exposure:
        include: health,info,gateway
```
Habilita endpoints:
- `/actuator/health`: Estado de la app.
- `/actuator/info`: Información adicional (si se configura).
- `/actuator/gateway`: Inspección de rutas activas y filtros (solo lectura).

## 4. Filtros Globales y Personalizados
### LoggingFilter.java
Ubicación: `src/main/java/.../gateway/filters/LoggingFilter.java`
```java
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String reqId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        System.out.println("--> [" + reqId + "] " + exchange.getRequest().getMethod() + " " + exchange.getRequest().getURI());
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() ->
                System.out.println("<-- [" + reqId + "] " + exchange.getResponse().getStatusCode())
            ));
    }

    @Override
    public int getOrder() { return 0; }
}
```
Responsabilidad:
- Loguea cada petición entrante y la respuesta saliente.
- Usa opcionalmente un header `X-Request-Id` para correlación manual.
- Al ser `Ordered` con valor `0`, se ejecuta temprano en la cadena de filtros.

Limitaciones / Mejoras sugeridas:
- Reemplazar `System.out.println` por un logger (SLF4J) y agregar nivel (INFO/DEBUG).
- Generar un `X-Request-Id` si no está presente.
- Estructurar logs en JSON para trazabilidad avanzada.

## 5. Predicados Comunes en Gateway
| Predicate | Uso | Ejemplo | Comentario |
|----------|-----|---------|------------|
| Path     | Coincidencia por patrón de URL | `Path=/api/clientes/**` | Acepta múltiples patrones separados por coma. |
| Query    | Coincidencia por parámetro en query string | `Query=estado, .*` | Segundo argumento es regex para el valor. |
| Method   | Filtrar por verbo HTTP | `Method=GET` | (No usado en archivo actual pero disponible). |
| Host     | Coincidencia por dominio de la petición | `Host=*.midominio.com` | Útil en escenarios multi-tenant. |

## 6. Filtros Comunes
| Filtro       | Uso | Ejemplo | Efecto |
|--------------|-----|---------|--------|
| StripPrefix  | Elimina segmentos iniciales del path | `StripPrefix=1` | Remueve el primer segmento antes de reenviar. |
| RewritePath  | Reescribe el path con regex | `RewritePath=/flota/(?<s>.*), /${s}` | Permite exponer Swagger de servicios con prefijos. |
| AddRequestHeader | Inserta header | `AddRequestHeader=X-Env, prod` | Enriquecimiento de metadata. |
| RemoveRequestHeader | Elimina header | `RemoveRequestHeader=Cookie` | Ocultar datos no necesarios al backend. |

## 7. Documentación Swagger unificada
El Gateway publica endpoints como:
- `/clientes/swagger-ui.html`, `/flota/swagger-ui.html`, `/operaciones/swagger-ui.html`, `/geoapi/swagger-ui.html`.
Internamente se reescriben a los paths reales del microservicio. Beneficios:
- Un único puerto externo (8080) para explorar todas las APIs.
- Simplifica pruebas manuales e integración con frontends tempranos.

## 8. CORS
Configuración global para permitir diferentes orígenes de frontends locales durante desarrollo (Vite, Angular, React). Ajustar en producción para reducir superficie de ataque.

## 9. Seguridad y Tokens
- El Gateway no crea tokens: confía en Keycloak como Authorization Server.
- Verifica `issuer` y firma del JWT; extrae roles y claims.
- Reglas de autorización específicas (role-based) pueden añadirse mediante anotaciones (@PreAuthorize) o filtros adaptativos.

## 10. Actuator y Observabilidad Inicial
- Endpoints expuestos permiten validar que las rutas están cargadas y el servicio está sano.
- Próximo paso recomendado: agregar métricas (Prometheus), tracing (OTel), y dashboards.

## 11. Mejoras Potenciales
1. Implementar Rate Limiting / Throttling (Resilience4j o filtros personalizados) para proteger backend.
2. Correlation ID automático si falta `X-Request-Id`.
3. Centralizar configuración sensible (issuer-uri, orígenes CORS) en variables externas o Config Server.
4. Cache de respuestas idempotentes (opcional) para reducción de latencia en catálogos.
5. Integrar autenticación mutua (mTLS) en entornos zero-trust.

## 12. Resumen de Valor
El API Gateway simplifica la interacción cliente ↔ ecosistema: oculta la complejidad de múltiples servicios, aplica políticas transversales y sirve como capa donde agregar reglas de seguridad, monitoreo y optimizaciones sin alterar cada microservicio.

---
Última actualización generada automáticamente.
