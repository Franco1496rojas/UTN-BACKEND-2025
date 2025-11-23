package backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    List<Ciudad> findByProvinciaId(Long provinciaId);
    Optional<Ciudad> findByNombreIgnoreCaseAndProvinciaId(String nombre, Long provinciaId);
    boolean existsByNombreIgnoreCaseAndProvinciaId(String nombre, Long provinciaId);
    Optional<Ciudad> findByCodigoPostal(String codigoPostal);
    boolean existsByCodigoPostal(String codigoPostal);
}
