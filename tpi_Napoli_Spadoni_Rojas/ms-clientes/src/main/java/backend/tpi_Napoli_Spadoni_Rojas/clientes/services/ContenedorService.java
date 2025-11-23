package backend.tpi_Napoli_Spadoni_Rojas.clientes.services;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Cliente;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Contenedor;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.ClienteRepository;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.ContenedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContenedorService {

    private final ContenedorRepository repository;
    private final ClienteRepository clienteRepository;

    public ContenedorService(ContenedorRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    public List<Contenedor> findAll() {
        return repository.findAll();
    }

    public Optional<Contenedor> findById(Long id) {
        return repository.findById(id);
    }

    public List<Contenedor> findByCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    public Contenedor save(Contenedor contenedor) {
        // Validación 1: El código no puede estar vacío
        if (contenedor.getCodigo() == null || contenedor.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del contenedor no puede estar vacío");
        }

        // Validación 2: El peso debe ser positivo
        if (contenedor.getPeso() == null || contenedor.getPeso() <= 0) {
            throw new IllegalArgumentException("El peso del contenedor debe ser mayor a cero");
        }

        // Validación 3: El volumen debe ser positivo
        if (contenedor.getVolumen() == null || contenedor.getVolumen() <= 0) {
            throw new IllegalArgumentException("El volumen del contenedor debe ser mayor a cero");
        }

        // Validación 4: El cliente no puede ser nulo
        if (contenedor.getCliente() == null || contenedor.getCliente().getId() == null) {
            throw new IllegalArgumentException("Debe especificar un cliente para el contenedor");
        }

        // Validación 5: El cliente debe existir
        if (!clienteRepository.existsById(contenedor.getCliente().getId())) {
            throw new IllegalArgumentException("No existe un cliente con el ID: " + contenedor.getCliente().getId());
        }

        Cliente cliente = clienteRepository.findById(contenedor.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con el ID: " + contenedor.getCliente().getId()));
        contenedor.setCliente(cliente);

        // Validación 6: El código no debe estar duplicado (creación)
        if (contenedor.getId() == null && repository.existsByCodigoIgnoreCase(contenedor.getCodigo().trim())) {
            throw new IllegalArgumentException("Ya existe un contenedor con el código: " + contenedor.getCodigo());
        }

        // Validación 7: El código no debe estar duplicado (actualización)
        if (contenedor.getId() != null) {
            Optional<Contenedor> contenedorExistente = repository.findByCodigoIgnoreCase(contenedor.getCodigo().trim());
            if (contenedorExistente.isPresent() && !contenedorExistente.get().getId().equals(contenedor.getId())) {
                throw new IllegalArgumentException("Ya existe un contenedor con el código: " + contenedor.getCodigo());
            }
        }

        // Normalizar datos
        contenedor.setCodigo(contenedor.getCodigo().trim().toUpperCase());

        return repository.save(contenedor);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No existe un contenedor con el ID: " + id);
        }
        repository.deleteById(id);
    }
}
