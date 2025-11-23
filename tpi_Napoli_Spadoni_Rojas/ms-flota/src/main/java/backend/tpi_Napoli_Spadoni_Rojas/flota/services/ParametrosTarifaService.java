package backend.tpi_Napoli_Spadoni_Rojas.flota.services;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.ParametrosTarifa;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.ParametrosTarifaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParametrosTarifaService {

    private final ParametrosTarifaRepository repository;

    public ParametrosTarifaService(ParametrosTarifaRepository repository) {
        this.repository = repository;
    }

    public List<ParametrosTarifa> findAll() {
        return repository.findAll();
    }

    public java.util.Optional<ParametrosTarifa> findById(Long id) {
        return repository.findById(id);
    }
    public ParametrosTarifa save(ParametrosTarifa parametros) {
        return repository.save(parametros);
    }
}
