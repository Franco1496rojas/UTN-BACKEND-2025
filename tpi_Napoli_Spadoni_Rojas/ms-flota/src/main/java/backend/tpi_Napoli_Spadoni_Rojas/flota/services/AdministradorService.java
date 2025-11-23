package backend.tpi_Napoli_Spadoni_Rojas.flota.services;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.Administrador;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.AdministradorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AdministradorService {

    private final AdministradorRepository repository;

    public AdministradorService(AdministradorRepository repository) {
        this.repository = repository;
    }

    public List<Administrador> findAll() {
        return repository.findAll();
    }

    public Optional<Administrador> findById(Long id) {
        return repository.findById(id);
    }

    public Administrador save(Administrador admin) {
        return repository.save(admin);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
