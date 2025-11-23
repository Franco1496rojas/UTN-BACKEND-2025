package backend.tpi_Napoli_Spadoni_Rojas.operaciones.services;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.*;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories.CambioEstadoSolicitudRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CambioEstadoSolicitudService {

    private final CambioEstadoSolicitudRepository repository;

    public CambioEstadoSolicitudService(CambioEstadoSolicitudRepository repository) {
        this.repository = repository;
    }

    public List<CambioEstadoSolicitud> findByContenedor(Long contenedorId) {
        return repository.findBySolicitud_ContenedorIdOrderByFechaCambioAsc(contenedorId);
    }

    public List<CambioEstadoSolicitud> findBySolicitud(Long solicitudId) {
        return repository.findBySolicitudIdOrderByFechaCambioAsc(solicitudId);
    }

    public void registrarCambio(Solicitud solicitud, Estado anterior, Estado nuevo,
            String observaciones) {
        // Si es el primer cambio (creación) anterior puede venir null. No forzamos un
        // valor
        // en DB porque la columna ahora permite null. Opcionalmente podrías setear
        // anterior = nuevo para mantener un historial más consistente.
        CambioEstadoSolicitud cambio = CambioEstadoSolicitud.builder()
                .solicitud(solicitud)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fechaCambio(LocalDateTime.now())
                .observaciones(observaciones)
                .build();
        repository.save(cambio);
    }
}
