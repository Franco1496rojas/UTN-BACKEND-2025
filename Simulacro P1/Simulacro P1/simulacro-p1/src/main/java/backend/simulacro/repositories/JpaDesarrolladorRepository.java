package backend.simulacro.repositories;

import backend.simulacro.entidades.Desarrollador;
import backend.simulacro.repositories.base.JpaBaseRepository;
import backend.simulacro.repositories.interfaces.IDesarrolladorRepository;

public class JpaDesarrolladorRepository extends JpaBaseRepository<Desarrollador, Integer>
    implements IDesarrolladorRepository {

  public JpaDesarrolladorRepository() {
    super(Desarrollador.class);
  }
}
