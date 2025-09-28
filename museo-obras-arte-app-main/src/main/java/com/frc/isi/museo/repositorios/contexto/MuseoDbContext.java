package com.frc.isi.museo.repositorios.contexto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MuseoDbContext {

    private final EntityManager manager;

    public static MuseoDbContext instance = null;

    private MuseoDbContext() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("museo");
        manager = emf.createEntityManager();
    }

    public static MuseoDbContext getInstance() {
        if (instance == null) {
            instance = new MuseoDbContext();
        }
        return instance;
    }

    public EntityManager getManager() {
        return this.manager;
    }

}
