package com.frc.isi.museo.servicios;

import java.util.HashMap;
import java.util.Map;

import com.frc.isi.museo.entidades.Museo;
import com.frc.isi.museo.repositorios.MuseoRepository;
import com.frc.isi.museo.servicios.interfaces.ILookUpOrPersistService;

public class MuseoService implements ILookUpOrPersistService<Museo> {

    private final MuseoRepository museoRepository;
    private final Map<String, Museo> museos;

    public MuseoService() {
        museoRepository = new MuseoRepository();
        museos = new HashMap<>();
    }

    @Override
    public Museo getOrCreateAutor(String descripcion) {
        return this.museos.computeIfAbsent(descripcion, desc -> {
            Museo museo = new Museo();
            museo.setNombre(desc);
            museoRepository.add(museo);
            return museo;
        });
    }

    public boolean existe(String museo) {
        return museoRepository.existeByNombreOrDescripcion(museo);
    }
}
