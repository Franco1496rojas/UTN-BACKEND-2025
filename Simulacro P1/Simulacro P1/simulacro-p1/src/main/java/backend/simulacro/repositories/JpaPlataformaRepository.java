package backend.simulacro.repositories;

import backend.simulacro.entidades.Plataforma;
import backend.simulacro.repositories.base.JpaBaseRepository;
import backend.simulacro.repositories.interfaces.IPlataformaRepository;

public class JpaPlataformaRepository extends JpaBaseRepository<Plataforma, Integer>
    implements IPlataformaRepository {

  public JpaPlataformaRepository() {
    super(Plataforma.class);
  }

}
