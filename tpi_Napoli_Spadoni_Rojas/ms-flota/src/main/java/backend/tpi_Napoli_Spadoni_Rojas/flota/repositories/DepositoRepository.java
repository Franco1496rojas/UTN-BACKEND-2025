package backend.tpi_Napoli_Spadoni_Rojas.flota.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    Optional<Deposito> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
