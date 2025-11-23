package backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.CambioEstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CambioEstadoSolicitudRepository extends JpaRepository<CambioEstadoSolicitud, Long> {
    List<CambioEstadoSolicitud> findBySolicitudIdOrderByFechaCambioAsc(Long solicitudId);

    // Navegación a través de la relación: solicitud.contenedorId
    List<CambioEstadoSolicitud> findBySolicitud_ContenedorIdOrderByFechaCambioAsc(Long contenedorId);
}
