package backend.tpi_Napoli_Spadoni_Rojas.clientes.services;

import backend.tpi_Napoli_Spadoni_Rojas.clientes.models.Provincia;
import backend.tpi_Napoli_Spadoni_Rojas.clientes.repositories.ProvinciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProvinciaService {
    private final ProvinciaRepository repository;

    public ProvinciaService(ProvinciaRepository repository) {
        this.repository = repository;
    }

    public List<Provincia> findAll() {
        return repository.findAll();
    }

    public Optional<Provincia> findById(Long id) {
        return repository.findById(id);
    }

    public Provincia save(Provincia provincia) {
        // Validación 1: El nombre no puede estar vacío
        if (provincia.getNombre() == null || provincia.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la provincia no puede estar vacío");
        }

        // Validación 2: El nombre debe tener al menos 3 caracteres
        if (provincia.getNombre().trim().length() < 3) {
            throw new IllegalArgumentException("El nombre de la provincia debe tener al menos 3 caracteres");
        }

        // Validación 3: No debe haber provincias con el mismo nombre (para creación)
        if (provincia.getId() == null && repository.existsByNombreIgnoreCase(provincia.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una provincia con el nombre: " + provincia.getNombre());
        }

        // Validación 4: No debe haber otra provincia con el mismo nombre (para actualización)
        if (provincia.getId() != null) {
            Optional<Provincia> provinciaExistente = repository.findByNombreIgnoreCase(provincia.getNombre().trim());
            if (provinciaExistente.isPresent() && !provinciaExistente.get().getId().equals(provincia.getId())) {
                throw new IllegalArgumentException("Ya existe una provincia con el nombre: " + provincia.getNombre());
            }
        }

        // Normalizar el nombre (primera letra mayúscula)
        provincia.setNombre(capitalizarNombre(provincia.getNombre().trim()));

        return repository.save(provincia);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No existe una provincia con el ID: " + id);
        }
        
        // Aquí podrías agregar validación para no eliminar provincias con ciudades asociadas
        // if (ciudadRepository.existsByProvinciaId(id)) {
        //     throw new IllegalArgumentException("No se puede eliminar la provincia porque tiene ciudades asociadas");
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
