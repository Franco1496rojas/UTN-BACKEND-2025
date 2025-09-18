package utnfc.isi.back.jpa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class LocalEntityManagerProvider {

    private static final String PU_NAME = "chinookPU"; // Nombre de la unidad de persistencia
    private static final EntityManagerFactory emf = buildEmf(); // EMF único para toda la app
    private static final ThreadLocal<EntityManager> current = new ThreadLocal<>(); // EM por hilo

    private LocalEntityManagerProvider() {
    } // Clase utilitaria, no instanciable

    private static EntityManagerFactory buildEmf() {
        // 1) Crear EMF una sola vez en la app
        return Persistence.createEntityManagerFactory(PU_NAME);
    }

    public static EntityManager em() {
        // 2) Un EM por hilo cuando se pida, reutilizable dentro del hilo
        EntityManager em = current.get();
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
            current.set(em);
        }
        return em;
    }

    public static void closeCurrent() {
        EntityManager em = current.get();
        if (em != null) {
            em.close();
            current.remove();
        }
    }

    public static void shutdown() {
        // 3) Cierre ordenado del EMF al terminar la app/tests
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
