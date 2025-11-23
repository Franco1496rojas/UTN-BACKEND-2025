# MS-GEOAPI - Documentación Técnica

## 1. Propósito del Microservicio
**ms-geoapi** actúa como un adaptador externo para obtener distancia y duración estimadas entre dos puntos usando la API **Google Distance Matrix**. Es consumido por `ms-operaciones` para:
- Calcular distancia total de una solicitud logística.
- Estimar duración de tramos y rutas.
- Dar respaldo en cálculos de costos (combustible, tiempos y planificación).

Provee un endpoint simple (`/api/distancia`) que acepta parámetros `origen` y `destino` (direcciones o coordenadas lat,long) y retorna un objeto estructurado con distancia en kilómetros y duración en texto.

## 2. Arquitectura Interna
```
geoapi/
  MsGeoapiApplication.java     # Punto de arranque Spring Boot
  controller/                  # GeoController (exposición REST)
  service/                     # GeoService (lógica de integración + fallback)
  model/                       # DTO de respuesta (DistanceDTO)
  config/                      # (Reservado para futuras configuraciones)
```

Características:
- Sin persistencia (no hay JPA / base de datos local).
- Uso de `RestClient` (Spring 6+) para llamadas HTTP a Google APIs.
- Fallback Haversine si la llamada falla para coordenadas (mantiene operatividad mínima).

## 3. Dependencias Clave (pom.xml)
Incluye (resumen conceptual):
- `spring-boot-starter-web`: Exposición de API REST.
- `spring-boot-starter-validation`: Base para validaciones (aún no usadas extensivamente).
- `spring-boot-starter-actuator`: Salud y métricas futuras.
- `springdoc-openapi-starter-webmvc-ui`: Documentación Swagger.
- `spring-boot-starter-test` (test scope) y `lombok` para DTO y clases.

No incluye JPA ni driver PostgreSQL, reforzando que es un servicio “stateless” respecto a almacenamiento.

## 4. Configuración (application.yml)
```yaml
server.port: 8084
springdoc.api-docs.path: /api-docs
springdoc.swagger-ui.path: /swagger-ui.html
google.maps.apikey: (inyectada también por docker-compose)
```
La API Key se consume vía `@Value("${google.maps.apikey}")` en `GeoService`. Debe mantenerse secreta en producción (recomendado: variable de entorno + secreto gestionado). El valor presente en el repositorio sirve solo para desarrollo/demos y debería rotarse.

## 5. Modelo de Datos (DTO)
`DistanceDTO`:
```java
class DistanceDTO { String origen; String destino; double kilometros; String duracionTexto; }
```
- `kilometros`: Distancia en Km (double, redondeo según cálculo y/origen dado por Google).
- `duracionTexto`: Cadena descriptiva (“1 h 25 min”, “3 días”, etc.). En fallback Haversine se retorna "N/A" al no disponer de duración real.

## 6. Endpoints REST
### GET /api/distancia
Parámetros Query:
- `origen`: Dirección (ej: "Córdoba Capital") o coordenadas "lat,long" (ej: "-31.4201,-64.1888").
- `destino`: Igual formato que `origen`.

Respuesta (200 OK):
```json
{
  "origen": "Córdoba Capital",
  "destino": "Rosario",
  "kilometros": 400.23,
  "duracionTexto": "3 h 50 min"
}
```
Errores potenciales (internos) se degradan silenciosamente a retorno con `kilometros=0` y `duracionTexto="N/A"` o se intenta fallback.

## 7. Lógica de Integración (GeoService)
Flujo en `calcularDistancia(origen, destino)`:
1. Detecta si cada parámetro es coordenada con regex (`COORD_PATTERN`).
2. Realiza primera consulta a `/distancematrix/json` de Google.
3. Si falla o `status != OK`:
   - Si ambos son coordenadas → aplica **fallback Haversine** (distancia geodésica aproximada, sin duración real).
   - Si eran direcciones → reintenta agregando sufijo `", Argentina"` a cada una para mejorar geocodificación regional.
4. Construye `DistanceDTO` final.

### Construcción de URL
`/distancematrix/json?origins={}&destinations={}&units=metric&language=es&region=ar&key=API_KEY`
- Encoding aplicado a direcciones con `URLEncoder`.
- Coordenadas se “limpian” (remoción de espacios internos) sin encoding.

### Fallback Haversine
Se ejecuta solo si ambos parámetros son coordenadas y la llamada externa falla.
Formula Haversine:
```
dist = 2 * R * asin( sqrt( sin²((lat2 - lat1)/2) + cos(lat1)*cos(lat2)*sin²((lon2 - lon1)/2) ) )
```
Donde R ≈ 6371 Km. Resultado redondeado a 2 decimales.

## 8. Manejo de Errores y Resiliencia
Estrategia actual:
- Excepciones en consulta → fallback (si procede) o retorno de objeto con distancia 0.
- No hay distinción explícita entre errores de red, API Key inválida o parámetros mal formados.

Mejoras sugeridas:
1. Clasificación de errores (400 parámetros, 502 API externo, 401 API Key inválida).
2. Circuit Breaker / Retry controlado (Resilience4j) para evitar saturar Google ante fallos repetidos.
3. Cache corta (TTL) para consultas repetidas (ej: misma pair origen-destino en pocos minutos).
4. Métricas Actuator + tags (tiempo promedio de respuesta, ratio de fallback, conteo por tipo de origen).
5. Validación más estricta de direcciones (p.ej. mínimo 3 caracteres, evitar solo números).

## 9. Seguridad
Actualmente expone libremente `/api/distancia`. En un entorno productivo se recomienda:
- Añadir validación de token JWT (Resource Server) como otros servicios.
- Rate limiting por IP o por usuario autenticado.
- Ocultar la API Key (no dejarla en `application.yml` plano).

## 10. Consumo desde ms-operaciones
`ms-operaciones` llama el endpoint interno `http://ms-geoapi:8084/api/distancia?origen={}&destino={}` usando su RestClient/WebClient.
Uso típico:
1. Recibe solicitud logística con origen/destino textual.
2. Invoca ms-geoapi para obtener distancia estimada.
3. Almacena distancia y duración para cálculos de costo y planificación temporal.
4. Si se requieren tramos adicionales, podría llamar múltiples veces con combinaciones origen-depósito / depósito-destino.

## 11. Ejemplo de Petición y Respuesta
Solicitud:
```
GET /api/distancia?origen=Córdoba%20Capital&destino=Rosario
```
Respuesta (ejemplo):
```json
{
  "origen": "Córdoba Capital",
  "destino": "Rosario",
  "kilometros": 400.12,
  "duracionTexto": "3 h 48 min"
}
```

Con coordenadas:
```
GET /api/distancia?origen=-31.4201,-64.1888&destino=-32.9442,-60.6505
```
Si la API falla → fallback Haversine: distancia aproximada y duración "N/A".

## 12. Campos Clave y Consideraciones
- `kilometros`: Puede variar por condiciones de tráfico / ruta escogida (depende de API de Google).
- `duracionTexto`: Texto human-readable (no se parsea a minutos en el servicio actual, eso podría ser mejora futura para cálculos más precisos).
- Sensibilidad de API Key: debe ser restringida (domains aprobados, cuota, rotación).

## 13. Oportunidades de Mejora
| Mejora | Descripción |
|--------|-------------|
| Cache | Evitar llamadas repetidas para mismo par origen-destino en ventana corta |
| Normalización | Pre-procesar direcciones (trim, capitalizar) antes de consulta |
| Duración numérica | Añadir campo `duracionSegundos` para cálculos internos |
| Validación avanzada | Rechazar entradas demasiado cortas o con caracteres inválidos |
| Observabilidad | Métricas de éxito vs fallback, latencia media, top pares consultados |
| Gestión de cuota | Contador de uso para evitar sobrepasar límites de Google |
| Seguridad | Autenticación/JWT + rate limiting por usuario/rol |
| Geocoding previo | Validar direcciones y convertir a coordenadas para cálculos alternativos |

## 14. Resumen para Presentación Oral
“ms-geoapi es un microservicio ligero que encapsula la interacción con Google Distance Matrix. Recibe origen y destino (dirección o coordenadas), devuelve distancia y duración. Implementa un fallback Haversine cuando ambos puntos son coordenadas y la llamada externa falla, asegurando continuidad en cálculos críticos. Es una pieza reutilizable y desacoplada que evita duplicar lógica de integración en otros servicios como ms-operaciones.”

---
Última actualización generada automáticamente.
