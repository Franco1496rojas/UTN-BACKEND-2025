package backend.tpi_Napoli_Spadoni_Rojas.clientes.services;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Ciudad;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Cliente;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.CiudadRepository;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final CiudadRepository ciudadRepository;

    public ClienteService(ClienteRepository repository, CiudadRepository ciudadRepository) {
        this.repository = repository;
        this.ciudadRepository = ciudadRepository;
    }

    public List<Cliente> findAll() {
        return repository.findAll();
    }

    public Optional<Cliente> findById(Long id) {
        return repository.findById(id);
    }

    public List<Cliente> findByApellido(String apellido) {
        return repository.findByApellidoContainingIgnoreCase(apellido);
    }

    public Cliente save(Cliente cliente) {
        // Validar y cargar la ciudad completa por ID
        if (cliente.getCiudad() == null || cliente.getCiudad().getId() == null) {
            throw new IllegalArgumentException("Debe especificar una ciudad válida");
        }
        Ciudad ciudad = ciudadRepository.findById(cliente.getCiudad().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una ciudad con el ID: " + cliente.getCiudad().getId()));
        cliente.setCiudad(ciudad);

        return repository.save(cliente);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
