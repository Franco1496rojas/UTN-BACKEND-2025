package backend.tpi_Napoli_Spadoni_Rojas.flota.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CamionRepository extends JpaRepository<Camion, Long> {
    List<Camion> findByDisponibilidadTrue();

    List<Camion> findByTransportistaId(Long transportistaId);

    Optional<Camion> findByDominio(String dominio);

    boolean existsByDominio(String dominio);
}
