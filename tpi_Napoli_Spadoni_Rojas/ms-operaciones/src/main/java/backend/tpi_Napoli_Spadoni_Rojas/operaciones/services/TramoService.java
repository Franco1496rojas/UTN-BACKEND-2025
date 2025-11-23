package backend.tpi_Napoli_Spadoni_Rojas.operaciones.services;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.ClientesApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.FlotaApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.GeoApiClient;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.CamionDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ContenedorDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.DepositoDTO;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Tramo;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.TramoRepository;
import jakarta.transaction.Transactional;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.EstadoTramo;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TramoService {

    private final TramoRepository repository;
    private final GeoApiClient geoapiClient;
    private final CalculoCostoService calculoCostoService;
    private final RutaService rutaService;
    private final SolicitudService solicitudService;
    private final FlotaApiClient flotaApiClient;
    private final ClientesApiClient clientesApiClient;

    public TramoService(TramoRepository repository, GeoApiClient geoapiClient,
            CalculoCostoService calculoCostoService, RutaService rutaService, SolicitudService solicitudService,
            FlotaApiClient flotaApiClient, ClientesApiClient clientesApiClient) {
        this.repository = repository;
        this.geoapiClient = geoapiClient;
        this.calculoCostoService = calculoCostoService;
        this.rutaService = rutaService;
        this.solicitudService = solicitudService;
        this.flotaApiClient = flotaApiClient;
        this.clientesApiClient = clientesApiClient;
    }

    public List<Tramo> findAll() {
        return repository.findAll();
    }

    public Optional<Tramo> findById(Long id) {
        return repository.findById(id);
    }

    public List<Tramo> findByRuta(Long rutaId) {
        return repository.findByRutaId(rutaId);
    }

    @Transactional
    public Tramo save(Tramo tramo) {
        if (tramo.getDistanciaKm() == null || tramo.getDistanciaKm() <= 0) {
            double km = geoapiClient.calcularDistanciaKm(tramo.getOrigen(), tramo.getDestino());
            tramo.setDistanciaKm(km);
        }

        Long contenedorId = tramo.getRuta() != null && tramo.getRuta().getSolicitud() != null
                ? tramo.getRuta().getSolicitud().getContenedorId()
                : null;

        Double costo = calculoCostoService.calcularCostoTramo(
                tramo.getCamionId(),
                contenedorId,
                tramo.getDistanciaKm());
        tramo.setCosto(costo);

        Tramo saved = repository.save(tramo);

        // Recalcular totales
        var ruta = rutaService.recalcTotales(saved.getRuta().getId());
        solicitudService.recalcTotales(ruta.getSolicitud().getId());

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Tramo t = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado"));
        Long rutaId = t.getRuta().getId();
        Long solicitudId = t.getRuta().getSolicitud().getId();

        repository.deleteById(id);

        rutaService.recalcTotales(rutaId);
        solicitudService.recalcTotales(solicitudId);
    }

    @Transactional
    public Tramo asignarCamion(Long tramoId, Long camionId) {
        // 1. Validar que el ID del camión no sea nulo
        if (camionId == null) {
            throw new IllegalArgumentException("El ID del camión es obligatorio");
        }

        // 2. Obtener el tramo
        Tramo tramo = repository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un tramo con el ID: " + tramoId));

        // 3. Validar que el tramo esté en estado ESTIMADO
        if (tramo.getEstado() != EstadoTramo.ESTIMADO) {
            throw new IllegalArgumentException(
                "El tramo debe estar en estado ESTIMADO para asignar un camión. Estado actual: " + tramo.getEstado());
        }

        // 4. Validar que el tramo no tenga ya un camión asignado
        if (tramo.getCamionId() != null && tramo.getEstado() == EstadoTramo.ASIGNADO) {
            throw new IllegalArgumentException(
                "El tramo ya tiene un camión asignado (ID: " + tramo.getCamionId() + ")");
        }

        // 5. Obtener información del contenedor para validar capacidad
        Long contenedorId = tramo.getRuta().getSolicitud().getContenedorId();
        ContenedorDTO contenedor = clientesApiClient.getContenedor(contenedorId);

        if (contenedor == null) {
            throw new RuntimeException("No se pudo obtener información del contenedor con ID: " + contenedorId);
        }

        Double pesoContenedor = contenedor.getPeso();
        Double volumenContenedor = contenedor.getVolumen();

        if (pesoContenedor == null || pesoContenedor <= 0) {
            throw new IllegalArgumentException("El peso del contenedor no es válido: " + pesoContenedor);
        }

        if (volumenContenedor == null || volumenContenedor <= 0) {
            throw new IllegalArgumentException("El volumen del contenedor no es válido: " + volumenContenedor);
        }

        // 6. Obtener información del camión
        CamionDTO camion = flotaApiClient.getCamion(camionId);

        if (camion == null) {
            throw new IllegalArgumentException("No se encontró un camión con el ID: " + camionId);
        }

        // 7. Validar que el camión esté disponible consultando la lista de camiones disponibles
        // con las capacidades requeridas (peso y volumen del contenedor)
        List<CamionDTO> camionesDisponibles = flotaApiClient.getCamionesDisponibles(pesoContenedor, volumenContenedor);
        
        boolean camionEstaDisponible = camionesDisponibles.stream()
                .anyMatch(c -> c.getId().equals(camionId));
        
        if (!camionEstaDisponible) {
            throw new IllegalArgumentException(
                "El camión (ID: " + camionId + ", Dominio: " + camion.getDominio() + ") no está disponible " +
                "o no cumple con los requisitos de capacidad (Peso: " + pesoContenedor + " kg, Volumen: " + volumenContenedor + " m³)");
        }

        // 8. Validar capacidad de peso (validación redundante, ya verificada con camiones disponibles)
        // Esta validación adicional protege contra errores de sincronización
        if (camion.getCapacidadPeso() == null || camion.getCapacidadPeso() < pesoContenedor) {
            throw new IllegalArgumentException(
                String.format(
                    "El camión no tiene capacidad de peso suficiente. " +
                    "Requerido: %.2f kg, Capacidad del camión: %.2f kg, Déficit: %.2f kg",
                    pesoContenedor, 
                    camion.getCapacidadPeso() != null ? camion.getCapacidadPeso() : 0.0,
                    pesoContenedor - (camion.getCapacidadPeso() != null ? camion.getCapacidadPeso() : 0.0)));
        }

        // 9. Validar capacidad de volumen (validación redundante, ya verificada con camiones disponibles)
        // Esta validación adicional protege contra errores de sincronización
        if (camion.getCapacidadVolumen() == null || camion.getCapacidadVolumen() < volumenContenedor) {
            throw new IllegalArgumentException(
                String.format(
                    "El camión no tiene capacidad de volumen suficiente. " +
                    "Requerido: %.2f m³, Capacidad del camión: %.2f m³, Déficit: %.2f m³",
                    volumenContenedor, 
                    camion.getCapacidadVolumen() != null ? camion.getCapacidadVolumen() : 0.0,
                    volumenContenedor - (camion.getCapacidadVolumen() != null ? camion.getCapacidadVolumen() : 0.0)));
        }

        // 10. Asignar camión al tramo
        tramo.setCamionId(camionId);
        tramo.setEstado(EstadoTramo.ASIGNADO);

        // 11. Derivar transportistaId si está disponible
        if (camion.getTransportista() != null && camion.getTransportista().getId() != null) {
            tramo.setTransportistaId(camion.getTransportista().getId());
            System.out.println("✅ Transportista asignado al tramo: ID " + camion.getTransportista().getId());
        }

        // 12. Guardar el tramo actualizado
        Tramo tramoGuardado = repository.save(tramo);

        // 13. Marcar el camión como no disponible en ms-flota
        try {
            flotaApiClient.actualizarDisponibilidadCamion(camionId, false);
            System.out.println("✅ Camión " + camionId + " marcado como no disponible");
        } catch (Exception e) {
            System.err.println("⚠️ Error al actualizar disponibilidad del camión: " + e.getMessage());
            // No lanzar excepción porque el tramo ya fue asignado
        }

        // 14. Verificar si todos los tramos de la ruta tienen camión asignado
        // Si es así, cambiar estado de la solicitud de PROGRAMADA a ASIGNADA
        List<Tramo> todosLosTramos = repository.findByRutaId(tramo.getRuta().getId());
        boolean todosAsignados = todosLosTramos.stream()
                .allMatch(t -> t.getEstado() == EstadoTramo.ASIGNADO || t.getEstado() == EstadoTramo.INICIADO || 
                              t.getEstado() == EstadoTramo.FINALIZADO);

        if (todosAsignados) {
            Long solicitudId = tramo.getRuta().getSolicitud().getId();
            try {
                solicitudService.actualizarEstado(
                    solicitudId,
                    backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Estado.ASIGNADA,
                    "Todos los tramos tienen camión asignado. Camión " + camion.getDominio() + 
                    " asignado al último tramo."
                );
                System.out.println("✅ Solicitud " + solicitudId + " cambió a estado ASIGNADA");
            } catch (Exception e) {
                System.err.println("⚠️ Error al actualizar estado de solicitud: " + e.getMessage());
            }
        }

        System.out.println("✅ Camión " + camionId + " (Dominio: " + camion.getDominio() + 
                          ") asignado al tramo " + tramoId + " con estado ASIGNADO");

        return tramoGuardado;
    }

    /**
     * INICIO DE TRAMO
     * - Set fechaHoraInicioReal, estado=INICIADO
     * - Si es el primer tramo de la ruta: cambiar Solicitud de ASIGNADA →
     * EN_TRANSITO
     */
    @Transactional
    public Tramo iniciarTramo(Long tramoId, String fechaHoraInicioReal) {
        Tramo tramo = repository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        // Validar que el tramo esté en estado ASIGNADO
        if (tramo.getEstado() != EstadoTramo.ASIGNADO) {
            throw new RuntimeException(
                    "El tramo debe estar en estado ASIGNADO para iniciarse. Estado actual: " + tramo.getEstado());
        }

        // Parsear la fecha del formato ISO (acepta con o sin offset/Z)
        LocalDateTime fechaInicio;
        try {
            fechaInicio = OffsetDateTime.parse(fechaHoraInicioReal).toLocalDateTime();
        } catch (Exception e) {
            fechaInicio = LocalDateTime.parse(fechaHoraInicioReal);
        }
        tramo.setFechaHoraInicioReal(fechaInicio);
        tramo.setEstado(EstadoTramo.INICIADO);

        Tramo tramoGuardado = repository.save(tramo);

        // Verificar si es el primer tramo de la ruta (orden = 1)
        if (tramo.getOrden() != null && tramo.getOrden() == 1) {
            // Cambiar estado de la Solicitud: ASIGNADA → EN_TRANSITO
            Long solicitudId = tramo.getRuta().getSolicitud().getId();
            solicitudService.actualizarEstado(solicitudId,
                    backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Estado.EN_TRANSITO,
                    "Inicio del primer tramo de transporte");

            System.out.println("✅ Solicitud " + solicitudId + " cambiada a estado EN_TRANSITO");
        }

        System.out.println("✅ Tramo " + tramoId + " iniciado con estado INICIADO");

        return tramoGuardado;
    }

    /**
     * FIN DE TRAMO
     * - Validar que esté en estado INICIADO
     * - Calcular automáticamente: duracionMinReal, distanciaKmReal, costoReal
     * - Si es el último tramo (llega a destino): actualizar Solicitud a COMPLETADA
     * - Calcular costos de estadía en depósitos
     * - Liberar camión
     */
    @Transactional
    public Tramo finalizarTramo(Long tramoId) {
        Tramo tramo = repository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        // =========================================
        // 1. VALIDAR ESTADO
        // =========================================
        if (tramo.getEstado() != EstadoTramo.INICIADO) {
            throw new RuntimeException(
                    "El tramo debe estar en estado INICIADO para finalizarse. Estado actual: " + tramo.getEstado());
        }

        // =========================================
        // 2. REGISTRAR FECHA FIN CON HORA ACTUAL
        // =========================================
        LocalDateTime fechaFin = LocalDateTime.now();
        tramo.setFechaHoraFinReal(fechaFin);

        // =========================================
        // 3. CALCULAR DURACIÓN REAL (minutos)
        // =========================================
        if (tramo.getFechaHoraInicioReal() == null) {
            throw new RuntimeException("El tramo no tiene fecha de inicio real registrada");
        }
        
        long minutosReales = java.time.Duration.between(
            tramo.getFechaHoraInicioReal(), 
            fechaFin
        ).toMinutes();
        
        tramo.setDuracionMinReal((int) minutosReales);
        
        System.out.println("⏱️ Duración real calculada: " + minutosReales + " minutos");

        // =========================================
        // 4. CALCULAR DISTANCIA REAL (km)
        // =========================================
        Double distanciaKmReal = geoapiClient.calcularDistanciaKm(
            tramo.getOrigen(), 
            tramo.getDestino()
        );
        tramo.setDistanciaKmReal(distanciaKmReal);
        
        System.out.println("📏 Distancia real calculada: " + distanciaKmReal + " km");

        // =========================================
        // 5. CALCULAR COSTO REAL DEL TRANSPORTE
        // =========================================
        Double costoRealTransporte = calcularCostoRealTransporte(tramo, distanciaKmReal);
        tramo.setCostoReal(costoRealTransporte);
        
        System.out.println("💰 Costo real del transporte: $" + costoRealTransporte);

        // =========================================
        // 6. CALCULAR ESTADÍA EN DEPÓSITO (si aplica)
        // =========================================
        calcularYAgregarCostoEstadia(tramo);

        // =========================================
        // 7. CAMBIAR ESTADO Y GUARDAR
        // =========================================
        tramo.setEstado(EstadoTramo.FINALIZADO);
        Tramo saved = repository.save(tramo);

        // =========================================
        // 8. LIBERAR CAMIÓN
        // =========================================
        try {
            flotaApiClient.actualizarDisponibilidadCamion(tramo.getCamionId(), true);
            System.out.println("✅ Camión " + tramo.getCamionId() + " liberado");
        } catch (Exception e) {
            System.err.println("⚠️ Error al liberar camión: " + e.getMessage());
        }

        // =========================================
        // 9. VERIFICAR SI ES EL ÚLTIMO TRAMO
        // =========================================
        verificarYCompletarSolicitud(tramo);

        System.out.println("✅ Tramo " + tramoId + " finalizado exitosamente");

        return saved;
    }

    /**
     * Calcula el costo real del transporte (sin estadía)
     * Fórmula: (distanciaKm × consumoPromedioCamion × precioLitro) + (distanciaKm × costoKmBase) + cargoFijo
     */
    private Double calcularCostoRealTransporte(Tramo tramo, Double distanciaKmReal) {
        Long camionId = tramo.getCamionId();

        // Obtener datos del camión
        CamionDTO camion = flotaApiClient.getCamion(camionId);
        if (camion == null) {
            System.err.println("⚠️ No se pudo obtener información del camión. Usando valores por defecto.");
            return distanciaKmReal * 100.0;
        }

        // Obtener parámetros globales
        backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto.ParametrosTarifaDTO parametros = 
            flotaApiClient.getParametros();

        double consumoPromedio = camion.getConsumoLitroKm() != null ? camion.getConsumoLitroKm() : 0.35;
        double precioLitro = parametros != null && parametros.getPrecioLitroCombustible() != null
                ? parametros.getPrecioLitroCombustible()
                : 1300.0;
        double costoKmBase = camion.getCostoKmBase() != null ? camion.getCostoKmBase() : 50.0;
        double cargoFijo = parametros != null && parametros.getCargoFijoTramo() != null
                ? parametros.getCargoFijoTramo()
                : 2500.0;

        // Calcular costo de combustible
        double costoCombustible = distanciaKmReal * consumoPromedio * precioLitro;
        
        // Calcular costo por distancia
        double costoDistancia = distanciaKmReal * costoKmBase;
        
        // Costo total del transporte
        double costoTotal = costoCombustible + costoDistancia + cargoFijo;

        System.out.println("💰 Desglose costo transporte:");
        System.out.println("   - Combustible: $" + costoCombustible);
        System.out.println("   - Distancia: $" + costoDistancia);
        System.out.println("   - Cargo fijo: $" + cargoFijo);
        System.out.println("   - TOTAL: $" + costoTotal);

        return Math.round(costoTotal * 100.0) / 100.0;
    }

    /**
     * Calcula y agrega el costo de estadía si el tramo viene de un depósito
     */
    private void calcularYAgregarCostoEstadia(Tramo tramo) {
        // Solo calcular estadía si el tramo tiene depositoOrigenId (viene de un depósito)
        if (tramo.getDepositoOrigenId() == null) {
            System.out.println("ℹ️ El tramo no viene de un depósito, no hay estadía");
            return;
        }

        // Buscar el tramo anterior (el que llegó a ese depósito)
        List<Tramo> todosLosTramosDeRuta = repository.findByRutaId(tramo.getRuta().getId());
        
        Tramo tramoAnterior = todosLosTramosDeRuta.stream()
                .filter(t -> t.getOrden() != null && tramo.getOrden() != null)
                .filter(t -> t.getOrden() == tramo.getOrden() - 1)
                .filter(t -> t.getDepositoDestinoId() != null)
                .filter(t -> t.getDepositoDestinoId().equals(tramo.getDepositoOrigenId()))
                .findFirst()
                .orElse(null);

        if (tramoAnterior == null || tramoAnterior.getFechaHoraFinReal() == null) {
            System.out.println("ℹ️ No se encontró tramo anterior con fecha fin real");
            return;
        }

        if (tramo.getFechaHoraInicioReal() == null) {
            System.out.println("⚠️ El tramo actual no tiene fecha inicio real");
            return;
        }

        // Calcular horas de estadía
        long horasEstadia = java.time.Duration.between(
            tramoAnterior.getFechaHoraFinReal(),
            tramo.getFechaHoraInicioReal()
        ).toHours();

        // Redondear hacia arriba a días completos
        int diasEstadia = (int) Math.ceil(horasEstadia / 24.0);

        if (diasEstadia <= 0) {
            System.out.println("ℹ️ No hay estadía (tiempo < 1 día)");
            return;
        }

        // Obtener costo de estadía del depósito
        Long depositoId = tramo.getDepositoOrigenId();
        Double costoEstadiaDiaria = 0.0;

        try {
            DepositoDTO deposito = flotaApiClient.getDeposito(depositoId);
            if (deposito != null && deposito.getCostoEstadiaDiaria() != null) {
                costoEstadiaDiaria = deposito.getCostoEstadiaDiaria();
            } else {
                System.out.println("⚠️ No se pudo obtener costo de estadía del depósito " + depositoId);
                costoEstadiaDiaria = 5000.0; // Valor por defecto
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al consultar depósito: " + e.getMessage());
            costoEstadiaDiaria = 5000.0;
        }

        double costoEstadiaTotal = diasEstadia * costoEstadiaDiaria;

        // Guardar información de estadía en el tramo
        tramo.setDiasEstadia(diasEstadia);
        tramo.setCostoEstadia(costoEstadiaTotal);

        // Sumar estadía al costo real del tramo
        Double costoRealActual = tramo.getCostoReal() != null ? tramo.getCostoReal() : 0.0;
        tramo.setCostoReal(costoRealActual + costoEstadiaTotal);

        System.out.println("🏢 Estadía calculada:");
        System.out.println("   - Depósito: " + depositoId);
        System.out.println("   - Horas: " + horasEstadia);
        System.out.println("   - Días (redondeado): " + diasEstadia);
        System.out.println("   - Costo diario: $" + costoEstadiaDiaria);
        System.out.println("   - Costo total estadía: $" + costoEstadiaTotal);
    }

    /**
     * Verifica si todos los tramos están finalizados y completa la solicitud
     */
    private void verificarYCompletarSolicitud(Tramo tramo) {
        var ruta = tramo.getRuta();
        Long solicitudId = ruta.getSolicitud().getId();

        // Obtener todos los tramos de la ruta
        List<Tramo> todosLosTramosDeRuta = repository.findByRutaId(ruta.getId());

        // Verificar si todos están finalizados
        boolean todosFinalizados = todosLosTramosDeRuta.stream()
                .allMatch(t -> t.getEstado() == EstadoTramo.FINALIZADO);

        if (!todosFinalizados) {
            System.out.println("ℹ️ Aún hay tramos pendientes de finalizar");
            return;
        }

        System.out.println("✅ Todos los tramos finalizados. Completando solicitud...");

        // Obtener la solicitud
        backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Solicitud solicitud = 
            solicitudService.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // =========================================
        // CALCULAR TIEMPO REAL TOTAL
        // =========================================
        // Usar fecha de solicitud y fecha fin real del último tramo
        Tramo ultimoTramo = todosLosTramosDeRuta.stream()
                .filter(t -> t.getFechaHoraFinReal() != null)
                .max((t1, t2) -> t1.getFechaHoraFinReal().compareTo(t2.getFechaHoraFinReal()))
                .orElse(tramo);

        if (solicitud.getFechaSolicitud() != null && ultimoTramo.getFechaHoraFinReal() != null) {
            long minutosRealesTotal = java.time.Duration.between(
                solicitud.getFechaSolicitud(),
                ultimoTramo.getFechaHoraFinReal()
            ).toMinutes();
            
            solicitud.setTiempoRealMin((int) minutosRealesTotal);
            
            System.out.println("⏱️ Tiempo real total: " + minutosRealesTotal + " minutos");
        }

        // =========================================
        // CALCULAR COSTO FINAL TOTAL
        // =========================================
        // Sumatoria de costos reales de todos los tramos (ya incluye estadías)
        Double costoFinalTotal = todosLosTramosDeRuta.stream()
                .mapToDouble(t -> t.getCostoReal() != null ? t.getCostoReal() : 0.0)
                .sum();

        solicitud.setCostoReal(costoFinalTotal);

        System.out.println("💰 Costo final total: $" + costoFinalTotal);
        System.out.println("   (incluye transporte + estadías)");

        // =========================================
        // CAMBIAR ESTADO A COMPLETADA
        // =========================================
        solicitudService.actualizarEstado(
            solicitudId,
            backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Estado.COMPLETADA,
            "Todos los tramos finalizados. Costo final: $" + costoFinalTotal + 
            ". Tiempo total: " + solicitud.getTiempoRealMin() + " minutos"
        );

        System.out.println("✅ Solicitud " + solicitudId + " marcada como COMPLETADA");
    }

    /**
     * Busca tramos por lista de camionIds y opcionalmente por estado
     */
    public List<Tramo> findByTransportistaAndEstado(List<Long> camionIds, EstadoTramo estado) {
        if (camionIds == null || camionIds.isEmpty()) {
            return List.of();
        }
        if (estado != null) {
            return repository.findByCamionIdInAndEstado(camionIds, estado);
        } else {
            return repository.findByCamionIdIn(camionIds);
        }
    }
}
