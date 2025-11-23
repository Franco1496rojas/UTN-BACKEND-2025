package backend.tpi_Napoli_Spadoni_Rojas.operaciones.services;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.RutaRepository;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.SolicitudRepository;
import jakarta.transaction.Transactional;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.GeoApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.ClientesApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.FlotaApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ClienteDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ContenedorDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ParametrosTarifaDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.TarifaRangoDTO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    private final SolicitudRepository repository;
    private final CambioEstadoSolicitudService cambioEstadoService;
    private final GeoApiClient geoapiClient;
    private final RutaRepository rutaRepository;
    private final ClientesApiClient clientesApiClient;
    private final FlotaApiClient flotaApiClient;

    public SolicitudService(SolicitudRepository repository,
            CambioEstadoSolicitudService cambioEstadoService,
            GeoApiClient geoapiClient,
            RutaRepository rutaRepository,
            ClientesApiClient clientesApiClient,
            FlotaApiClient flotaApiClient) {
        this.repository = repository;
        this.cambioEstadoService = cambioEstadoService;
        this.geoapiClient = geoapiClient;
        this.rutaRepository = rutaRepository;
        this.clientesApiClient = clientesApiClient;
        this.flotaApiClient = flotaApiClient;
    }

    @Transactional
    public Solicitud recalcTotales(Long solicitudId) {
        Solicitud s = repository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Double costo = rutaRepository.sumCostoBySolicitud(solicitudId);
        Double km = rutaRepository.sumDistanciaBySolicitud(solicitudId);

        s.setCostoReal(costo); // Cambio: costoTotal → costoReal (costo real después de asignar rutas)
        s.setDistanciaKm(km);
        return repository.save(s);
    }

    public List<Solicitud> findAll() {
        return repository.findAll();
    }

    public Optional<Solicitud> findById(Long id) {
        return repository.findById(id);
    }

    public List<Solicitud> findByCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    public List<Solicitud> findByEstado(Estado estado) {
        return repository.findByEstadoActual(estado);
    }

    public List<Solicitud> findByClienteAndEstado(Long clienteId, Estado estado) {
        return repository.findByClienteIdAndEstadoActual(clienteId, estado);
    }

    public Solicitud save(Solicitud solicitud) {
        if (solicitud.getFechaSolicitud() == null)
            solicitud.setFechaSolicitud(java.time.LocalDateTime.now());
        if (solicitud.getEstadoActual() == null)
            solicitud.setEstadoActual(Estado.BORRADOR);

        // Si no informan distancia, la pedimos a geoapi (origen/destino de la
        // solicitud)
        if (solicitud.getDistanciaKm() == null || solicitud.getDistanciaKm() <= 0) {
            double km = geoapiClient.calcularDistanciaKm(solicitud.getOrigen(), solicitud.getDestino());
            solicitud.setDistanciaKm(km);
        }

        // Calcular estimaciones (tiempo y costo) en base a la lógica de registrarSolicitud
        // 1) Tiempo estimado: distancia / 70 km/h
        if (solicitud.getDistanciaKm() != null && solicitud.getDistanciaKm() > 0) {
            int tiempoEstimadoMin = (int) Math.ceil((solicitud.getDistanciaKm() / 70.0) * 60);
            solicitud.setTiempoEstimadoMin(tiempoEstimadoMin);
        }

        // 2) Costo estimado: requiere peso/volumen del contenedor
        if (solicitud.getContenedorId() == null) {
            throw new IllegalArgumentException("El ID del contenedor es obligatorio para calcular el costo estimado");
        }

        ContenedorDTO cont = clientesApiClient.getContenedor(solicitud.getContenedorId());
        if (cont == null) {
            throw new IllegalArgumentException("No se pudo obtener información del contenedor con ID: " + solicitud.getContenedorId());
        }
        Double peso = cont.getPeso();
        Double volumen = cont.getVolumen();
        if (peso == null || peso <= 0) {
            throw new IllegalArgumentException("El peso del contenedor no es válido: " + peso);
        }
        if (volumen == null || volumen <= 0) {
            throw new IllegalArgumentException("El volumen del contenedor no es válido: " + volumen);
        }

        if (solicitud.getDistanciaKm() == null || solicitud.getDistanciaKm() <= 0) {
            throw new IllegalArgumentException("No se pudo calcular la distancia entre origen y destino");
        }

        double costoEstimado = calcularCostoEstimado(solicitud.getDistanciaKm(), peso, volumen);
        solicitud.setCostoEstimado(costoEstimado);

        Solicitud nueva = repository.save(solicitud);
        cambioEstadoService.registrarCambio(nueva, null, nueva.getEstadoActual(), "Solicitud creada");
        return nueva;
    }

    public Solicitud actualizarEstado(Long id, Estado nuevoEstado, String observaciones) {
        Solicitud solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Estado anterior = solicitud.getEstadoActual();
        solicitud.setEstadoActual(nuevoEstado);
        repository.save(solicitud);

        cambioEstadoService.registrarCambio(solicitud, anterior, nuevoEstado, observaciones);
        return solicitud;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Registrar una nueva solicitud con el flujo completo:
     * 1. Validar datos básicos de entrada
     * 2. Buscar/crear cliente en ms-clientes
     * 3. Crear nuevo contenedor (siempre nuevo)
     * 4. Calcular distancia estimada con geoapi
     * 5. Calcular tiempo estimado (distancia / 70 km/h)
     * 6. Calcular costo estimado basado en consumo promedio de camiones aptos
     * 7. Crear solicitud con estado BORRADOR
     */
    @Transactional
    public Solicitud registrarSolicitud(RegistrarSolicitudDTO dto) {
        // 1. Validar datos básicos
        validarDatosEntrada(dto);

        // 2. Buscar o crear cliente
        ClienteDTO cliente = buscarOCrearCliente(dto);

        // 3. Validar que el código del contenedor no exista (si se proporciona)
        if (dto.getContenedorCodigo() != null && !dto.getContenedorCodigo().trim().isEmpty()) {
            ContenedorDTO contenedorExistente = clientesApiClient.buscarContenedorPorCodigo(dto.getContenedorCodigo().trim());
            if (contenedorExistente != null) {
                throw new IllegalArgumentException("Ya existe un contenedor con el código: " + dto.getContenedorCodigo().trim());
            }
        }

        // 4. Crear nuevo contenedor (siempre nuevo según requerimiento)
        ContenedorDTO contenedor = crearContenedor(dto, cliente.getId());

        // 5. Calcular distancia con geoapi (no bloquear si falla)
        double distanciaCalculada = geoapiClient.calcularDistanciaKm(dto.getOrigen(), dto.getDestino());
        Double distanciaKm = distanciaCalculada > 0 ? distanciaCalculada : null;

        // 6. Calcular tiempo estimado (si hay distancia) usando 70 km/h
        Integer tiempoEstimadoMin = null;
        if (distanciaKm != null) {
            tiempoEstimadoMin = (int) Math.ceil((distanciaKm / 70.0) * 60);
        }

        // 7. Calcular costo estimado (mejor esfuerzo, no bloquear por fallos externos)
        Double costoEstimado = null;
        if (distanciaKm != null) {
            try {
                costoEstimado = calcularCostoEstimado(distanciaKm, contenedor.getPeso(), contenedor.getVolumen());
            } catch (Exception ex) {
                // Dejar costo estimado en null si faltan parámetros, tarifas o camiones
                System.err.println("⚠️ No se pudo calcular costo estimado al registrar solicitud: " + ex.getMessage());
                costoEstimado = null;
            }
        }

        // 8. Crear solicitud con estado BORRADOR
        Solicitud solicitud = Solicitud.builder()
                .clienteId(cliente.getId())
                .contenedorId(contenedor.getId())
                .origen(dto.getOrigen())
                .destino(dto.getDestino())
                .distanciaKm(distanciaKm)
                .costoEstimado(costoEstimado)
                .tiempoEstimadoMin(tiempoEstimadoMin)
                .costoReal(null) // Cambio: se calculará cuando se finalicen todas las rutas
                .estadoActual(Estado.BORRADOR)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        Solicitud nuevaSolicitud = repository.save(solicitud);

        // 9. Registrar en historial con estado BORRADOR
        cambioEstadoService.registrarCambio(
                nuevaSolicitud,
                null,
                Estado.BORRADOR,
                "Solicitud creada en estado borrador. " + (dto.getObservaciones() != null ? dto.getObservaciones() : ""));

        return nuevaSolicitud;
    }

    /**
     * Validar datos básicos de entrada
     */
    private void validarDatosEntrada(RegistrarSolicitudDTO dto) {
        // Validar origen y destino
        if (dto.getOrigen() == null || dto.getOrigen().trim().isEmpty()) {
            throw new IllegalArgumentException("El origen es obligatorio");
        }
        if (dto.getDestino() == null || dto.getDestino().trim().isEmpty()) {
            throw new IllegalArgumentException("El destino es obligatorio");
        }
        if (dto.getOrigen().trim().equalsIgnoreCase(dto.getDestino().trim())) {
            throw new IllegalArgumentException("El origen y destino no pueden ser iguales");
        }

        // Validar datos del contenedor
        if (dto.getContenedorPeso() == null || dto.getContenedorPeso() <= 0) {
            throw new IllegalArgumentException("El peso del contenedor debe ser mayor a cero");
        }
        if (dto.getContenedorVolumen() == null || dto.getContenedorVolumen() <= 0) {
            throw new IllegalArgumentException("El volumen del contenedor debe ser mayor a cero");
        }

        // Validación flexible de cliente: permitir crear con solo nombre si no hay DNI/Email
        boolean tieneIdentificacion = (dto.getClienteDni() != null && !dto.getClienteDni().trim().isEmpty()) ||
                                      (dto.getClienteEmail() != null && !dto.getClienteEmail().trim().isEmpty());
        boolean tieneNombre = dto.getClienteNombre() != null && !dto.getClienteNombre().trim().isEmpty();
        if (!tieneIdentificacion && !tieneNombre) {
            throw new IllegalArgumentException("Debe proporcionar DNI, email o nombre del cliente");
        }
    }

    /**
     * Buscar cliente por DNI o email, si no existe lo crea
     */
    private ClienteDTO buscarOCrearCliente(RegistrarSolicitudDTO dto) {
        ClienteDTO cliente = null;

        // Buscar por DNI
        if (dto.getClienteDni() != null && !dto.getClienteDni().trim().isEmpty()) {
            cliente = clientesApiClient.buscarClientePorDni(dto.getClienteDni().trim());
        }

        // Si no se encontró por DNI, buscar por email
        if (cliente == null && dto.getClienteEmail() != null && !dto.getClienteEmail().trim().isEmpty()) {
            cliente = clientesApiClient.buscarClientePorEmail(dto.getClienteEmail().trim());
        }

        // Si existe el cliente, retornarlo
        if (cliente != null) {
            return cliente;
        }

        // Si no existe, crear nuevo cliente
        // Validar que tenga nombre si se va a crear
        if (dto.getClienteNombre() == null || dto.getClienteNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio para crear un nuevo cliente");
        }

        ClienteDTO nuevoCliente = ClienteDTO.builder()
                .dni(dto.getClienteDni() != null ? dto.getClienteDni().trim() : null)
                .email(dto.getClienteEmail() != null ? dto.getClienteEmail().trim() : null)
                .nombre(dto.getClienteNombre().trim())
                .telefono(dto.getClienteTelefono() != null ? dto.getClienteTelefono().trim() : null)
                .build();

        try {
            cliente = clientesApiClient.crearCliente(nuevoCliente);
        } catch (Exception e) {
            // Extraer mensaje de error más específico
            String mensajeError = e.getMessage();
            if (mensajeError != null && mensajeError.contains("Ya existe")) {
                throw new IllegalArgumentException(mensajeError);
            }
            throw new IllegalArgumentException("No se pudo crear el cliente: " + mensajeError);
        }

        if (cliente == null) {
            throw new IllegalArgumentException("No se pudo crear el cliente en el sistema");
        }

        return cliente;
    }

    /**
     * Crear nuevo contenedor (siempre se crea uno nuevo)
     */
    private ContenedorDTO crearContenedor(RegistrarSolicitudDTO dto, Long clienteId) {
        // Validar que el cliente existe
        if (clienteId == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        // Crear DTO con la estructura que ms-clientes espera
        // IMPORTANTE: ms-clientes solo acepta: codigo, peso, volumen, cliente
        // NO acepta el campo "tipo" ni "estado"
        String contenedorJson = String.format("""
            {
                "codigo": "%s",
                "peso": %s,
                "volumen": %s,
                "cliente": { "id": %d }
            }
            """,
            dto.getContenedorCodigo() != null ? dto.getContenedorCodigo().trim() : generarCodigoContenedor(),
            dto.getContenedorPeso(),
            dto.getContenedorVolumen(),
            clienteId
        );

        ContenedorDTO contenedor;
        try {
            // Usar el método que envía JSON directamente
            contenedor = clientesApiClient.crearContenedorConJson(contenedorJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo crear el contenedor: " + e.getMessage());
        }

        if (contenedor == null) {
            throw new IllegalArgumentException("No se pudo crear el contenedor en el sistema");
        }

        return contenedor;
    }

    /**
     * Generar código único para contenedor si no se proporciona
     */
    private String generarCodigoContenedor() {
        return "CONT-" + System.currentTimeMillis();
    }

    /**
     * Calcular costo estimado basado en:
     * 1. Promedio de consumo de combustible de camiones aptos
     * 2. Precio por litro de combustible
     * 3. Cargo fijo por tramo
     * 4. Costo base por km según tarifa (dimensiones del contenedor)
     */
    private double calcularCostoEstimado(double distanciaKm, Double peso, Double volumen) {
        // Obtener parámetros globales
        ParametrosTarifaDTO parametros = flotaApiClient.getParametros();
        if (parametros == null) {
            throw new IllegalArgumentException("No se pudieron obtener los parámetros de tarifa del sistema");
        }

        Double precioLitroCombustible = parametros.getPrecioLitroCombustible();
        Double cargoFijoTramo = parametros.getCargoFijoTramo();

        if (precioLitroCombustible == null || precioLitroCombustible <= 0) {
            throw new IllegalArgumentException("El precio por litro de combustible no está configurado correctamente");
        }
        if (cargoFijoTramo == null || cargoFijoTramo < 0) {
            throw new IllegalArgumentException("El cargo fijo por tramo no está configurado correctamente");
        }

        // Obtener camiones aptos para el contenedor
        List<backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.CamionDTO> camionesAptos = 
            flotaApiClient.getCamionesDisponibles(peso, volumen);

        if (camionesAptos == null || camionesAptos.isEmpty()) {
            throw new IllegalArgumentException("No hay camiones disponibles con la capacidad requerida para transportar este contenedor");
        }

        // Calcular promedio de consumo de combustible de los camiones aptos
        double promedioConsumoLitroKm = camionesAptos.stream()
                .filter(c -> c.getConsumoLitroKm() != null && c.getConsumoLitroKm() > 0)
                .mapToDouble(backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.CamionDTO::getConsumoLitroKm)
                .average()
                .orElse(0.0);

        if (promedioConsumoLitroKm <= 0) {
            throw new IllegalArgumentException("No se pudo calcular el consumo promedio de combustible de los camiones aptos");
        }

        // Calcular costo de combustible: (distancia * promedioConsumo * precioLitro)
        double costoCombustible = distanciaKm * promedioConsumoLitroKm * precioLitroCombustible;

        // Obtener costo base por km según dimensiones del contenedor (tarifas)
        List<TarifaRangoDTO> tarifas = flotaApiClient.getTarifas();
        double costoKmBase = 0.0;

        if (tarifas != null && !tarifas.isEmpty()) {
            for (TarifaRangoDTO tarifa : tarifas) {
                // Validar que los campos de la tarifa no sean nulos
                if (tarifa.getVolumenMin() == null || tarifa.getVolumenMax() == null) {
                    continue; // Saltar tarifas con datos incompletos
                }
                
                boolean cumpleVolumen = volumen >= tarifa.getVolumenMin() && volumen <= tarifa.getVolumenMax();
                boolean cumplePeso = true; // Por defecto cumple si no hay restricción de peso
                
                // Validar peso solo si los campos están definidos
                if (tarifa.getPesoMin() != null && tarifa.getPesoMax() != null) {
                    cumplePeso = peso >= tarifa.getPesoMin() && peso <= tarifa.getPesoMax();
                } else if (tarifa.getPesoMin() != null) {
                    cumplePeso = peso >= tarifa.getPesoMin();
                } else if (tarifa.getPesoMax() != null) {
                    cumplePeso = peso <= tarifa.getPesoMax();
                }
                
                if (cumpleVolumen && cumplePeso) {
                    costoKmBase = tarifa.getCostoKmBase() != null ? tarifa.getCostoKmBase() : 0.0;
                    break;
                }
            }
        }

        // Costo total estimado = costoCombustible + cargoFijo + (costoKmBase * distancia)
        double costoEstimado = costoCombustible + cargoFijoTramo + (costoKmBase * distanciaKm);

        return Math.round(costoEstimado * 100.0) / 100.0; // Redondear a 2 decimales
    }
}
