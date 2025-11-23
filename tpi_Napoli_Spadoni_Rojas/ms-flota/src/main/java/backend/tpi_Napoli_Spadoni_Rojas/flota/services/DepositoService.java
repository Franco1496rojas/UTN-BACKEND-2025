package backend.tpi_Napoli_Spadoni_Rojas.flota.services;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Deposito;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.DepositoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepositoService {

    private final DepositoRepository repository;

    public DepositoService(DepositoRepository repository) {
        this.repository = repository;
    }

    public List<Deposito> findAll() {
        return repository.findAll();
    }

    public Optional<Deposito> findById(Long id) {
        return repository.findById(id);
    }

    public Deposito save(Deposito deposito) {
        // Validación 1: El nombre no puede estar vacío
        if (deposito.getNombre() == null || deposito.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del depósito no puede estar vacío");
        }

        // Validación 2: La dirección no puede estar vacía
        if (deposito.getDireccion() == null || deposito.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección del depósito no puede estar vacía");
        }

        // Validación 3: El costo de estadía debe ser positivo
        if (deposito.getCostoEstadiaDiaria() == null || deposito.getCostoEstadiaDiaria() <= 0) {
            throw new IllegalArgumentException("El costo de estadía diaria debe ser mayor a cero");
        }

        // Validación 4: La capacidad máxima debe ser positiva
        if (deposito.getCapacidadMaxima() == null || deposito.getCapacidadMaxima() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a cero");
        }

        // Validación 5: La cantidad ocupada no puede ser negativa
        if (deposito.getCantidadOcupada() == null || deposito.getCantidadOcupada() < 0) {
            throw new IllegalArgumentException("La cantidad ocupada no puede ser negativa");
        }

        // Validación 6: La cantidad ocupada no puede exceder la capacidad máxima
        if (deposito.getCantidadOcupada() > deposito.getCapacidadMaxima()) {
            throw new IllegalArgumentException(
                String.format("La cantidad ocupada (%d) no puede exceder la capacidad máxima (%d)",
                    deposito.getCantidadOcupada(), deposito.getCapacidadMaxima())
            );
        }

        // Validación 7: Al crear, la cantidad ocupada debe inicializarse en 0
        if (deposito.getId() == null && deposito.getCantidadOcupada() == null) {
            deposito.setCantidadOcupada(0);
        }

        // Validación 8: El estado no puede ser nulo
        if (deposito.getEstado() == null) {
            deposito.setEstado(true); // Por defecto activo
        }

        // Validación 9: El nombre no debe estar duplicado (creación)
        if (deposito.getId() == null && repository.existsByNombreIgnoreCase(deposito.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un depósito con el nombre: " + deposito.getNombre());
        }

        // Validación 10: El nombre no debe estar duplicado (actualización)
        if (deposito.getId() != null) {
            Optional<Deposito> depositoExistente = repository.findByNombreIgnoreCase(deposito.getNombre().trim());
            if (depositoExistente.isPresent() && !depositoExistente.get().getId().equals(deposito.getId())) {
                throw new IllegalArgumentException("Ya existe un depósito con el nombre: " + deposito.getNombre());
            }
        }

        // Normalizar datos
        deposito.setNombre(deposito.getNombre().trim());
        deposito.setDireccion(deposito.getDireccion().trim());

        return repository.save(deposito);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No existe un depósito con el ID: " + id);
        }

        // Verificar si el depósito tiene contenedores
        Optional<Deposito> deposito = repository.findById(id);
        if (deposito.isPresent() && deposito.get().getCantidadOcupada() > 0) {
            throw new IllegalArgumentException(
                String.format("No se puede eliminar el depósito porque tiene %d contenedores ocupados",
                    deposito.get().getCantidadOcupada())
            );
        }

        repository.deleteById(id);
    }

    /**
     * Incrementa la ocupación del depósito
     */
    public Deposito incrementarOcupacion(Long depositoId, int cantidad) {
        Deposito deposito = repository.findById(depositoId)
            .orElseThrow(() -> new IllegalArgumentException("No existe un depósito con el ID: " + depositoId));

        if (!deposito.tieneCapacidadPara(cantidad)) {
            throw new IllegalArgumentException(
                String.format("El depósito '%s' no tiene capacidad suficiente. Disponible: %d, Solicitado: %d",
                    deposito.getNombre(), deposito.getCapacidadDisponible(), cantidad)
            );
        }

        deposito.incrementarOcupacion(cantidad);
        return repository.save(deposito);
    }

    /**
     * Decrementa la ocupación del depósito
     */
    public Deposito decrementarOcupacion(Long depositoId, int cantidad) {
        Deposito deposito = repository.findById(depositoId)
            .orElseThrow(() -> new IllegalArgumentException("No existe un depósito con el ID: " + depositoId));

        deposito.decrementarOcupacion(cantidad);
        return repository.save(deposito);
    }

    public int contarContenedoresPendientes(Long depositoId) {
        Deposito deposito = repository.findById(depositoId)
            .orElseThrow(() -> new IllegalArgumentException("No existe un depósito con el ID: " + depositoId));

        return deposito.getCantidadOcupada();
    }
}
