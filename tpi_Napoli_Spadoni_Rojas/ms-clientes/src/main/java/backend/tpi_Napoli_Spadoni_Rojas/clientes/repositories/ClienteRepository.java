package backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByApellidoContainingIgnoreCase(String apellido);
}
