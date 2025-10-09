package backend.simulacro.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class LocalEntityManagerProvider {
  private final EntityManager em;
  // Instnancia unica del proveedor de EntityManager
  public static LocalEntityManagerProvider INSTANCE = null;

  // Singleton. Crea una unica instancia de EntityManager
  private LocalEntityManagerProvider() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("juegos");
    this.em = emf.createEntityManager();
  }

  // Si no existe la instancia, la crea. Si existe, la devuelve.
  public static LocalEntityManagerProvider getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new LocalEntityManagerProvider();
    }
    return INSTANCE;
  }

  public EntityManager getManager() {
    return this.em;
  }
}
