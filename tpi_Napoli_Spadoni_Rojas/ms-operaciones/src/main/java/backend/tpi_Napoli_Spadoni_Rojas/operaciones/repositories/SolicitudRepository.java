package backend.tpi_Napoli_Spadoni_Rojas.operaciones.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Estado;
import backend.tpi_Napoli_Spadoni_Rojas.operaciones.models.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByClienteId(Long clienteId);

    List<Solicitud> findByEstadoActual(Estado estado);

    List<Solicitud> findByClienteIdAndEstadoActual(Long clienteId, Estado estado);

    List<Solicitud> findByClienteIdAndEstadoActualNot(Long clienteId, Estado estado);
}
