package backend.simulacro.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.simulacro.entidades.Desarrollador;
import backend.simulacro.repositories.JpaDesarrolladorRepository;

public class DesarrolladorService {
  private final JpaDesarrolladorRepository desarrolladorRepo;
  private final Map<String, Desarrollador> desarrolladores;

  public DesarrolladorService() {
    this.desarrolladorRepo = new JpaDesarrolladorRepository();
    this.desarrolladores = new HashMap<>();
  }

  public Desarrollador getOrCreate(String nomDesarrollador) {
    return desarrolladores.computeIfAbsent(nomDesarrollador, n -> {
      // Crear nueva entidad
      Desarrollador desarrollador = new Desarrollador();
      desarrollador.setNombre(n);

      // El repositorio se encarga de verificar duplicados y manejar transacciones
      return desarrolladorRepo.saveIfNotExists(desarrollador, n);
    });
  }

  public List<Desarrollador> obtenerTodos() {
    return desarrolladorRepo.findAll();
  }

  // Método para verificar que las colecciones se llenan automáticamente
  public void verificarRelaciones() {
    var todosDesarrolladores = desarrolladorRepo.findAll();
    System.out.println("\n=== VERIFICACIÓN DE DESARROLLADORES ===");

    for (Desarrollador desarrollador : todosDesarrolladores) {
      System.out.println("Desarrollador: " + desarrollador.getNombre() +
          " - Juegos: " + desarrollador.getJuegos().size());

      // Mostrar algunos títulos de juegos
      desarrollador.getJuegos().stream()
          .limit(3)
          .forEach(juego -> System.out.println("  - " + juego.getTitulo()));
    }
  }

}
