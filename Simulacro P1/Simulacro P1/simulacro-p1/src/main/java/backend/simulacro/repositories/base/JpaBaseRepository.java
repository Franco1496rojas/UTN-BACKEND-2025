package backend.simulacro.repositories.base;

import java.util.List;
import java.util.Optional;

import backend.simulacro.config.LocalEntityManagerProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class JpaBaseRepository<T, ID> implements IBaseRepository<T, ID> {
  private final Class<T> entityClass;
  protected final EntityManager em;

  public JpaBaseRepository(Class<T> entityClass) {
    this.entityClass = entityClass;
    this.em = LocalEntityManagerProvider.getInstance().getManager();
  }

  // Metodo de save (insert o update)
  @Override
  public T save(T entity) {
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      T mergedEntity = em.merge(entity);
      tx.commit();
      return mergedEntity;
    } catch (RuntimeException e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    }
  }

  // Metodo de busqueda por ID de la entidad
  @Override
  public Optional<T> findById(ID id) {
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      T entity = em.find(entityClass, id);
      tx.commit();
      // Si encuentra, el Optional contiene el objeto, sino esta vacio (null)
      return Optional.ofNullable(entity);
    } catch (RuntimeException e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    }
  }

  @Override
  public Optional<T> findByName(String name) {
    try {
      String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.nombre = :name";
      List<T> results = em.createQuery(jpql, entityClass)
          .setParameter("name", name)
          .getResultList();

      // Si hay resultados, devolver el primero; si no, Optional vacío
      return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    } catch (Exception e) {
      // En caso de cualquier error, devolver Optional vacío
      return Optional.empty();
    }
  }

  // Busca y devuelve todos los registros de la entidad T
  @Override
  public List<T> findAll() {
    String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
    return em.createQuery(jpql, entityClass).getResultList();
  }

  @Override
  public List<T> findAll(int offset, int limit) {
    String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
    return em.createQuery(jpql, entityClass)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  // Metodo para eliminar una entidad por su ID
  @Override
  public void deleteById(ID id) {
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      T entity = em.find(entityClass, id);
      if (entity != null) {
        em.remove(entity);
      }
      tx.commit();
    } catch (RuntimeException e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    }
  }

  // Metodo para eliminar una entidad
  @Override
  public void delete(T entity) {
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      // Si el em no esta manejando la entidad, hace un merge para gestionarla
      em.remove(em.contains(entity) ? entity : em.merge(entity));
      tx.commit();
    } catch (RuntimeException e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    }
  }

  // Metodo para verificar si una entidad existe por su ID
  @Override
  public boolean existsById(ID id) {
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();
      boolean exists = em.find(entityClass, id) != null;
      tx.commit();
      return exists;
    } catch (RuntimeException e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    }
  }

  public boolean existsByName(String name) {
    return findByName(name).isPresent();
  }

  // Metodo para contar el numero de registros de la entidad
  @Override
  public long count() {
    String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
    return em.createQuery(jpql, Long.class).getSingleResult();
  }

  // Método para guardar solo si no existe (busca primero por nombre)
  public T saveIfNotExists(T entity, String nombre) {
    EntityTransaction tx = em.getTransaction();
    try {
      tx.begin();

      // Buscar si ya existe
      Optional<T> existente = findByNameInTransaction(nombre);
      if (existente.isPresent()) {
        tx.commit();
        return existente.get();
      }

      // Si no existe, persistir
      em.persist(entity);
      tx.commit();
      return entity;

    } catch (RuntimeException e) {
      if (tx.isActive())
        tx.rollback();
      throw e;
    }
  }

  // Método auxiliar para buscar por nombre dentro de una transacción
  private Optional<T> findByNameInTransaction(String name) {
    try {
      String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e.nombre = :name";
      List<T> results = em.createQuery(jpql, entityClass)
          .setParameter("name", name)
          .getResultList();

      return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

}
