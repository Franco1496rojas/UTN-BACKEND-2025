# MS-CLIENTES - Documentación Técnica

## 1. Propósito del Microservicio
El microservicio **ms-clientes** gestiona el dominio de:
- Clientes (datos personales y ubicación)
- Contenedores (carga asociada a cada cliente)
- Provincias y Ciudades (geografía y clasificación territorial)

Es una capa de datos para otros servicios (por ejemplo `ms-operaciones`) que necesitan validar existencia de clientes, recuperar contenedores disponibles o normalizar datos geográficos. Se apoya en **Spring Boot + Spring Data JPA** para persistencia y expone una API REST documentada con **springdoc OpenAPI**.

## 2. Arquitectura Interna
Estructura de paquetes:
```
clientes/
  MsClientesApplication.java          # Clase de arranque Spring Boot
  config/                             # Configuraciones transversales (OpenAPI, zona horaria, datos dev)
  controllers/                        # Controladores REST (endpoints públicos)
  models/                             # Entidades JPA (mapeo a tablas SQL)
  repositories/                       # Interfaces Spring Data (consultas y persistencia)
  services/                           # Lógica de negocio y validaciones
```

Separación de responsabilidades:
- Controller: Entrada HTTP, parsing de parámetros y devolución de códigos de estado.
- Service: Reglas de negocio/validaciones (existencia, unicidad, formatos, integridad referencial).
- Repository: Acceso a datos (CRUD + métodos derivados por convención Spring Data).
- Model: Representación de filas en tablas, con relaciones (ManyToOne).
- Config: Autoconfiguraciones (Swagger, timezone, inicialización de datos de desarrollo).

## 3. Dependencias Clave (pom.xml)
```xml
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-validation</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>org.postgresql:postgresql</dependency>
<dependency>org.springdoc:springdoc-openapi-starter-webmvc-ui</dependency>
<dependency>org.projectlombok:lombok</dependency>
```
Función:
- Web: Endpoints REST (Servlet stack).
- Validation: Bean Validation (aunque varias validaciones son manuales en los services).
- Data JPA: ORM con Hibernate.
- PostgreSQL: Driver de base de datos.
- Springdoc: Documentación dinámica.
- Lombok: Reduce boilerplate (getters, setters, builders).

## 4. Configuración (application.yml)
Principales propiedades:
- `server.port=8081`
- Datasource: URL hacia Postgres (en contenedor), credenciales y dialecto.
- JPA: `ddl-auto=none` (schema gestionado externamente por scripts SQL).
- Logging SQL habilitado en desarrollo (`show-sql=true`).
- Swagger: `api-docs` y `swagger-ui.html` expuestos para documentación.

Perfil `dev` carga `DataInitializer` para datos de prueba internos si la base está vacía.

## 5. Entidades JPA (models/)
### Cliente
```java
@Entity @Table(name = "clientes")
class Cliente { Long id; Integer dni; String nombre; String apellido; String email; String telefono; String domicilio; String keycloakId; Ciudad ciudad; }
```
- `dni` único. `ciudad` relación ManyToOne.
- `keycloakId` enlaza el registro con identidad autenticada.

### Contenedor
```java
@Entity @Table(name = "contenedores")
class Contenedor { Long id; String codigo; Double peso; Double volumen; Cliente cliente; }
```
- `codigo` único. Asociado a un `Cliente`.
- Permite calcular volúmenes/pesos en otros servicios.

### Provincia
```java
@Entity @Table(name = "provincias")
class Provincia { Long id; String nombre; }
```
- Unicidad controlada en service (`existsByNombreIgnoreCase`).

### Ciudad
```java
@Entity @Table(name = "ciudades")
class Ciudad { Long id; String nombre; String codigoPostal; Provincia provincia; }
```
- Restricción lógica: no duplicar `nombre` dentro de la misma `provincia`.
- `codigoPostal` también validado para unicidad.

## 6. Repositorios (repositories/)
Ejemplos de métodos derivados:
- `ClienteRepository.findByApellidoContainingIgnoreCase(String apellido)`.
- `ContenedorRepository.findByClienteId(Long clienteId)`, `existsByCodigoIgnoreCase(String codigo)`.
- `ProvinciaRepository.findByNombreIgnoreCase(String nombre)`.
- `CiudadRepository.findByProvinciaId(Long provinciaId)` y validadores combinados (`existsByNombreIgnoreCaseAndProvinciaId`).

Spring Data genera automáticamente la implementación según nombres de métodos.

## 7. Servicios (services/)
### ClienteService
- Valida que la ciudad exista antes de guardar.
- Permite filtrar por apellido (búsqueda parcial).

### ContenedorService
Validaciones encadenadas:
1. Código no vacío.
2. Peso y volumen > 0.
3. Cliente presente y existente.
4. Código único (creación) y no entra en conflicto (actualización).
5. Normaliza código a mayúsculas.

### ProvinciaService
Validaciones:
1. Nombre no vacío y longitud mínima (>=3).
2. Unicidad de nombre en creación y actualización.
3. Normaliza capitalización (primera letra de cada palabra).

### CiudadService
Validaciones:
1. Nombre no vacío y longitud mínima (>=2).
2. Código postal no vacío y único.
3. Provincia existente.
4. Unicidad de nombre dentro de provincia (creación/actualización).
5. Normaliza nombre capitalizando palabras.

### Beneficio del Patrón
Centralizar validaciones mantiene controladores livianos y facilita reutilizar reglas desde otros componentes (si se expone lógica interna en el futuro).

## 8. Controladores (controllers/)
### ClienteController (`/api/clientes`)
| Método | Path              | Parámetros            | Descripción                                  |
|--------|-------------------|-----------------------|----------------------------------------------|
| GET    | /api/clientes     | `apellido` opcional   | Lista todos o filtra por apellido parcial    |
| GET    | /api/clientes/{id}|                       | Obtiene cliente por ID                       |
| POST   | /api/clientes     | Body Cliente          | Crea cliente (requiere ciudad existente)     |
| PUT    | /api/clientes/{id}| Body Cliente          | Actualiza datos (mantiene ID)                |
| DELETE | /api/clientes/{id}|                       | Elimina cliente                              |

### ContenedorController (`/api/contenedores`)
| Método | Path                     | Parámetros              | Descripción                                         |
|--------|--------------------------|-------------------------|-----------------------------------------------------|
| GET    | /api/contenedores        | `clienteId` opcional    | Lista contenedores o filtra por cliente             |
| GET    | /api/contenedores/{id}   |                         | Obtiene contenedor                                 |
| POST   | /api/contenedores        | Body Contenedor         | Crea validando unicidad y referencia a Cliente     |
| PUT    | /api/contenedores/{id}   | Body Contenedor         | Actualiza; controla conflicto de código            |
| DELETE | /api/contenedores/{id}   |                         | Elimina si existe                                  |

### ProvinciaController (`/api/provincias`)
| Método | Path                 | Descripción                       |
|--------|----------------------|-----------------------------------|
| GET    | /api/provincias      | Lista todas                       |
| GET    | /api/provincias/{id} | Busca por ID                      |
| POST   | /api/provincias      | Crea (validaciones de nombre)     |
| PUT    | /api/provincias/{id} | Actualiza con control de unicidad |
| DELETE | /api/provincias/{id} | Elimina si existe                 |

### CiudadController (`/api/ciudades`)
| Método | Path                | Parámetros          | Descripción                                   |
|--------|---------------------|---------------------|-----------------------------------------------|
| GET    | /api/ciudades       | `provinciaId` opc.  | Lista todas o filtra por provincia            |
| GET    | /api/ciudades/{id}  |                     | Obtiene por ID                                |
| POST   | /api/ciudades       | Body Ciudad         | Crea con validaciones (nombre, CP, provincia) |
| PUT    | /api/ciudades/{id}  | Body Ciudad         | Actualiza con controles de unicidad           |
| DELETE | /api/ciudades/{id}  |                     | Elimina si existe                             |

### Manejo de Errores
- Cada controller devuelve `ErrorResponse` (clase interna simple con `mensaje`) para errores de validación o existencia (404, 400, 409, 500).
- Se podría mejorar introduciendo un `@ControllerAdvice` global para respuestas uniformes.

## 9. Configuración Adicional (config/)
### OpenApiConfig
Define metadata de la API (título, descripción, contacto) y lista dos servidores:
- Directo: `http://localhost:8081`
- A través del Gateway: `http://localhost:8080/api`
Facilita probar rutas equivalentes según punto de entrada.

### TimezoneConfig
Establece timezone global en UTC al iniciar (buena práctica para consistencia temporal entre servicios y base de datos).

### DataInitializer (perfil `dev`)
Inserta datos de prueba (provincias, ciudades, clientes, contenedores) solo si la tabla de clientes está vacía para evitar duplicados en reinicios.
Beneficios:
- Bootstrap de entorno local sin depender de scripts SQL externos.
- Mejora velocidad de desarrollo y demo.

## 10. Integración con Otros Microservicios
- `ms-operaciones` puede invocar endpoints de clientes y contenedores para validar datos antes de crear solicitudes logísticas.
- La asociación `Cliente.keycloakId` permite enlazar identidad OIDC con la entidad de negocio (filtrar recursos según usuario logueado en futuro desarrollo).
- Los contenedores proporcionan peso/volumen necesarios para cálculos de rutas y tarifas (en conjunto con datos de flota y distancia externa).

## 11. Patrones y Buenas Prácticas Presentes
- Separación clara Controller/Service/Repository.
- Validaciones de dominio en Services (no solo anotaciones JPA/Bean Validation) para reglas más expresivas.
- Uso de builders Lombok para claridad en `DataInitializer`.
- Normalización de datos (capitalización, trim, mayúsculas) antes de persistir.

## 12. Oportunidades de Mejora
1. Centralizar manejo de errores con `@ControllerAdvice` + estándar (código, timestamp, detalle, path).
2. Añadir DTOs separados de entidades para evitar exposición directa del modelo persistente.
3. Validar uniqueness y reglas con anotaciones Bean Validation personalizadas (por ejemplo, `@UniqueCodigoContenedor`).
4. Incorporar paginación (`Pageable`) en listados grandes (clientes, contenedores, ciudades).
5. Añadir filtros por criterios múltiples (ej: ciudad + apellido) vía Specifications o QueryDSL.
6. Vincular seguridad (roles) para restringir operaciones (solo ADMIN crea/elimina provincias o ciudades, etc.).
7. Cache de provincias/ciudades (son datos relativamente estáticos) con Caffeine o Redis.
8. Migrar a eventos (pub/sub) para notificar a `ms-operaciones` de cambios significativos (por ejemplo contenedor actualizado).

## 13. Ejemplo de Flujo de Creación de Contenedor
1. Frontend envía `POST /api/contenedores` con JSON incluyendo `cliente.id`.
2. Controller delega a `ContenedorService.save()`.
3. Service valida: código, peso, volumen, existencia de cliente, unicidad de código.
4. Normaliza código (uppercase) y persiste.
5. Respuesta `201 Created` con entidad creada.

## 14. Resumen para Presentación Oral
“ms-clientes encapsula toda la información de clientes, sus contenedores y localización territorial (provincias y ciudades). Expone una API REST con validaciones de negocio en servicios para garantizar integridad (unicidad, referencias existentes y formatos). Otros microservicios consultan estos endpoints para validar datos antes de procesar operaciones logísticas. Incluye documentación OpenAPI, inicialización de datos en desarrollo y prácticas de normalización de entradas.”

---
Última actualización generada automáticamente.
