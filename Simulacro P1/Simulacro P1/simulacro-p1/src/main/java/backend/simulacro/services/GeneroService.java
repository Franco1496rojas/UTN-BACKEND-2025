package backend.simulacro.services;

import java.util.HashMap;
import java.util.Map;

import backend.simulacro.entidades.Genero;
import backend.simulacro.repositories.JpaGeneroRepository;

public class GeneroService {
  private final JpaGeneroRepository generoRepo;
  private final Map<String, Genero> generos;

  public GeneroService() {
    this.generoRepo = new JpaGeneroRepository();
    this.generos = new HashMap<>();
  }

  public Genero getOrCreate(String nombre) {
    return generos.computeIfAbsent(nombre, nom -> {
      // Crear nueva entidad
      Genero genero = new Genero();
      genero.setNombre(nom);
      
      // El repositorio se encarga de verificar duplicados y manejar transacciones
      return generoRepo.saveIfNotExists(genero, nom);
    });
  }

  // Método para verificar que las colecciones se llenan automáticamente
  public void verificarRelaciones() {
    var todosGeneros = generoRepo.findAll();
    System.out.println("\n=== VERIFICACIÓN DE RELACIONES ===");

    for (Genero genero : todosGeneros) {
      System.out.println("Género: " + genero.getNombre() +
          " - Juegos: " + genero.getJuegos().size());

      // Mostrar algunos títulos de juegos
      genero.getJuegos().stream()
          .limit(3)
          .forEach(juego -> System.out.println("  - " + juego.getTitulo()));
    }
  }

}
