package backend.tpi_Napoli_Spadoni_Rojas.clientes.services;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Ciudad;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Provincia;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.CiudadRepository;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.ProvinciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CiudadService {
    private final CiudadRepository repository;
    private final ProvinciaRepository provinciaRepository;

    public CiudadService(CiudadRepository repository, ProvinciaRepository provinciaRepository) {
        this.repository = repository;
        this.provinciaRepository = provinciaRepository;
    }

    public List<Ciudad> findAll() {
        return repository.findAll();
    }

    public List<Ciudad> findByProvinciaId(Long provinciaId) {
        return repository.findByProvinciaId(provinciaId);
    }

    public Optional<Ciudad> findById(Long id) {
        return repository.findById(id);
    }

    public Ciudad save(Ciudad ciudad) {
        // Validación 1: El nombre no puede estar vacío
        if (ciudad.getNombre() == null || ciudad.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la ciudad no puede estar vacío");
        }

        // Validación 2: El nombre debe tener al menos 2 caracteres
        if (ciudad.getNombre().trim().length() < 2) {
            throw new IllegalArgumentException("El nombre de la ciudad debe tener al menos 2 caracteres");
        }

        // Validación 3: El código postal no puede estar vacío
        if (ciudad.getCodigoPostal() == null || ciudad.getCodigoPostal().trim().isEmpty()) {
            throw new IllegalArgumentException("El código postal no puede estar vacío");
        }

        // Validación 4: La provincia debe existir
        if (ciudad.getProvincia() == null || ciudad.getProvincia().getId() == null) {
            throw new IllegalArgumentException("Debe especificar una provincia válida");
        }
        Provincia provincia = provinciaRepository.findById(ciudad.getProvincia().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una provincia con el ID: " + ciudad.getProvincia().getId()));
        ciudad.setProvincia(provincia);

        // Validación 5: No debe haber otra ciudad con el mismo nombre en la misma
        // provincia (creación)
        if (ciudad.getId() == null &&
                repository.existsByNombreIgnoreCaseAndProvinciaId(ciudad.getNombre().trim(),
                        ciudad.getProvincia().getId())) {
            throw new IllegalArgumentException(
                    String.format("Ya existe una ciudad con el nombre '%s' en esta provincia", ciudad.getNombre()));
        }

        // Validación 6: No debe haber otra ciudad con el mismo nombre en la misma
        // provincia (actualización)
        if (ciudad.getId() != null) {
            Optional<Ciudad> ciudadExistente = repository.findByNombreIgnoreCaseAndProvinciaId(
                    ciudad.getNombre().trim(),
                    ciudad.getProvincia().getId());
            if (ciudadExistente.isPresent() && !ciudadExistente.get().getId().equals(ciudad.getId())) {
                throw new IllegalArgumentException(
                        String.format("Ya existe una ciudad con el nombre '%s' en esta provincia", ciudad.getNombre()));
            }
        }

        // Validación 7: El código postal no debe estar duplicado (creación)
        if (ciudad.getId() == null && repository.existsByCodigoPostal(ciudad.getCodigoPostal().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe una ciudad con el código postal: " + ciudad.getCodigoPostal());
        }

        // Validación 8: El código postal no debe estar duplicado (actualización)
        if (ciudad.getId() != null) {
            Optional<Ciudad> ciudadExistente = repository.findByCodigoPostal(ciudad.getCodigoPostal().trim());
            if (ciudadExistente.isPresent() && !ciudadExistente.get().getId().equals(ciudad.getId())) {
                throw new IllegalArgumentException(
                        "Ya existe una ciudad con el código postal: " + ciudad.getCodigoPostal());
            }
        }

        // Normalizar datos
        ciudad.setNombre(capitalizarNombre(ciudad.getNombre().trim()));
        ciudad.setCodigoPostal(ciudad.getCodigoPostal().trim());

        return repository.save(ciudad);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No existe una ciudad con el ID: " + id);
        }

        // Aquí podrías agregar validación para no eliminar ciudades con clientes
        // asociados
        // if (clienteRepository.existsByCiudadId(id)) {
        // throw new IllegalArgumentException("No se puede eliminar la ciudad porque
        // tiene clientes asociados");
        // }

        repository.deleteById(id);
    }

    private String capitalizarNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return nombre;
        }
        String[] palabras = nombre.split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return resultado.toString().trim();
    }
}
