package com.frc.isi.museo.repositorios;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.frc.isi.museo.entidades.Museo;
import com.frc.isi.museo.entidades.ObraArtistica;

public class ObraArtisticaRepository extends Repository<ObraArtistica, Integer> {

    public ObraArtisticaRepository() {
        super();
    }

    @Override
    public ObraArtistica getById(Integer id) {
        // SELECT o FROM o WHERE o.codigo = :codigo
        var lista = this.manager.createNamedQuery("ObraArtistica.GetById", ObraArtistica.class)
                .setParameter("codigo", id)
                .getResultList();
        return lista.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe una Obra con el Id" + id));
    }

    @Override
    public Set<ObraArtistica> getAll() {
        return this.manager.createQuery("SELECT o FROM ObraArtistica o", ObraArtistica.class)
                .getResultList().stream().collect(Collectors.toSet());
    }

    @Override
    public Stream<ObraArtistica> getAllStrem() {
        return this.manager.createQuery("SELECT o FROM ObraArtistica o", ObraArtistica.class).getResultStream();
    }

    @Override
    public boolean existeByNombreOrDescripcion(String valor) {
        return this.manager.createNamedQuery("ObraArtistica.GetByNombre", ObraArtistica.class)
                .setParameter("nombre", valor)
                .getResultStream()
                .findAny()
                .isPresent();
    }

}
