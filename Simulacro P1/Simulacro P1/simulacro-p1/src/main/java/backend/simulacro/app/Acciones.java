package backend.simulacro.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import backend.simulacro.entidades.Desarrollador;
import backend.simulacro.entidades.Juego;
import backend.simulacro.entidades.Plataforma;
import backend.simulacro.menu.ApplicationContext;
import backend.simulacro.services.DesarrolladorService;
import backend.simulacro.services.GeneroService;
import backend.simulacro.services.JuegoService;
import backend.simulacro.services.PlataformaService;

public class Acciones {
  // Firma de los metodos: void nombreMetodo(ApplicationContext ctx)

  public void cargarArchivoCsv(ApplicationContext ctx) {
    var folderPath = (URL) ctx.get("folderPath");

    try (var paths = Files.walk(Paths.get(folderPath.toURI()))) {
      var csvFiles = paths
          .filter(Files::isRegularFile) // Me quedo solo con los regulares
          .filter(path -> path.toString().endsWith(".csv")) // Me quedo solo con los .csv
          .map(path -> path.toFile()) // Convierto a File
          .toList();

      csvFiles.stream()
          .filter(file -> file.getName().contains("games_data"))
          .findFirst()
          .ifPresentOrElse(file -> {
            var service = ctx.getService(JuegoService.class);
            service.bulkInsert(file);
          }, () -> {
            throw new IllegalArgumentException("No se encontro el archivo ");
          });
    } catch (IOException | URISyntaxException ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }

  public void mostrarJuegos(ApplicationContext ctx) {
    var service = ctx.getService(JuegoService.class);
    List<Juego> juegosTodos = service.obtenerTodos();
    for (Juego juego : juegosTodos) {
      String resumenTexto = obtenerResumenComoTexto(juego.getResumen());

      System.out.println("Titulo: " + juego.getTitulo() + ", Fecha Lanzamiento: " + juego.getFechaLanzamiento() +
          ", Rating: " + juego.getRating() + ", Generos: " + juego.getGenero().getNombre() + ", Desarrollador: "
          + juego.getDesarrollador().getNombre() + ", Plataforma: " + juego.getPlataforma().getNombre()
          + ", clasiificacion: " + juego.getClasificacionESRB() + ", Resumen: " + resumenTexto + "\n");
    }
  }

  public void mostrarPlataformas(ApplicationContext ctx) {
    var service = ctx.getService(PlataformaService.class);
    List<Plataforma> plataformas = service.obtenerTodos();
    System.out.println("Plataformas disponibles:");
    for (Plataforma plataforma : plataformas) {
      System.out.println("- " + plataforma.getNombre());
    }

  }

  public void mostrarDesarrolladores(ApplicationContext ctx) {
    var service = ctx.getService(DesarrolladorService.class);
    List<Desarrollador> desarrolladores = service.obtenerTodos();
    System.out.println("Desarrolladores disponibles:");
    for (Desarrollador desarrollador : desarrolladores) {
      System.out.println("- " + desarrollador.getNombre());
    }
  }

  private String obtenerResumenComoTexto(String resumen) {
    if (resumen == null || resumen.trim().isEmpty()) {
      return "Sin resumen";
    }

    // Limpiar espacios en blanco al inicio y final
    resumen = resumen.trim();

    // Limitar a 500 caracteres para la visualización
    if (resumen.length() > 500) {
      return resumen.substring(0, 500) + "...";
    }

    return resumen;
  }

  public void verificarRelaciones(ApplicationContext ctx) {
    System.out.println("\n🔍 VERIFICANDO TODAS LAS RELACIONES BIDIRECCIONALES...\n");

    GeneroService generoService = ctx.getService(GeneroService.class);
    generoService.verificarRelaciones();

    PlataformaService plataformaService = ctx.getService(PlataformaService.class);
    plataformaService.verificarRelaciones();

    DesarrolladorService desarrolladorService = ctx.getService(DesarrolladorService.class);
    desarrolladorService.verificarRelaciones();
  }

  public void top5GenerosMasJugados(ApplicationContext ctx) {
    var service = ctx.getService(JuegoService.class);
    var top5 = service.top5GenerosMasJugados();

    // Mostrar los resultados
    System.out.println("\n=== TOP 5 GÉNEROS MÁS JUGADOS ===");
    System.out.println("Posición | Género                | Total Jugadores");
    System.out.println("---------|----------------------|----------------");

    if (top5.isEmpty()) {
      System.out.println("No hay datos de géneros disponibles.");
      return;
    }

    int posicion = 1;
    for (Map.Entry<String, Integer> entry : top5) {
      System.out.printf("%8d | %-20s | %,14d%n",
          posicion++,
          entry.getKey(),
          entry.getValue());
    }

    System.out.println("=========================================");
  }

  public void mostrarJuegosPorDesarrollador(ApplicationContext ctx) {
    var service = ctx.getService(JuegoService.class);
    var analisis = service.obtenerJuegosPorDesarrollador();

    // Mostrar desarrolladores con más de 30 juegos
    System.out.println("\n=== DESARROLLADORES CON MÁS DE 30 JUEGOS ===");
    System.out.println("Posición | Desarrollador                     | Cantidad de Juegos");
    System.out.println("---------|----------------------------------|-------------------");

    if (analisis.desarrolladoresCon30Plus.isEmpty()) {
      System.out.println("No hay desarrolladores con más de 30 juegos.");
    } else {
      int posicion = 1;
      for (Map.Entry<String, Long> entry : analisis.desarrolladoresCon30Plus) {
        System.out.printf("%8d | %-32s | %,17d%n",
            posicion++,
            entry.getKey(),
            entry.getValue());
      }
    }

    System.out.println("=================================================");

    // Mostrar cantidad de juegos con múltiples desarrolladores
    System.out.println("\n=== ESTADÍSTICAS ADICIONALES ===");
    System.out.printf("Juegos con múltiples desarrolladores en CSV: %,d%n",
        analisis.juegosConMultiplesDesarrolladores);
    System.out.println("=====================================");
  }

  public void desarrolladorMejorValorado(ApplicationContext ctx) {
    var service = ctx.getService(JuegoService.class);
    var mejorDesarrollador = service.obtenerDesarrolladorMejorValorado();

    System.out.println("\n=== DESARROLLADOR MEJOR VALORADO (mínimo 5 juegos) ===");

    if (mejorDesarrollador == null) {
      System.out.println("No hay desarrolladores con al menos 5 juegos que tengan rating.");
    } else {
      System.out.printf("Desarrollador: %s%n", mejorDesarrollador.nombre);
      System.out.printf("Rating Promedio: %.2f%n", mejorDesarrollador.ratingPromedio);
      System.out.printf("Cantidad de Juegos: %,d%n", mejorDesarrollador.cantidadJuegos);
    }

    System.out.println("=====================================================");
  }

}
