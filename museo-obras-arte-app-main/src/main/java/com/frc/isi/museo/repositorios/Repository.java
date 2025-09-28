package com.frc.isi.museo.repositorios;

import java.util.Set;
import java.util.stream.Stream;

import com.frc.isi.museo.repositorios.contexto.MuseoDbContext;

import jakarta.persistence.EntityManager;

public abstract class Repository<T, K> {

    protected EntityManager manager;

    public Repository() {
        manager = MuseoDbContext.getInstance().getManager();
    }

    public void add(T entity) {
        var transaction = manager.getTransaction();
        transaction.begin();
        manager.persist(entity);
        transaction.commit();
    }

    public void update(T entity) {
        var transaction = manager.getTransaction();
        transaction.begin();
        manager.merge(entity);
        transaction.commit();
    }

    public T delete(K id) {
        var transaction = manager.getTransaction();
        transaction.begin();
        var entity = this.getById(id);
        manager.remove(entity);
        transaction.commit();
        return entity;
    }

    public abstract T getById(K id);

    public abstract Set<T> getAll();

    public abstract Stream<T> getAllStrem();

    public boolean existeByNombreOrDescripcion(String valor) {
        return false;
    };

}
