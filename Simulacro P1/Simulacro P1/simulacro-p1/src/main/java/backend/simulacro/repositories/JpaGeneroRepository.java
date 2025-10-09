package backend.simulacro.repositories;

import backend.simulacro.entidades.Genero;
import backend.simulacro.repositories.base.JpaBaseRepository;
import backend.simulacro.repositories.interfaces.IGeneroRepository;

public class JpaGeneroRepository extends JpaBaseRepository<Genero, Integer>
    implements IGeneroRepository {

  public JpaGeneroRepository() {
    super(Genero.class);
  }

}
