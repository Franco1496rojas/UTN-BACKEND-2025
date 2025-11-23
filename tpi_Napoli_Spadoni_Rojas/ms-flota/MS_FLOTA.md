# MS-FLOTA - Documentación Técnica

## 1. Propósito del Microservicio
El microservicio **ms-flota** centraliza la gestión de recursos logísticos que impactan en el cálculo de costos y planificación de rutas:
- Transportistas (personas que operan camiones)
- Administradores (gestión interna)
- Camiones (capacidad de peso/volumen, costo base por km, consumo)
- Depósitos (capacidad, ocupación, estadía)
- Tarifas por rangos de volumen y peso (`tarifas_rango_volumen_peso`)
- Parámetros globales de tarifa (`parametros_tarifa`: precio combustible, cargo fijo por tramo)

Es consumido por **ms-operaciones** para estimar costos de solicitudes logísticas y validar disponibilidad de recursos físicos.

## 2. Arquitectura Interna
```
flota/
  MsFlotaApplication.java        # Punto de arranque Spring Boot
  controllers/                   # Endpoints REST públicos
  models/                        # Entidades JPA (tablas y relaciones)
  repositories/                  # Interfaces Spring Data
  services/                      # Lógica y validaciones de negocio
  config/                        # (No se listaron configs adicionales específicas aquí)
```

Patrones aplicados:
- Controller ↔ Service ↔ Repository: separación de capas.
- Validaciones de dominio en Services (unicidad, rangos, consistencia, capacidad).
- Uso de builders y Lombok para reducir boilerplate.
- `ddl-auto=update` permite que Hibernate sincronice cambios menores con el schema (complementado por migraciones SQL externas para columnas adicionales).

## 3. Dependencias Clave (pom.xml)
Similar a `ms-clientes`:
```xml
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-validation</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>org.postgresql:postgresql</dependency>
<dependency>org.springdoc:springdoc-openapi-starter-webmvc-ui</dependency>
<dependency>org.projectlombok:lombok</dependency>
```
Funcionalidad:
- Web: API REST.
- Validation: Bean Validation (complementa validaciones manuales).
- JPA + PostgreSQL: Persistencia ORM.
- Springdoc: Documentación OpenAPI.
- Lombok: Construcción fluida de entidades y DTO simples.

## 4. Configuración (application.yml / application-dev.yml)
Principales aspectos:
- Puerto: `8082`.
- Datasource apuntando a Postgres (contenedor vs localhost en `dev`).
- `hibernate.ddl-auto=update`: permite agregar columnas/cambios simples sin intervención manual inmediata.
- `show-sql=true` para visibilidad en desarrollo.
- Swagger expuesto en `/swagger-ui.html` y `/api-docs`.
- En `application-dev.yml` se fija timezone en UTC vía Hikari (`connection-init-sql`).

## 5. Entidades JPA (models/)
### Transportista
Campos: `dni` único, datos personales, `keycloakId` para correlación con identidad OIDC, `ciudad` como string.

### Administrador
Similar a `Transportista`; representa usuarios con rol administrativo.

### Camion
Campos:
- `dominio` (único): matrícula.
- `capacidadPeso`, `capacidadVolumen`.
- `disponibilidad` (boolean).
- `costoKmBase`, `consumoLitroKm` (usados para cálculos de tarifas reales).
- Relación `ManyToOne` con `Transportista`.

### Deposito
Campos:
- Datos geográficos: `latitud`, `longitud` (para cálculo de distancia y estadía).
- `costoEstadiaDiaria`: factor costo adicional.
- `estado`: habilitado / deshabilitado.
- `capacidadMaxima`, `cantidadOcupada` (control de ocupación).
Métodos utilitarios encapsulan lógica de capacidad (`tieneCapacidadDisponible`, `incrementarOcupacion`, etc.).

### TarifaRango
Define un rango doble (volumen y peso) con `costoKmBase` asociado:
- Evita solapamientos entre rangos (validado en Service).
- Se utiliza para seleccionar costo base según características del contenedor.

### ParametrosTarifa
Valores globales:
- `precioLitroCombustible` (afecta costo variable).
- `cargoFijoTramo` (costo fijo por tramo adicional en cálculos compuestos).

## 6. Repositorios (repositories/)
Ejemplos:
- `CamionRepository.findByDisponibilidadTrue()` → lista camiones libres.
- `TarifaRangoRepository.findOverlappingRangos(...)` → Query personalizada para detectar solapamientos en volumen/peso.
- `DepositoRepository.existsByNombreIgnoreCase(String nombre)` → control de unicidad.
- `ParametrosTarifaRepository` y `TransportistaRepository` utilizan CRUD estándar.

## 7. Servicios (services/)
### CamionService
Validaciones:
1. Unicidad de `dominio` en creación/actualización.
2. Transportista debe existir (carga explícita del objeto para evitar referencias huecas).
3. Filtros de disponibilidad por peso y volumen (`findDisponiblesPesoVolumen`).

### TransportistaService / AdministradorService
CRUD directo sin validaciones complejas (se podría ampliar con restricciones de email/dni).

### DepositoService
Validaciones robustas:
1. Nombre, dirección no vacíos.
2. Costos y capacidades positivos.
3. `cantidadOcupada` coherente (0 <= ocupada <= capacidad).
4. Unicidad de nombre.
5. Evitar eliminar depósito con ocupación > 0.
6. Métodos específicos para incrementar/decrementar ocupación con lógica encapsulada.

### TarifaService
Validaciones de integridad de rangos:
1. `volumenMin < volumenMax` y `pesoMin < pesoMax`.
2. No negativos y costo por km > 0.
3. Prevención de solapamientos (query custom `findOverlappingRangos`).

### ParametrosTarifaService
CRUD simple de parámetros globales; puede extenderse para control de versión o histórico.

## 8. Controladores (controllers/)
### CamionController (`/api/camiones`)
| Método | Path                              | Parámetros                                 | Descripción |
|--------|-----------------------------------|--------------------------------------------|-------------|
| GET    | /api/camiones                     | `transportistaId` opcional                 | Lista todos o filtra por transportista |
| GET    | /api/camiones/disponibles         | `pesoMaximo`, `volumenMaximo` opcionales   | Filtra camiones disponibles capaces |
| PUT    | /api/camiones/disponibilidad      | `camionId`, `disponible`                   | Cambia estado disponibilidad |
| GET    | /api/camiones/{id}                |                                            | Obtiene camión por ID |
| GET    | /api/camiones/libres              |                                            | Cantidad disponible (libres) |
| GET    | /api/camiones/ocupados            |                                            | Cantidad ocupada |
| POST   | /api/camiones                     | Body Camion                                | Crea con validaciones dominio y transportista |
| PUT    | /api/camiones/{id}                | Body Camion                                | Actualiza conservando unicidad |
| DELETE | /api/camiones/{id}                |                                            | Elimina |

### TransportistaController (`/api/transportistas`)
| Método | Path | Descripción |
|--------|------|-------------|
| GET    | /api/transportistas | Lista todos |
| GET    | /api/transportistas/{id} | Obtiene por ID |
| POST   | /api/transportistas | Crea |
| PUT    | /api/transportistas/{id} | Actualiza |
| DELETE | /api/transportistas/{id} | Elimina |

### AdministradorController (`/api/administradores`)
| Método | Path | Descripción |
|--------|------|-------------|
| GET    | /api/administradores | Lista administradores |
| POST   | /api/administradores | Crea administrador |

### DepositoController (`/api/depositos`)
| Método | Path | Parámetros | Descripción |
|--------|------|-----------|-------------|
| GET    | /api/depositos | - | Lista depósitos |
| GET    | /api/depositos/{id} | - | Obtiene depósito por ID |
| GET    | /api/depositos/{id}/contenedores-pendientes | - | Devuelve ocupación, disponible y capacidad |
| POST   | /api/depositos | Body Deposito | Crea (inicializa ocupación si null) |
| PUT    | /api/depositos/{id} | Body Deposito | Actualiza con validaciones |
| DELETE | /api/depositos/{id} | - | Elimina si ocupación == 0 |
| PATCH  | /api/depositos/{id}/incrementar-ocupacion | `cantidad` | (Deprecated) Incrementa ocupación |
| PATCH  | /api/depositos/{id}/decrementar-ocupacion | `cantidad` | (Deprecated) Decrementa ocupación |

### TarifaController (`/api/tarifas`)
| Método | Path | Descripción |
|--------|------|-------------|
| GET    | /api/tarifas | Lista rangos de tarifas |
| GET    | /api/tarifas/{id} | Obtiene tarifa por ID |
| POST   | /api/tarifas | Crea validando rangos y solapamientos |
| PUT    | /api/tarifas/{id} | Actualiza manteniendo integridad |
| DELETE | /api/tarifas/{id} | Elimina |

### ParametrosTarifaController (`/api/parametros-tarifa`)
| Método | Path | Descripción |
|--------|------|-------------|
| GET    | /api/parametros-tarifa | Lista parámetros actuales |
| POST   | /api/parametros-tarifa | Crea / actualiza valor inicial |
| PUT    | /api/parametros-tarifa/{id} | Actualiza registro existente |

### Manejo de Errores
Controladores usan `ErrorResponse` interno para retornos semánticos (mensaje). Se recomienda evolucionar a manejo global (`@ControllerAdvice`) para formato consistente (timestamp, path, código, detalles).

## 9. Validaciones Críticas Resumidas
| Entidad | Validaciones Principales |
|---------|--------------------------|
| Camion | Dominio único, transportista existente, capacidades presentes |
| Deposito | Nombre único, no eliminar si ocupado, capacidad ≥ ocupada, costo estadía > 0 |
| TarifaRango | Rango volumen/peso consistente, sin solapamientos, costo > 0 |
| ParametrosTarifa | Valores positivos (extensible) |
| Transportista / Administrador | (Actual: sin validaciones estrictas; mejora pendiente) |

## 10. Cómo Contribuye al Cálculo de Costos
1. Selección de tarifa base por contenedor (rango volumen/peso).
2. Ajuste por `costoKmBase` específico del camión (puede diferir del rango genérico).
3. `consumoLitroKm` + `precioLitroCombustible` → costo variable de combustible.
4. `cargoFijoTramo` → agrega costo fijo por tramo adicional en rutas multisegmento.
5. Estadía en `Deposito` → costos extras según días/calculado por `costoEstadiaDiaria`.

## 11. Interacción con Otros Microservicios
- `ms-operaciones` consulta:
  - /api/camiones/disponibles → asignación de recursos.
  - /api/depositos → disponibilidad y costos de estadía.
  - /api/tarifas → costo base por rango.
  - /api/parametros-tarifa → factores globales de cálculo.
  - /api/transportistas → información operativa si se asigna conductor.

## 12. Oportunidades de Mejora
1. Añadir restricciones (Bean Validation) en entidades (ej. `@Email`, `@Positive`).
2. Introducir DTOs (evitar exponer entidades JPA directamente para flexibilidad futura).
3. Integrar caching para tarifas y parámetros (datos de lectura frecuente, baja mutación).
4. Unificar respuesta de errores con un formato estándar.
5. Control de concurrencia al actualizar ocupación de depósitos (optimistic locking con `@Version`).
6. Endpoints para búsqueda avanzada de camiones (por rango de consumo, costo, disponibilidad futura).
7. Histórico de cambios en parámetros de tarifas (auditoría y comparación temporal).
8. Agregar seguridad basada en roles (solo ADMIN crea o modifica tarifas y parámetros).

## 13. Ejemplo de Flujo: Asignación de Camión
1. `ms-operaciones` recibe características (peso/volumen) del contenedor.
2. Llama `GET /api/camiones/disponibles?pesoMaximo=X&volumenMaximo=Y`.
3. Filtra camión adecuado según capacidades y disponibilidad.
4. Obtiene tarifa aplicable (`/api/tarifas`) y parámetros globales.
5. Calcula costo estimado de tramo (combina costo base + combustible + cargo fijo).
6. Marca camión como no disponible (`PUT /api/camiones/disponibilidad`).

## 14. Resumen para Presentación Oral
“ms-flota administra todos los recursos físicos y parámetros económicos necesarios para calcular costos logísticos: camiones con sus capacidades y costos base, transportistas operativos, depósitos con control de ocupación, tarifas escalonadas por rangos de peso y volumen y parámetros globales de combustible y cargos. Expone endpoints REST con validaciones que garantizan integridad (unicidad de dominio, no solapamiento de tarifas, capacidad de depósitos) y provee los datos esenciales que ms-operaciones consume para estimar y ajustar costos de transporte.”

---
Última actualización generada automáticamente.
