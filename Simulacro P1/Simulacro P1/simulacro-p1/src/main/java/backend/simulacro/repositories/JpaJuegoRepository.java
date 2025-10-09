package backend.simulacro.repositories;

import java.util.List;
import java.util.Optional;

import backend.simulacro.entidades.Juego;
import backend.simulacro.repositories.base.JpaBaseRepository;
import backend.simulacro.repositories.interfaces.IJuegoRepository;

public class JpaJuegoRepository extends JpaBaseRepository<Juego, Integer>
    implements IJuegoRepository {

  public JpaJuegoRepository() {
    super(Juego.class);
  }

  public List<Juego> findByGenero(String genero) {
    String jpql = "SELECT j FROM Juego j WHERE j.genero = :genero";
    return em.createQuery(jpql, Juego.class)
        .setParameter("genero", genero)
        .getResultList();
  }

  public List<Juego> findByGeneroPag(String genero, int offset, int limit) {
    String jpql = "SELECT j FROM Juego j WHERE j.genero = :genero";
    return em.createQuery(jpql, Juego.class)
        .setParameter("genero", genero)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<Juego> findByDesarrollador(String desarrollador) {
    String jpql = "SELECT j FROM Juego j WHERE j.desarrollador = :desarrollador";
    return em.createQuery(jpql, Juego.class)
        .setParameter("desarrollador", desarrollador)
        .getResultList();
  }

  public List<Juego> findByDesarrolladorPag(String desarrollador, int offset, int limit) {
    String jpql = "SELECT j FROM Juego j WHERE j.desarrollador = :desarrollador";
    return em.createQuery(jpql, Juego.class)
        .setParameter("desarrollador", desarrollador)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  public List<Juego> findByPlataforma(String plataforma) {
    String jpql = "SELECT j FROM Juego j WHERE j.plataforma = :plataforma";
    return em.createQuery(jpql, Juego.class)
        .setParameter("plataforma", plataforma)
        .getResultList();
  }

  public List<Juego> findByPlataformaPag(String plataforma, int offset, int limit) {
    String jpql = "SELECT j FROM Juego j WHERE j.plataforma = :plataforma";
    return em.createQuery(jpql, Juego.class)
        .setParameter("plataforma", plataforma)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  // Método específico para buscar juegos por título
  public Optional<Juego> findByTitulo(String titulo) {
    try {
      String jpql = "SELECT j FROM Juego j WHERE j.titulo = :titulo";
      List<Juego> results = em.createQuery(jpql, Juego.class)
          .setParameter("titulo", titulo)
          .getResultList();

      return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // Método para verificar si existe un juego por título
  public boolean existsByTitulo(String titulo) {
    return findByTitulo(titulo).isPresent();
  }
}
