package backend.tpi_Napoli_Spadoni_Rojas.operaciones.config;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.*;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
public class DataInitializer {

        private final SolicitudRepository solicitudRepository;
        private final RutaRepository rutaRepository;
        private final TramoRepository tramoRepository;
        private final CambioEstadoSolicitudRepository cambioEstadoRepository;

        public DataInitializer(SolicitudRepository solicitudRepository,
                        RutaRepository rutaRepository,
                        TramoRepository tramoRepository,
                        CambioEstadoSolicitudRepository cambioEstadoRepository) {
                this.solicitudRepository = solicitudRepository;
                this.rutaRepository = rutaRepository;
                this.tramoRepository = tramoRepository;
                this.cambioEstadoRepository = cambioEstadoRepository;
        }

        @PostConstruct
        public void init() {
                if (solicitudRepository.count() > 0) {
                        // evita duplicar datos
                        return;
                }

                System.out.println("🚀 Cargando datos iniciales en ms-operaciones...");

                // --- Solicitud 1 ---
                Solicitud solicitud1 = Solicitud.builder()
                                .clienteId(1L)
                                .contenedorId(1L)
                                .fechaSolicitud(LocalDateTime.now().minusDays(3))
                                .origen("Córdoba Capital")
                                .destino("Rosario")
                                .distanciaKm(400.0)
                                .estadoActual(Estado.EN_TRANSITO)
                                .build();
                solicitud1 = solicitudRepository.save(solicitud1);

                // --- Solicitud 2 ---
                // --- Solicitud 2 ---
                Solicitud solicitud2 = Solicitud.builder()
                                .clienteId(2L)
                                .contenedorId(2L)
                                .fechaSolicitud(LocalDateTime.now().minusDays(1))
                                .origen("La Plata")
                                .destino("Córdoba Capital")
                                .distanciaKm(700.0)
                                .estadoActual(Estado.ASIGNADA)
                                .build();
                solicitud2 = solicitudRepository.save(solicitud2);
                // --- Ruta asociada a solicitud 1 ---
                Ruta ruta1 = Ruta.builder()
                                .solicitud(solicitud1)
                                .fechaInicio(LocalDateTime.now().minusDays(2))
                                .fechaFinEstimada(LocalDateTime.now().plusHours(10))
                                .distanciaTotalKm(400.0)
                                .costoTotal(38000.0)
                                .build();
                ruta1 = rutaRepository.save(ruta1);

                // --- Ruta asociada a solicitud 2 ---
                Ruta ruta2 = Ruta.builder()
                                .solicitud(solicitud2)
                                .fechaInicio(LocalDateTime.now().minusHours(12))
                                .fechaFinEstimada(LocalDateTime.now().plusHours(20))
                                .distanciaTotalKm(700.0)
                                .costoTotal(67000.0)
                                .build();
                ruta2 = rutaRepository.save(ruta2);

                // --- Tramos para ruta 1 ---
                Tramo tramo1a = Tramo.builder()
                                .ruta(ruta1)
                                .origen("Córdoba Capital")
                                .destino("Villa María")
                                .distanciaKm(150.0)
                                .costo(13000.0)
                                .camionId(1L)
                                .depositoOrigenId(1L)
                                .depositoDestinoId(2L)
                                .fechaInicio(LocalDateTime.now().minusDays(2))
                                .fechaFinEstimada(LocalDateTime.now().minusDays(1))
                                .estado(EstadoTramo.FINALIZADO)
                                .tipo(TipoTramo.DEPOSITO_DEPOSITO)
                                .orden(1)
                                .build();
                tramoRepository.save(tramo1a);

                Tramo tramo1b = Tramo.builder()
                                .ruta(ruta1)
                                .origen("Villa María")
                                .destino("Rosario")
                                .distanciaKm(250.0)
                                .costo(25000.0)
                                .camionId(2L)
                                .depositoOrigenId(2L)
                                .depositoDestinoId(3L)
                                .fechaInicio(LocalDateTime.now().minusDays(1))
                                .fechaFinEstimada(LocalDateTime.now().plusHours(6))
                                .estado(EstadoTramo.INICIADO)
                                .tipo(TipoTramo.DEPOSITO_DESTINO)
                                .orden(2)
                                .build();
                tramoRepository.save(tramo1b);

                // --- Tramo para ruta 2 ---
                Tramo tramo2a = Tramo.builder()
                                .ruta(ruta2)
                                .origen("La Plata")
                                .destino("Córdoba Capital")
                                .distanciaKm(700.0)
                                .costo(67000.0)
                                .camionId(3L)
                                .depositoOrigenId(4L)
                                .depositoDestinoId(5L)
                                .fechaInicio(LocalDateTime.now().minusHours(8))
                                .fechaFinEstimada(LocalDateTime.now().plusHours(10))
                                .estado(EstadoTramo.ASIGNADO)
                                .tipo(TipoTramo.ORIGEN_DESTINO)
                                .orden(1)
                                .build();
                tramoRepository.save(tramo2a);

                // --- Historial de estados para solicitud 1 ---
                cambioEstadoRepository.saveAll(List.of(
                                CambioEstadoSolicitud.builder()
                                                .solicitud(solicitud1)
                                                .estadoAnterior(Estado.PENDIENTE)
                                                .estadoNuevo(Estado.ASIGNADA)
                                                .fechaCambio(LocalDateTime.now().minusDays(3))
                                                .observaciones("Solicitud asignada a camión AA123BB")
                                                .build(),
                                CambioEstadoSolicitud.builder()
                                                .solicitud(solicitud1)
                                                .estadoAnterior(Estado.ASIGNADA)
                                                .estadoNuevo(Estado.EN_TRANSITO)
                                                .fechaCambio(LocalDateTime.now().minusDays(1))
                                                .observaciones("Tramo Córdoba → Rosario en curso")
                                                .build()));

                // --- Historial para solicitud 2 ---
                cambioEstadoRepository.save(
                                CambioEstadoSolicitud.builder()
                                                .solicitud(solicitud2)
                                                .estadoAnterior(Estado.PENDIENTE)
                                                .estadoNuevo(Estado.ASIGNADA)
                                                .fechaCambio(LocalDateTime.now().minusHours(12))
                                                .observaciones("Camión AC555EE asignado")
                                                .build());

                System.out.println("✅ DataInitializer (ms-operaciones): datos de prueba cargados correctamente.");
        }
}
