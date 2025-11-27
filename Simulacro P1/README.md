# 📦 Microservicio de Clientes (ms-clientes)

Este microservicio es el encargado de la gestión de la información de los **Clientes**, sus **Contenedores** asociados y los datos maestros de ubicación (**Ciudades** y **Provincias**). Actúa como la fuente de verdad para la identificación de usuarios y el inventario de carga dentro del ecosistema de transporte.

## 🚀 Funcionalidades Principales

1.  **Gestión de Clientes (CRUD):**
    *   Registro, actualización y consulta de clientes.
    *   Vinculación con **Keycloak** a través del campo `keycloakId` para la seguridad.
    *   Búsqueda de clientes por apellido o DNI.

2.  **Gestión de Contenedores:**
    *   Registro de contenedores físicos asignados a un cliente específico.
    *   Validación de unicidad del código del contenedor.
    *   Almacenamiento de características físicas (peso, volumen).

3.  **Datos Geográficos (Maestros):**
    *   Administración de **Provincias** y **Ciudades**.
    *   Estos datos son consumidos por otros servicios para validar orígenes y destinos.

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 3 (Web, Data JPA, Validation)
*   **Base de Datos:** PostgreSQL
*   **Documentación:** OpenAPI / Swagger UI
*   **Build Tool:** Maven
*   **Contenedorización:** Docker

## 📂 Estructura de Datos

Las entidades principales del dominio se encuentran en el paquete `models`:

*   **Cliente:** Representa a la persona o empresa que solicita el transporte.
    *   *Atributos clave:* `dni`, `nombre`, `apellido`, `email`, `keycloakId`, `ciudad`.
*   **Contenedor:** Representa la carga física.
    *   *Atributos clave:* `codigo` (único), `peso`, `volumen`, `cliente` (Relación ManyToOne).
*   **Ciudad / Provincia:** Normalización de direcciones.

## 🔌 API Endpoints

El servicio expone una API RESTful en el puerto **8081**. A continuación, los endpoints más relevantes gestionados por los controladores:

### 👤 Clientes
Controlador: `ClienteController`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/clientes` | Lista todos los clientes. Soporta filtro `?apellido=...`. |
| `GET` | `/api/clientes/{id}` | Obtiene el detalle de un cliente específico. |
| `POST` | `/api/clientes` | Crea un nuevo cliente. Requiere `ciudad.id`. |
| `PUT` | `/api/clientes/{id}` | Actualiza datos de contacto o dirección. |
| `DELETE`| `/api/clientes/{id}` | Elimina un cliente (solo si no tiene dependencias activas). |

### 📦 Contenedores
Controlador: `ContenedorController`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/contenedores` | Lista contenedores. Filtro común: `?clienteId={id}`. |
| `POST` | `/api/contenedores` | Registra un nuevo contenedor para un cliente. |
| `GET` | `/api/contenedores/{id}`| Obtiene detalles (peso, volumen) de un contenedor. |

### 🌍 Ubicación
*   `/api/provincias`: Gestión de provincias.
*   `/api/ciudades`: Gestión de ciudades.

## 🔄 Integración con otros Microservicios

Este servicio es fundamental para el flujo de **ms-operaciones**:

1.  **Validación de Solicitudes:** Cuando `ms-operaciones` recibe una nueva solicitud de transporte, consulta a `ms-clientes` para verificar que el cliente exista y que el contenedor le pertenezca.
2.  **Creación Automática:** Si se registra una solicitud con un cliente nuevo, `ms-operaciones` llama a este servicio para darlo de alta automáticamente.
3.  **Datos para Reportes:** Provee la información personal (nombre, dirección) para los reportes de seguimiento y hojas de ruta.

## ⚙️ Configuración y Ejecución

### Variables de Entorno Requeridas
El servicio espera una base de datos PostgreSQL. Las credenciales suelen configurarse en `application.properties` o via variables de entorno en Docker:

```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/tpi_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Ejecución con Docker
El proyecto incluye un `Dockerfile` optimizado con Eclipse Temurin 21.

```bash
# Construir la imagen
docker build -t ms-clientes .

# Ejecutar (asegúrate de tener la red y la DB configuradas)
docker run -p 8081:8081 --network tpi-network ms-clientes
```

### Ejecución Local (Maven)
```bash
cd ms-clientes
mvn spring-boot:run
```

## 🧪 Testing

Puedes probar los endpoints utilizando el archivo `test/test.rest` incluido en el repositorio, el cual contiene ejemplos de peticiones HTTP para crear clientes, asignar contenedores y listar datos.
