# 🚚 Microservicio de Flota (ms-flota)

Este microservicio es el corazón logístico del sistema. Se encarga de administrar los recursos físicos (**Camiones**, **Depósitos**), los recursos humanos (**Transportistas**) y la lógica económica (**Tarifas** y **Parámetros**) necesaria para calcular los costos de los viajes.

## 🚀 Funcionalidades Principales

1.  **Gestión de Camiones (Inventario Móvil):**
    *   Registro de vehículos con sus capacidades específicas (peso y volumen máximo).
    *   Control de **disponibilidad** en tiempo real (Libre vs. Ocupado).
    *   Cálculo de consumo y costos base por kilómetro específicos por unidad.

2.  **Gestión de Transportistas:**
    *   Administración de los choferes o empresas de transporte.
    *   Vinculación con **Keycloak** para la autenticación y permisos.

3.  **Gestión de Depósitos (Nodos Logísticos):**
    *   Registro de puntos de almacenamiento intermedio.
    *   **Control de Stock:** Monitoreo de capacidad máxima vs. ocupación actual.
    *   Gestión de costos por estadía diaria.

4.  **Motor de Tarifas (Lógica de Negocio):**
    *   Configuración de **Rangos de Tarifas** basados en peso y volumen de la carga.
    *   Administración de **Parámetros Globales** (precio del combustible, cargo fijo por tramo).
    *   Provee la información necesaria a `ms-operaciones` para cotizar envíos.

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 3 (Web, Data JPA)
*   **Base de Datos:** PostgreSQL
*   **Documentación:** OpenAPI / Swagger UI
*   **Build Tool:** Maven
*   **Contenedorización:** Docker

## 📂 Estructura de Datos

Las entidades principales del dominio se encuentran en el paquete `models`:

*   **Camion:** El vehículo de transporte.
    *   *Atributos:* `dominio` (patente), `capacidadPeso`, `capacidadVolumen`, `disponibilidad`, `costoKmBase`.
*   **Transportista:** El conductor responsable.
    *   *Atributos:* `dni`, `nombre`, `keycloakId`.
*   **Deposito:** Ubicación física de almacenamiento.
    *   *Atributos:* `latitud`, `longitud`, `capacidadMaxima`, `cantidadOcupada`, `costoEstadiaDiaria`.
*   **TarifaRango:** Reglas de precios.

## 🔌 API Endpoints

El servicio expone una API RESTful en el puerto **8082**.

### 🚛 Camiones
Controlador: `CamionController`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/camiones` | Lista todos los camiones. Filtro opcional: `?transportistaId={id}`. |
| `GET` | `/api/camiones/libres` | Retorna la cantidad de camiones disponibles. |
| `GET` | `/api/camiones/disponibles` | Busca camiones aptos para una carga específica (`?pesoMaximo=X&volumenMaximo=Y`). |
| `PUT` | `/api/camiones/disponibilidad` | Cambia el estado de un camión (Libre/Ocupado). |

### 🏢 Depósitos
Controlador: `DepositoController`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/depositos` | Lista todos los depósitos con su ubicación y costos. |
| `POST` | `/api/depositos` | Crea un nuevo depósito. |
| `PATCH`| `/api/depositos/{id}/...` | Métodos para incrementar/decrementar ocupación (usados internamente). |

### 💰 Tarifas y Parámetros
Controladores: `ParametrosTarifaController` y `TarifaController` (implícito)

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/tarifas` | Obtiene la tabla de rangos de precios (peso/volumen). |
| `GET` | `/api/parametros-tarifa` | Obtiene valores globales (precio combustible, cargos fijos). |
| `PUT` | `/api/parametros-tarifa/{id}` | Actualiza el precio del combustible o cargos fijos. |

## 🔄 Integración con otros Microservicios

`ms-flota` actúa como un proveedor de recursos para el sistema:

1.  **Para `ms-operaciones` (Cotización):** Cuando se crea una solicitud, Operaciones consulta los endpoints de `/api/tarifas` y `/api/parametros-tarifa` para calcular cuánto costará el viaje.
2.  **Para `ms-operaciones` (Asignación):** Operaciones consulta `/api/camiones/disponibles` filtrando por el peso y volumen del contenedor del cliente para saber qué camiones pueden realizar el viaje.
3.  **Para `ms-operaciones` (Rutas):** Provee las coordenadas (lat/long) de los depósitos para que la API de Geografía calcule las rutas intermedias.

## ⚙️ Configuración y Ejecución

### Datos Iniciales (Seed Data)
El servicio cuenta con un `DataInitializer` que, en perfil `dev`, precarga:
*   Transportistas de prueba (Juan, Sofía).
*   Camiones con distintas capacidades.
*   Depósitos en Córdoba, Rosario y Buenos Aires.
*   Una tabla base de tarifas.

### Ejecución con Docker
El proyecto incluye un `Dockerfile`.

```bash
# Construir la imagen
docker build -t ms-flota .

# Ejecutar en el puerto 8082
docker run -p 8082:8082 --network tpi-network ms-flota
```

### Ejecución Local (Maven)
```bash
cd ms-flota
mvn spring-boot:run
```

## 🧪 Testing

Puedes probar los flujos de administración de flota utilizando el archivo `test/test_complete.rest`, específicamente las secciones bajo el encabezado **MS-FLOTA**.

Ejemplo de consulta de camiones disponibles:
```http
GET http://localhost:8080/api/camiones/disponibles?pesoMaximo=3000&volumenMaximo=4
Authorization: Bearer {TOKEN_ADMIN}
```
