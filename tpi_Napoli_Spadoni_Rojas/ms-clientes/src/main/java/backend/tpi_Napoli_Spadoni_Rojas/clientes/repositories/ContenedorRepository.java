package backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    List<Contenedor> findByClienteId(Long clienteId);
    Optional<Contenedor> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
}
