# ms-operaciones

## 1. Propósito y Alcance
El microservicio `ms-operaciones` orquesta el ciclo de vida completo de una operación logística solicitada por un cliente: creación de la solicitud, generación y asignación de rutas, gestión de tramos, cálculo de costos estimados y reales, manejo de estados y cierre de la operación. Integra datos y capacidades de otros servicios (clientes, flota y geoapi) para producir información consolidada y trazable.

## 2. Responsabilidades Clave
- Registrar solicitudes (crea cliente si no existe y siempre crea contenedor nuevo).
- Generar rutas tentativas (directa e indirecta vía depósito) con estimaciones de distancia, costo y tiempo.
- Asignar ruta definitiva a una solicitud (directa o indirecta) creando tramos en estado ESTIMADO.
- Asignar camión a cada tramo validando capacidades y disponibilidad.
- Iniciar y finalizar tramos calculando métricas reales (tiempo, distancia, costo transporte, costos de estadía en depósito).
- Actualizar costos y tiempos totales en rutas y solicitudes (estimados y reales).
- Gestionar el flujo de estados de la solicitud y registrar historial de cambios.

## 3. Flujo General de una Operación
1. Cliente envía datos mínimos para crear la solicitud (origen, destino, datos de contenedor y cliente).  
2. Servicio valida datos, crea cliente si hace falta; crea contenedor único; obtiene distancia vía `ms-geoapi`; calcula tiempo y costo estimado.  
3. La solicitud se crea en estado BORRADOR y se registra historial.  
4. Se solicita generación de rutas estimadas (directa e indirecta).  
5. Se selecciona una ruta tentativamente y se crea la ruta y sus tramos (estado ESTIMADO).  
6. Se asigna una ruta (directa o indirecta) mediante endpoint especializado: cambia estado a PROGRAMADA.  
7. Se asignan camiones a cada tramo: cuando todos están ASIGNADOS la solicitud pasa a estado ASIGNADA.  
8. Inicio del primer tramo: solicitud pasa a EN_TRANSITO.  
9. Cada tramo al finalizar calcula datos reales y libera camión.  
10. Último tramo finalizado: se consolidan costos y tiempo real total; solicitud pasa a COMPLETADA.  

## 4. Estados de la Solicitud
`BORRADOR` → `PROGRAMADA` → `ASIGNADA` → `EN_TRANSITO` → `COMPLETADA`

Historial de estados registrado en entidad `CambioEstadoSolicitud` (estadoAnterior, estadoNuevo, fechaCambio, observaciones). Cada transición generada por servicios `SolicitudService`, `RutaService` y `TramoService` según hitos operativos.

## 5. Estados de Tramo
- `ESTIMADO`: creado al generar/seleccionar ruta.  
- `ASIGNADO`: tras asignar camión (capacidad y disponibilidad validadas).  
- `INICIADO`: al invocar inicio (primer tramo dispara cambio a EN_TRANSITO si la solicitud estaba ASIGNADA).  
- `FINALIZADO`: calcula métricas reales, estadías y costos; si todos los tramos finalizados → solicitud COMPLETADA.  

## 6. Principales Entidades (Esquema Resumido)
### Solicitud
- `clienteId`, `contenedorId` (referencias externas).  
- `origen`, `destino`, `distanciaKm` (estimada inicial), `costoEstimado`, `costoReal`.  
- `tiempoEstimadoMin`, `tiempoRealMin`.  
- `estadoActual`, `fechaSolicitud`.  
- Relaciones: `rutas` (1..n), `historialEstados` (1..n).  

### Ruta
- Relación a `solicitud`.  
- `fechaInicio`, `fechaFinEstimada`.  
- `distanciaTotalKm`, `costoTotal` (estimados agregados).  
- `tramos` (1..n).  

### Tramo
- Relación a `ruta`.  
- `origen`, `destino`, `distanciaKm` (estimada) y `distanciaKmReal`.  
- `costo` (estimado), `costoReal` (transporte + estadía), `costoEstadia`, `diasEstadia`.  
- `fechaInicio`, `fechaFinEstimada`, `fechaHoraInicioReal`, `fechaHoraFinReal`, `duracionMinReal`.  
- `camionId`, `transportistaId` (externos), `depositoOrigenId`, `depositoDestinoId`, `orden`, `estado`, `tipo`.  

### CambioEstadoSolicitud
- `solicitud`, `estadoAnterior`, `estadoNuevo`, `fechaCambio`, `observaciones`.  

### DTOs
- `RegistrarSolicitudDTO`: datos cliente + contenedor + origen/destino.  
- `RutaTentativaDTO`, `TramoEstimadoDTO`: estructura para rutas generadas antes de persistir definitiva.  
- `SeleccionRutaDTO`: selección final con lista de tramos estimados.  
- `AsignarRutaDTO`: tipo de ruta a asignar (directa / indirecta).  

## 7. Integraciones Externas
- `ms-clientes`: crear/buscar cliente, crear contenedor, obtener datos de contenedor.  
- `ms-flota`: obtener camiones disponibles (filtra por peso/volumen), parámetros globales (precio combustible, cargo fijo, velocidad promedio), depósitos (para rutas indirectas), tarifas por rango (volumen/peso) y datos de camión individual (consumo, costos).  
- `ms-geoapi`: cálculo de distancia (Haversine o proveedor externo). Usado en: creación solicitud, generación de rutas, finalización de tramo para distancia real.  

## 8. Cálculo de Costos
### Costo Estimado de Solicitud (SolicitudService.registrarSolicitud / save)
1. Distancia estimada (`geoapi`).  
2. Tiempo estimado = distancia / 70 km/h.  
3. Obtener parámetros globales (`precioLitroCombustible`, `cargoFijoTramo`).  
4. Promedio de consumo de camiones aptos (`consumoLitroKm`).  
5. Tarifa base por km según rangos de volumen/peso (TarifaRangoDTO).  
6. Fórmula aproximada:  
   `costoEstimado = (distancia * consumoPromedio * precioLitro) + (cargoFijoTramo) + (costoKmBase * distancia)` (redondeado).  

### Costo de Tramo (CalculoCostoService / TramoService.save)
`costoTramo = distanciaKm * (costoKmTarifa + costoKmCamion) + distanciaKm * consumoLitroKm * precioLitro + cargoFijoTramo`  
Variables obtenidas de ms-flota y del camión específico.  

### Costo Real de Tramo (TramoService.finalizarTramo)
Recalcula usando distancia real y consumo específico del camión + cargo fijo + costo base por km del camión.  
Si procede desde un depósito, se suma costo de estadía:  
`costoEstadia = diasEstadia * costoEstadiaDiariaDeposito` (días redondeados hacia arriba según horas entre fin del tramo anterior y inicio real del siguiente).  

### Consolidación
- Ruta: sumatoria de costos y distancias de tramos (estimada).  
- Solicitud: costoEstimado y tiempoEstimado actualizados al asignar ruta; costoReal y tiempoReal calculados al finalizar último tramo.  

## 9. Endpoints Principales
### Solicitudes (`/api/solicitudes`)
- `GET /api/solicitudes?clienteId=&estado=`: listar filtrado.  
- `GET /api/solicitudes/{id}`: obtener por ID.  
- `POST /api/solicitudes`: crear solicitud manual (usa `save`).  
- `POST /api/solicitudes/registrar`: flujo completo de registro (crea cliente, contenedor, distancia, costo).  
- `PUT /api/solicitudes/{id}/estado?nuevoEstado=&observaciones=`: cambiar estado manual.  
- `POST /api/solicitudes/{id}/asignar-ruta`: asignar ruta directa/indirecta y cambiar estado BORRADOR→PROGRAMADA.  

### Rutas (`/api/solicitudes/{id}/...`)
- `POST /api/solicitudes/{id}/rutas/estimadas`: generar lista (directa + mejor depósito).  
- `POST /api/solicitudes/{id}/ruta`: persistir selección (`SeleccionRutaDTO`) creando ruta + tramos ESTIMADO.  

### Tramos (`/api/tramos`)
- `GET /api/tramos?rutaId=`: listar todos o por ruta.  
- `POST /api/tramos`: crear tramo (recalcula totales).  
- `POST /api/tramos/{tramoId}/asignar-camion`: valida y asigna camión (tramo→ASIGNADO, posible cambio solicitud→ASIGNADA).  
- `POST /api/tramos/{tramoId}/iniciar`: inicia tramo (ASIGNADO→INICIADO; si orden=1 entonces solicitud→EN_TRANSITO).  
- `POST /api/tramos/{tramoId}/finalizar`: finaliza tramo (INICIADO→FINALIZADO; calcula métricas reales, estadía y puede completar solicitud).  

## 10. Validaciones Clave
- Origen ≠ destino, no nulos.  
- Peso y volumen de contenedor > 0.  
- Código de contenedor único si se provee.  
- Tipo ruta aceptado: `directa` | `indirecta`.  
- Solicitud debe estar en BORRADOR para asignar ruta; no debe tener otra ruta previa.  
- Tramo en ESTIMADO para asignar camión; en ASIGNADO para iniciar; en INICIADO para finalizar.  
- Camión disponible y con capacidades suficientes (peso/volumen).  
- Estadía sólo si tramo previo finalizado llega a depósito y siguiente inicia desde mismo depósito.  

## 11. Persistencia y Actualización de Totales
Servicios recalculan agregados tras cambios:  
- `RutaService.recalcTotales` suma costo/distancia de tramos.  
- `SolicitudService.recalcTotales` toma sumatorias de rutas.  
- `TramoService.save/delete` desencadena recálculos encadenados.  

## 12. Migraciones y Columnas (PostgreSQL)
Migraciones agregan:  
- Columnas para costo real vs estimado en solicitud y tramos.  
- Campos de estadía (días, costo) en tramos.  
- Tiempos reales y estimados para análisis y KPIs futuros.  

## 13. Manejo de Errores
Controladores devuelven `ErrorResponse { mensaje }` con códigos adecuados:  
- 400 Validaciones de negocio o datos faltantes.  
- 404 Identificadores inexistentes (solicitud, tramo, camión).  
- 500 Errores internos o servicios externos no disponibles (se intenta mensaje claro).  

## 14. Diferencias entre Flujos de Ruta
| Aspecto | Directa | Indirecta |
|---------|---------|-----------|
| Tramos | 1 | 2 (por depósito) |
| Posible Estadía | No | Sí (si existe espera entre tramos) |
| Costo | Sólo transporte | Transporte + potencial estadía |
| Distancia | Origen→Destino | Origen→Depósito→Destino (optimiza distancia total) |

## 15. Consideraciones de Diseño
- Separación clara Controller → Service; lógica de cálculo centralizada en servicios para reutilización.  
- Historial de estados persistente habilita auditoría y métricas (tiempo en cada estado).  
- Costo real diferido al final permite comparar desviación vs estimado.  
- Uso de DTOs desacopla modelo persistido de alternativas de ruta y selección.  
- Validaciones redundantes (capacidad camión) protegen contra inconsistencias en disponibilidad entre llamados.  

## 16. Posibles Mejoras Futuras
- Persistir rutas tentativas para análisis histórico y recomendaciones.  
- Algoritmo de optimización multi-depósito y ventanas de tiempo.  
- Reintentos/resiliencia avanzada en integraciones externas (circuit breakers).  
- Métricas Prometheus (tiempos reales vs estimados, % desviación costo).  
- Publicación de eventos (Kafka) al cambiar estados para notificaciones y BI.  
- Versionar tarifas y registrar cuál se aplicó a cada tramo.  
- Validación de solapamiento temporal de uso de camión en múltiples solicitudes.  

## 17. Resumen Ejecutivo
`ms-operaciones` centraliza la lógica de negocio del transporte: transforma entradas mínimas del cliente en una operación completa con trazabilidad de cada tramo y comparación estimado vs real, integrándose con flota, geo y clientes para enriquecer datos y mantener coherencia operativa.

---
Última actualización: generada automáticamente para exposición técnica.
