package com.frc.isi.museo.servicios;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.frc.isi.museo.entidades.EstiloArtistico;
import com.frc.isi.museo.repositorios.EstiloArtisticoRepository;
import com.frc.isi.museo.servicios.interfaces.ILookUpOrPersistService;

public class EstiloArtisticoService implements ILookUpOrPersistService<EstiloArtistico> {

    private final EstiloArtisticoRepository estiloArtisticoRepository;
    private Map<String, EstiloArtistico> estilos;

    public EstiloArtisticoService() {
        estiloArtisticoRepository = new EstiloArtisticoRepository();
        estilos = new HashMap<>();
    }

    @Override
    public EstiloArtistico getOrCreateAutor(String descripcion) {
        return this.estilos.computeIfAbsent(descripcion, desc -> {
            EstiloArtistico estilo = new EstiloArtistico();
            estilo.setNombre(desc);
            estiloArtisticoRepository.add(estilo);
            return estilo;
        });
    }

    public Map<String, Long> getCantidadObrasPorEstilo() {
        Map<String, Long> resultado;
        var estilos = this.estiloArtisticoRepository.getAllStrem();

        resultado = estilos.collect(Collectors.groupingBy(EstiloArtistico::getNombre,
                Collectors.summingLong(p -> p.getObras().size())));

        return resultado;
    }

}
