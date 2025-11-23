package backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {
    Optional<Provincia> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
