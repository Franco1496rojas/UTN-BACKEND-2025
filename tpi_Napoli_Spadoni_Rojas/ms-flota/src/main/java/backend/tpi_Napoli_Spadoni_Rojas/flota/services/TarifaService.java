package backend.tpi_Napoli_Spadoni_Rojas.flota.services;

import backend.tpi_Napoli_Spadoni_Rojas.flota.models.TarifaRango;
import backend.tpi_Napoli_Spadoni_Rojas.flota.repositories.TarifaRangoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {

    private final TarifaRangoRepository repository;

    public TarifaService(TarifaRangoRepository repository) {
        this.repository = repository;
    }

    public List<TarifaRango> findAll() {
        return repository.findAll();
    }

    public Optional<TarifaRango> findById(Long id) {
        return repository.findById(id);
    }

    public TarifaRango save(TarifaRango tarifa) {
        // Validación 1: El mínimo no puede ser mayor al máximo (Volumen)
        if (tarifa.getVolumenMin() >= tarifa.getVolumenMax()) {
            throw new IllegalArgumentException(
                String.format("El volumen mínimo (%.2f) debe ser menor al volumen máximo (%.2f)", 
                    tarifa.getVolumenMin(), tarifa.getVolumenMax())
            );
        }

        // Validación 2: El mínimo no puede ser mayor al máximo (Peso)
        if (tarifa.getPesoMin() >= tarifa.getPesoMax()) {
            throw new IllegalArgumentException(
                String.format("El peso mínimo (%.2f) debe ser menor al peso máximo (%.2f)", 
                    tarifa.getPesoMin(), tarifa.getPesoMax())
            );
        }

        // Validación 3: Los valores no pueden ser negativos
        if (tarifa.getVolumenMin() < 0 || tarifa.getVolumenMax() < 0) {
            throw new IllegalArgumentException("Los valores de volumen no pueden ser negativos");
        }

        if (tarifa.getPesoMin() < 0 || tarifa.getPesoMax() < 0) {
            throw new IllegalArgumentException("Los valores de peso no pueden ser negativos");
        }

        if (tarifa.getCostoKmBase() <= 0) {
            throw new IllegalArgumentException("El costo por kilómetro debe ser mayor a cero");
        }

        // Validación 4: No debe haber rangos solapados
        List<TarifaRango> rangosSolapados = repository.findOverlappingRangos(
            tarifa.getId(),
            tarifa.getVolumenMin(),
            tarifa.getVolumenMax(),
            tarifa.getPesoMin(),
            tarifa.getPesoMax()
        );

        if (!rangosSolapados.isEmpty()) {
            TarifaRango solapado = rangosSolapados.get(0);
            throw new IllegalArgumentException(
                String.format(
                    "El rango especificado se solapa con una tarifa existente (ID: %d). " +
                    "Rango existente: Volumen [%.2f - %.2f], Peso [%.2f - %.2f]",
                    solapado.getId(),
                    solapado.getVolumenMin(),
                    solapado.getVolumenMax(),
                    solapado.getPesoMin(),
                    solapado.getPesoMax()
                )
            );
        }

        return repository.save(tarifa);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No existe una tarifa con el ID: " + id);
        }
        repository.deleteById(id);
    }
}
