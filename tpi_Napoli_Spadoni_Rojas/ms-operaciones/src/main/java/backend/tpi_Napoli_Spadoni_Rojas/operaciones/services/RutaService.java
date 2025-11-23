package backend.tpi_Napoli_Spadoni_Rojas.operaciones.services;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.RutaRepository;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.TramoRepository;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.SolicitudRepository;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.GeoApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.FlotaApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.DepositoDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ParametrosTarifaDTO;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final SolicitudRepository solicitudRepository;
    private final GeoApiClient geoapiClient;
    private final FlotaApiClient flotaApiClient;
    private final CambioEstadoSolicitudService cambioEstadoService;

    public RutaService(RutaRepository rutaRepository, TramoRepository tramoRepository,
            SolicitudRepository solicitudRepository, GeoApiClient geoapiClient, 
            FlotaApiClient flotaApiClient, CambioEstadoSolicitudService cambioEstadoService) {
        this.rutaRepository = rutaRepository;
        this.tramoRepository = tramoRepository;
        this.solicitudRepository = solicitudRepository;
        this.geoapiClient = geoapiClient;
        this.flotaApiClient = flotaApiClient;
        this.cambioEstadoService = cambioEstadoService;
    }

    @Transactional
    public Ruta recalcTotales(Long rutaId) {
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
        Double costo = tramoRepository.sumCostoByRuta(rutaId);
        Double km = tramoRepository.sumDistanciaByRuta(rutaId);
        ruta.setCostoTotal(costo);
        ruta.setDistanciaTotalKm(km);
        return rutaRepository.save(ruta);
    }

    public List<Ruta> findAll() {
        return rutaRepository.findAll();
    }

    public Optional<Ruta> findById(Long id) {
        return rutaRepository.findById(id);
    }

    public List<Ruta> findBySolicitud(Long solicitudId) {
        return rutaRepository.findBySolicitudId(solicitudId);
    }

    public Ruta save(Ruta ruta) {
        return rutaRepository.save(ruta);
    }

    public void delete(Long id) {
        rutaRepository.deleteById(id);
    }

    /**
     * Genera rutas tentativas con tramos estimados para una solicitud
     * Opción A: Ruta directa (origen → destino)
     * Opción B: Mejor ruta por depósito (origen → depósito → destino)
     */
    public List<RutaTentativaDTO> obtenerRutasEstimadas(Long solicitudId) {
        // Obtener solicitud
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        String origen = solicitud.getOrigen();
        String destino = solicitud.getDestino();

        List<RutaTentativaDTO> rutasTentativas = new ArrayList<>();

        // OPCIÓN A: Ruta Directa
        RutaTentativaDTO rutaDirecta = generarRutaDirecta(origen, destino);
        rutasTentativas.add(rutaDirecta);

        // OPCIÓN B: Mejor Ruta por Depósito
        RutaTentativaDTO rutaPorDeposito = generarMejorRutaPorDeposito(origen, destino);
        if (rutaPorDeposito != null) {
            rutasTentativas.add(rutaPorDeposito);
        }

        return rutasTentativas;
    }

    /**
     * Genera ruta directa: origen → destino (un solo tramo)
     */
    private RutaTentativaDTO generarRutaDirecta(String origen, String destino) {
        // Calcular distancia con ms-geoapi
        double distanciaKm = geoapiClient.calcularDistanciaKm(origen, destino);

        // Estimar costo y tiempo
        CostoTiempoEstimado estimacion = estimarCostoYTiempo(distanciaKm);

        // Crear tramo estimado
        TramoEstimadoDTO tramo = TramoEstimadoDTO.builder()
                .origen(origen)
                .destino(destino)
                .distanciaKm(distanciaKm)
                .costoEstimado(estimacion.getCosto())
                .duracionMinutos(estimacion.getTiempoMinutos())
                .tipo("ORIGEN_DESTINO")
                .build();

        // Crear ruta tentativa
        return RutaTentativaDTO.builder()
                .nombre("Ruta Directa")
                .descripcion("Ruta directa sin paradas intermedias")
                .tramos(List.of(tramo))
                .distanciaTotalKm(distanciaKm)
                .costoTotalEstimado(estimacion.getCosto())
                .tiempoTotalMinutos(estimacion.getTiempoMinutos())
                .build();
    }

    /**
     * Genera la mejor ruta pasando por un depósito
     * Evalúa todos los depósitos y elige el de menor distancia total
     */
    private RutaTentativaDTO generarMejorRutaPorDeposito(String origen, String destino) {
        // Obtener todos los depósitos de ms-flota
        List<DepositoDTO> depositos = flotaApiClient.getDepositos();

        if (depositos == null || depositos.isEmpty()) {
            System.out.println("⚠️ No hay depósitos disponibles para generar ruta alternativa");
            return null;
        }

        DepositoDTO mejorDeposito = null;
        double menorDistanciaTotal = Double.MAX_VALUE;
        double distOrigenDeposito = 0;
        double distDepositoDestino = 0;

        // Evaluar cada depósito
        for (DepositoDTO deposito : depositos) {
            String ubicacionDeposito = obtenerUbicacionDeposito(deposito);

            // Calcular distancias
            double d1 = geoapiClient.calcularDistanciaKm(origen, ubicacionDeposito);
            double d2 = geoapiClient.calcularDistanciaKm(ubicacionDeposito, destino);
            double distTotal = d1 + d2;

            // Comparar con el mejor hasta ahora
            if (distTotal < menorDistanciaTotal) {
                menorDistanciaTotal = distTotal;
                mejorDeposito = deposito;
                distOrigenDeposito = d1;
                distDepositoDestino = d2;
            }
        }

        if (mejorDeposito == null) {
            return null;
        }

        String ubicacionDeposito = obtenerUbicacionDeposito(mejorDeposito);

        // Crear tramo 1: origen → depósito
        CostoTiempoEstimado est1 = estimarCostoYTiempo(distOrigenDeposito);
        TramoEstimadoDTO tramo1 = TramoEstimadoDTO.builder()
                .origen(origen)
                .destino(ubicacionDeposito)
                .distanciaKm(distOrigenDeposito)
                .costoEstimado(est1.getCosto())
                .duracionMinutos(est1.getTiempoMinutos())
                .depositoDestinoId(mejorDeposito.getId())
                .tipo("ORIGEN_DEPOSITO")
                .build();

        // Crear tramo 2: depósito → destino
        CostoTiempoEstimado est2 = estimarCostoYTiempo(distDepositoDestino);
        TramoEstimadoDTO tramo2 = TramoEstimadoDTO.builder()
                .origen(ubicacionDeposito)
                .destino(destino)
                .distanciaKm(distDepositoDestino)
                .costoEstimado(est2.getCosto())
                .duracionMinutos(est2.getTiempoMinutos())
                .depositoOrigenId(mejorDeposito.getId())
                .tipo("DEPOSITO_DESTINO")
                .build();

        // Crear ruta tentativa
        return RutaTentativaDTO.builder()
                .nombre("Ruta por " + mejorDeposito.getNombre())
                .descripcion("Ruta con parada en depósito: " + mejorDeposito.getNombre())
                .tramos(List.of(tramo1, tramo2))
                .distanciaTotalKm(menorDistanciaTotal)
                .costoTotalEstimado(est1.getCosto() + est2.getCosto())
                .tiempoTotalMinutos(est1.getTiempoMinutos() + est2.getTiempoMinutos())
                .build();
    }

    /**
     * Obtiene la ubicación del depósito (usa coordenadas si está disponible, sino
     * dirección)
     */
    private String obtenerUbicacionDeposito(DepositoDTO deposito) {
        if (deposito.getCoordenadas() != null && !deposito.getCoordenadas().isEmpty()) {
            return deposito.getCoordenadas();
        }
        if (deposito.getDireccion() != null && !deposito.getDireccion().isEmpty()) {
            return deposito.getDireccion();
        }
        return deposito.getCiudad() != null ? deposito.getCiudad() : deposito.getNombre();
    }

    /**
     * Estima costo y tiempo para una distancia dada
     * Usa parámetros y tarifas de ms-flota
     */
    private CostoTiempoEstimado estimarCostoYTiempo(double distanciaKm) {
        // Obtener parámetros de flota
        ParametrosTarifaDTO parametros = flotaApiClient.getParametros();

        double costoBaseKm = 100.0; // Valor por defecto
        double velocidadPromedio = 40.0; // km/h por defecto

        if (parametros != null) {
            if (parametros.getCostoBase() != null) {
                costoBaseKm = parametros.getCostoBase();
            }
            if (parametros.getVelocidadPromedio() != null) {
                velocidadPromedio = parametros.getVelocidadPromedio();
            }
        }

        // Calcular costo (promedio simple, sin considerar volumen específico)
        double costo = distanciaKm * costoBaseKm;

        // Calcular tiempo en minutos
        int tiempoMinutos = (int) Math.ceil((distanciaKm / velocidadPromedio) * 60);

        return new CostoTiempoEstimado(costo, tiempoMinutos);
    }

    /**
     * Clase interna para encapsular costo y tiempo estimados
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CostoTiempoEstimado {
        private double costo;
        private int tiempoMinutos;
    }

    /**
     * Guarda la ruta seleccionada por el usuario y crea los tramos correspondientes
     * Los tramos se crean con estado=ESTIMADO
     * Actualiza los totales en Solicitud
     */
    @Transactional
    public Long guardarRutaSeleccionada(Long solicitudId, SeleccionRutaDTO seleccionRuta) {
        // Obtener la solicitud
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        // Crear la entidad Ruta
        Ruta ruta = Ruta.builder()
                .solicitud(solicitud)
                .fechaInicio(java.time.LocalDateTime.now())
                .fechaFinEstimada(java.time.LocalDateTime.now().plusMinutes(seleccionRuta.getTiempoTotalMinutos()))
                .distanciaTotalKm(seleccionRuta.getDistanciaTotalKm())
                .costoTotal(seleccionRuta.getCostoTotalEstimado())
                .build();

        // Guardar la ruta primero para obtener el ID
        Ruta rutaGuardada = rutaRepository.save(ruta);

        // Crear los tramos a partir del DTO
        List<Tramo> tramos = new ArrayList<>();
        int orden = 1;

        for (TramoEstimadoDTO tramoDTO : seleccionRuta.getTramos()) {
            Tramo tramo = Tramo.builder()
                    .ruta(rutaGuardada)
                    .origen(tramoDTO.getOrigen())
                    .destino(tramoDTO.getDestino())
                    .distanciaKm(tramoDTO.getDistanciaKm())
                    .costo(tramoDTO.getCostoEstimado())
                    .camionId(1L) // Por ahora asignar un camión por defecto, luego se puede asignar
                                  // específicamente
                    .depositoOrigenId(tramoDTO.getDepositoOrigenId())
                    .depositoDestinoId(tramoDTO.getDepositoDestinoId())
                    .fechaInicio(java.time.LocalDateTime.now().plusMinutes((orden - 1) * tramoDTO.getDuracionMinutos()))
                    .fechaFinEstimada(java.time.LocalDateTime.now().plusMinutes(orden * tramoDTO.getDuracionMinutos()))
                    .estado(EstadoTramo.ESTIMADO)
                    .tipo(TipoTramo.valueOf(tramoDTO.getTipo()))
                    .orden(orden)
                    .build();

            tramos.add(tramo);
            orden++;
        }

        // Guardar los tramos
        tramoRepository.saveAll(tramos);

        // Actualizar los totales en la Solicitud
        solicitud.setCostoEstimado(seleccionRuta.getCostoTotalEstimado());
        solicitud.setTiempoEstimadoMin(seleccionRuta.getTiempoTotalMinutos());
        solicitudRepository.save(solicitud);

        return rutaGuardada.getId();
    }

    /**
     * Asigna una ruta (directa o indirecta) a una solicitud en estado BORRADOR
     * - tipo "directa": crea ruta con un solo tramo ORIGEN_DESTINO
     * - tipo "indirecta": crea ruta pasando por el depósito más cercano al destino
     * - Cambia el estado de BORRADOR a PROGRAMADA
     * - Registra el cambio en el historial de estados
     */
    @Transactional
    public Ruta asignarRuta(Long solicitudId, String tipo) {
        // Validar tipo de ruta
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de ruta es obligatorio");
        }
        
        tipo = tipo.toLowerCase().trim();
        if (!tipo.equals("directa") && !tipo.equals("indirecta")) {
            throw new IllegalArgumentException(
                "El tipo de ruta debe ser 'directa' o 'indirecta'. Tipo recibido: " + tipo);
        }

        // Obtener la solicitud
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró una solicitud con el ID: " + solicitudId));

        // Validar que la solicitud esté en estado BORRADOR
        if (solicitud.getEstadoActual() != Estado.BORRADOR) {
            throw new IllegalArgumentException(
                "La solicitud debe estar en estado BORRADOR para asignar una ruta. Estado actual: " 
                + solicitud.getEstadoActual());
        }

        // Validar que la solicitud no tenga rutas ya asignadas
        List<Ruta> rutasExistentes = rutaRepository.findBySolicitudId(solicitudId);
        if (!rutasExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                "La solicitud ya tiene una ruta asignada. ID de ruta existente: " + rutasExistentes.get(0).getId());
        }

        String origen = solicitud.getOrigen();
        String destino = solicitud.getDestino();

        // Validar que origen y destino estén definidos
        if (origen == null || origen.trim().isEmpty()) {
            throw new IllegalArgumentException("La solicitud debe tener un origen definido");
        }
        if (destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("La solicitud debe tener un destino definido");
        }

        RutaTentativaDTO rutaTentativa;

        // Generar la ruta según el tipo
        if (tipo.equals("directa")) {
            rutaTentativa = generarRutaDirecta(origen, destino);
        } else {
            // tipo = "indirecta"
            rutaTentativa = generarMejorRutaPorDeposito(origen, destino);
            if (rutaTentativa == null) {
                throw new IllegalArgumentException(
                    "No se pudo generar una ruta indirecta. No hay depósitos disponibles.");
            }
        }

        // Crear la entidad Ruta
        Ruta ruta = Ruta.builder()
                .solicitud(solicitud)
                .fechaInicio(java.time.LocalDateTime.now())
                .fechaFinEstimada(java.time.LocalDateTime.now().plusMinutes(rutaTentativa.getTiempoTotalMinutos()))
                .distanciaTotalKm(rutaTentativa.getDistanciaTotalKm())
                .costoTotal(rutaTentativa.getCostoTotalEstimado())
                .build();

        // Guardar la ruta
        Ruta rutaGuardada = rutaRepository.save(ruta);

        // Crear los tramos
        List<Tramo> tramos = new ArrayList<>();
        int orden = 1;

        for (TramoEstimadoDTO tramoDTO : rutaTentativa.getTramos()) {
            Tramo tramo = Tramo.builder()
                    .ruta(rutaGuardada)
                    .origen(tramoDTO.getOrigen())
                    .destino(tramoDTO.getDestino())
                    .distanciaKm(tramoDTO.getDistanciaKm())
                    .costo(tramoDTO.getCostoEstimado())
                    .camionId(1L) // Camión por defecto, se asignará específicamente después
                    .depositoOrigenId(tramoDTO.getDepositoOrigenId())
                    .depositoDestinoId(tramoDTO.getDepositoDestinoId())
                    .fechaInicio(java.time.LocalDateTime.now().plusMinutes((orden - 1) * tramoDTO.getDuracionMinutos()))
                    .fechaFinEstimada(java.time.LocalDateTime.now().plusMinutes(orden * tramoDTO.getDuracionMinutos()))
                    .estado(EstadoTramo.ESTIMADO)
                    .tipo(TipoTramo.valueOf(tramoDTO.getTipo()))
                    .orden(orden)
                    .build();

            tramos.add(tramo);
            orden++;
        }

        // Guardar los tramos
        tramoRepository.saveAll(tramos);

        // Actualizar totales en la solicitud
        solicitud.setCostoEstimado(rutaTentativa.getCostoTotalEstimado());
        solicitud.setTiempoEstimadoMin(rutaTentativa.getTiempoTotalMinutos());
        
        // Cambiar estado de BORRADOR a PROGRAMADA
        Estado estadoAnterior = solicitud.getEstadoActual();
        solicitud.setEstadoActual(Estado.PROGRAMADA);
        solicitudRepository.save(solicitud);

        // Registrar el cambio de estado en el historial
        String tipoRutaDescripcion = tipo.equals("directa") ? "directa" : "indirecta (con parada en depósito)";
        cambioEstadoService.registrarCambio(
            solicitud, 
            estadoAnterior, 
            Estado.PROGRAMADA, 
            "Ruta " + tipoRutaDescripcion + " asignada. " + rutaTentativa.getNombre() + ". " +
            "Distancia: " + String.format("%.2f", rutaTentativa.getDistanciaTotalKm()) + " km. " +
            "Costo estimado: $" + String.format("%.2f", rutaTentativa.getCostoTotalEstimado())
        );

        return rutaGuardada;
    }
}
