package com.frc.isi.museo.repositorios;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.frc.isi.museo.entidades.Museo;

public class MuseoRepository extends Repository<Museo, Integer> {

    public MuseoRepository() {
        super();
    }

    @Override
    public Museo getById(Integer id) {
        return this.manager.find(Museo.class, id);
    }

    @Override
    public Set<Museo> getAll() {
        // Aqui no busco los resultados sino que le pido a hibernate que me
        // envie los resultados ya en un stream para hacer lo que necesite
        return this.manager.createNamedQuery("Museo.GetAll", Museo.class)
                .getResultStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Museo> getAllStrem() {
        return this.manager.createQuery("SELECT o FROM Museo o", Museo.class).getResultStream();
    }

    @Override
    public boolean existeByNombreOrDescripcion(String valor) {
        return this.manager.createNamedQuery("Museo.GetByNombre", Museo.class)
                .setParameter("nombre", valor)
                .getResultStream()
                .findAny()
                .isPresent();
    }

}
