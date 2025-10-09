package backend.simulacro.repositories.base;

import java.util.List;
import java.util.Optional;

// T = Tipo de entidad
// ID = Tipo de la clave primaria
public interface IBaseRepository<T, ID> {
  T save(T entity);

  Optional<T> findById(ID id);

  Optional<T> findByName(String name);

  List<T> findAll();

  List<T> findAll(int offset, int limit);

  void deleteById(ID id);

  void delete(T entity);

  boolean existsById(ID id);

  boolean existsByName(String name);

  long count();

  // Método para guardar solo si no existe por nombre
  T saveIfNotExists(T entity, String nombre);
}
