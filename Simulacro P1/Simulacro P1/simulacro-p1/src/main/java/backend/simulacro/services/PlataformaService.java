package backend.simulacro.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.simulacro.entidades.Plataforma;
import backend.simulacro.repositories.JpaPlataformaRepository;

public class PlataformaService {
  private final JpaPlataformaRepository plataformaRepo;
  private final Map<String, Plataforma> plataformas;

  public PlataformaService() {
    this.plataformaRepo = new JpaPlataformaRepository();
    this.plataformas = new HashMap<>();
  }

  public Plataforma getOrCreate(String nombre) {
    return plataformas.computeIfAbsent(nombre, n -> {
      // Crear nueva entidad
      Plataforma plataforma = new Plataforma();
      plataforma.setNombre(n);

      return plataformaRepo.saveIfNotExists(plataforma, n);
    });
  }

  public List<Plataforma> obtenerTodos() {
    return plataformaRepo.findAll();
  }

  // Método para verificar que las colecciones se llenan automáticamente
  public void verificarRelaciones() {
    var todasPlataformas = plataformaRepo.findAll();
    System.out.println("\n=== VERIFICACIÓN DE PLATAFORMAS ===");

    for (Plataforma plataforma : todasPlataformas) {
      System.out.println("Plataforma: " + plataforma.getNombre() +
          " - Juegos: " + plataforma.getJuegos().size());

      // Mostrar algunos títulos de juegos
      plataforma.getJuegos().stream()
          .limit(3)
          .forEach(juego -> System.out.println("  - " + juego.getTitulo()));
    }
  }
}
