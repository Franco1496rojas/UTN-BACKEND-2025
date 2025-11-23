package backend.tpi_Napoli_Spadoni_Rojas.flota.services;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Camion;
import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Transportista;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.CamionRepository;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.TransportistaRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CamionService {

    private final CamionRepository repository;
    private final TransportistaRepository transportistaRepository;

    public CamionService(CamionRepository repository, TransportistaRepository transportistaRepository) {
        this.repository = repository;
        this.transportistaRepository = transportistaRepository;
    }

    public List<Camion> findAll() {
        return repository.findAll();
    }

    public Optional<Camion> findById(Long id) {
        return repository.findById(id);
    }

    public List<Camion> findDisponibles() {
        return repository.findByDisponibilidadTrue();
    }

    // Implementacion del metodo para obtener camiones disponibles segun peso y
    // volumen
    public List<Camion> findDisponiblesPesoVolumen(Double pesoMaximo, Double volumenMaximo) {
        return repository.findByDisponibilidadTrue().stream()
                .filter(c -> (pesoMaximo == null || c.getCapacidadPeso() >= pesoMaximo) &&
                        (volumenMaximo == null || c.getCapacidadVolumen() >= volumenMaximo))
                .toList();
    }

    public List<Camion> findByTransportista(Long id) {
        return repository.findByTransportistaId(id);
    }

    public Camion save(Camion camion) {
        // Validar que el dominio no exista (para creación)
        if (camion.getId() == null && repository.existsByDominio(camion.getDominio())) {
            throw new IllegalArgumentException("Ya existe un camión con el dominio: " + camion.getDominio());
        }

        // Validar que el dominio no esté siendo usado por otro camión (para
        // actualización)
        if (camion.getId() != null) {
            Optional<Camion> camionExistente = repository.findByDominio(camion.getDominio());
            if (camionExistente.isPresent() && !camionExistente.get().getId().equals(camion.getId())) {
                throw new IllegalArgumentException("Ya existe un camión con el dominio: " + camion.getDominio());
            }
        }

        // Cargar transportista por ID para evitar nulls en la respuesta
        if (camion.getTransportista() == null || camion.getTransportista().getId() == null) {
            throw new IllegalArgumentException("Debe especificar un transportista válido");
        }
        Transportista t = transportistaRepository.findById(camion.getTransportista().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un transportista con el ID: " + camion.getTransportista().getId()));
        camion.setTransportista(t);

        return repository.save(camion);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
