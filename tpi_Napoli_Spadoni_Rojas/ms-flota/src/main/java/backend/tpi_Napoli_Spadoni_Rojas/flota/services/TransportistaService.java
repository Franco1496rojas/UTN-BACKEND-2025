package backend.tpi_Napoli_Spadoni_Rojas.flota.services;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Transportista;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.TransportistaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransportistaService {

    private final TransportistaRepository repository;

    public TransportistaService(TransportistaRepository repository) {
        this.repository = repository;
    }

    public List<Transportista> findAll() {
        return repository.findAll();
    }

    public Optional<Transportista> findById(Long id) {
        return repository.findById(id);
    }

    public Transportista save(Transportista transportista) {
        return repository.save(transportista);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
