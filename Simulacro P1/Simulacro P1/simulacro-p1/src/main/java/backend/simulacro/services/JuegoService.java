package backend.simulacro.services;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import backend.simulacro.entidades.ClasificacionESRB;
import backend.simulacro.entidades.Desarrollador;
import backend.simulacro.entidades.Genero;
import backend.simulacro.entidades.Juego;
import backend.simulacro.entidades.Plataforma;
import backend.simulacro.repositories.JpaJuegoRepository;

public class JuegoService {
  private final JpaJuegoRepository juegoRepo;
  private final PlataformaService plataformaService;
  private final GeneroService generoService;
  private final DesarrolladorService desarrolladorService;

  // Contador para juegos con múltiples desarrolladores en el CSV
  private int juegosConMultiplesDesarrolladores = 0;

  public JuegoService() {
    this.juegoRepo = new JpaJuegoRepository();
    this.plataformaService = new PlataformaService();
    this.generoService = new GeneroService();
    this.desarrolladorService = new DesarrolladorService();
  }

  public void bulkInsert(File file) {
    // Resetear contador al inicio de la carga
    juegosConMultiplesDesarrolladores = 0;

    try {
      // Crear parser con punto y coma como separador
      CSVParser parser = new CSVParserBuilder()
          .withSeparator(';')
          .build();

      // Crear reader con el parser personalizado
      CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
          .withCSVParser(parser)
          .build();

      // Leer headers manualmente
      String[] headers = csvReader.readNext();
      if (headers == null) {
        System.out.println("No se encontraron headers en el archivo CSV");
        return;
      }

      System.out.println("Headers encontrados: " + String.join(", ", headers));

      // Procesar cada línea
      String[] valores;
      int lineNumber = 1;

      while ((valores = csvReader.readNext()) != null) {
        lineNumber++;

        // Declarar titulo fuera del try para uso en catch
        String titulo = "línea desconocida";

        try {
          // Crear mapa de header -> valor
          Map<String, String> linea = new java.util.HashMap<>();
          for (int i = 0; i < Math.min(headers.length, valores.length); i++) {
            linea.put(headers[i].trim(), valores[i]);
          }

          titulo = linea.get("Title");

          // Skip si no tiene título
          if (titulo == null || titulo.trim().isEmpty()) {
            System.out.println("Saltando línea " + lineNumber + " - sin título");
            continue;
          }

          // Verificar si el juego ya existe por título
          if (!this.juegoRepo.existsByTitulo(titulo)) {
            Juego juegoNuevo = this.procesarLinea(linea);
            this.juegoRepo.save(juegoNuevo);
            System.out.println("Juego guardado: " + titulo);
          } else {
            System.out.println("Juego ya existe: " + titulo);
          }
        } catch (IllegalArgumentException e) {
          System.out.println("Saltando línea " + lineNumber + " (" + titulo + "): " + e.getMessage());
        } catch (Exception e) {
          System.err.println("Error procesando línea " + lineNumber + " (" + titulo + "): " + e.getMessage());
          e.printStackTrace();
          // Continuar con la siguiente línea en lugar de fallar completamente
        }
      }

      csvReader.close();

    } catch (IOException | CsvValidationException ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }

  private Juego procesarLinea(Map<String, String> linea) {
    Juego juego = new Juego();

    // Título (obligatorio)
    String titulo = linea.get("Title");
    if (titulo == null || titulo.trim().isEmpty()) {
      throw new IllegalArgumentException("Título no puede estar vacío");
    }
    juego.setTitulo(titulo.trim());

    // Fecha de lanzamiento
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    String dateString = linea.get("Release_Date");
    if (dateString != null && !dateString.equals("TBD") && !dateString.trim().isEmpty()) {
      try {
        juego.setFechaLanzamiento(formatter.parse(dateString.trim()).getTime());
      } catch (Exception e) {
        System.out.println("Error parseando fecha para \"" + titulo + "\": " + dateString);
        // Dejar fecha como null si no se puede parsear
      }
    }

    // Clasificación ESRB
    String clasificacionRaw = linea.get("esrb_rating");
    ClasificacionESRB cat = Optional.ofNullable(clasificacionRaw)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .flatMap(c -> ClasificacionESRB.findByCodigo(c).or(() -> ClasificacionESRB.findByClasificacion(c)))
        .orElse(ClasificacionESRB.UR);
    juego.setClasificacionESRB(cat);

    juego.setRating(procesarCampoRating(linea.get("Rating")));
    juego.setJuegosFinalizados(procesarCampoNumerico(linea.get("Plays")));
    juego.setJugando(procesarCampoNumerico(linea.get("Playing")));
    juego.setResumen(linea.get("Summary"));

    // ---- VALIDACIONES OBLIGATORIAS ---- //
    // Verificar si hay múltiples desarrolladores en el CSV
    String developersField = linea.get("Developers");
    if (tieneMultiplesValores(developersField)) {
      juegosConMultiplesDesarrolladores++;
    }

    // Validar que tenga al menos un desarrollador
    String nomDesarrollador = procesarCampoArray(developersField);
    if (nomDesarrollador == null) {
      throw new IllegalArgumentException("El juego debe tener al menos un desarrollador");
    }

    // Validar que tenga al menos un género
    String nomGenero = procesarCampoArray(linea.get("Genres"));
    if (nomGenero == null) {
      throw new IllegalArgumentException("El juego debe tener al menos un género");
    }

    // ---- RELACIONES ---- //
    // Crear relación con género (ya validado)
    Genero genero = generoService.getOrCreate(nomGenero);
    genero.añadirJuego(juego);

    // Manejar plataforma: usar cadena vacía si no hay valor
    String nomPlataforma = procesarCampoArray(linea.get("Platforms"));
    if (nomPlataforma == null) {
      nomPlataforma = ""; // Usar cadena vacía en lugar de null para plataforma
    }
    Plataforma plataforma = plataformaService.getOrCreate(nomPlataforma);
    plataforma.añadirJuego(juego);

    // Crear relación con desarrollador (ya validado)
    Desarrollador desarrollador = desarrolladorService.getOrCreate(nomDesarrollador);
    desarrollador.añadirJuego(juego);

    return juego;
  }

  public List<Juego> obtenerTodos() {
    return juegoRepo.findAll();
  }

  /**
   * Verifica si un campo del CSV tiene múltiples valores
   * 
   * @param csvArrayField Campo del CSV en formato ['valor1', 'valor2'] o []
   * @return true si tiene más de un valor, false en caso contrario
   */
  private boolean tieneMultiplesValores(String csvArrayField) {
    if (csvArrayField == null || csvArrayField.trim().isEmpty()) {
      return false;
    }

    String field = csvArrayField.trim();

    // Si es [] o está vacío
    if (field.equals("[]") || field.equals("")) {
      return false;
    }

    // Contar las comillas simples para determinar número de valores
    int quoteCount = 0;
    for (int i = 0; i < field.length(); i++) {
      if (field.charAt(i) == '\'') {
        quoteCount++;
      }
    }

    // Si hay más de 2 comillas (más de un par), entonces hay múltiples valores
    return quoteCount > 2;
  }

  /**
   * Procesa un campo del CSV que viene en formato ['valor'] o [] y extrae el
   * primer valor.
   * 
   * @param csvArrayField Campo del CSV en formato ['valor1', 'valor2'] o []
   * @return El primer valor sin corchetes ni comillas, o null si está vacío
   */
  private String procesarCampoArray(String csvArrayField) {
    if (csvArrayField == null || csvArrayField.trim().isEmpty()) {
      return null;
    }

    String field = csvArrayField.trim();

    // Si es [] o está vacío
    if (field.equals("[]") || field.equals("")) {
      return null;
    }

    // Intentar extraer contenido entre comillas simples, aceptando escapes:
    // - soporta apóstrofes escapados con backslash: 'O\'Reilly'
    // - soporta apóstrofe doble como escape: 'O''Reilly'
    // Patrón: captura el primer grupo entre comillas simples
    Pattern singleQuotePattern = Pattern.compile("'((?:\\\\'|''|[^'])*)'");
    Matcher m = singleQuotePattern.matcher(field);
    if (m.find()) {
      String raw = m.group(1);
      // Unescape backslash-escaped and doubled-apostrophe forms
      raw = raw.replace("\\'", "'").replace("''", "'");
      return raw;
    }

    // Si no hay comillas simples válidas, intentar comillas dobles como fallback
    // Fallback: extraer contenido entre la primera pareja de comillas dobles (sin
    // soporte complejo de escapes)
    Pattern doubleQuotePattern = Pattern.compile("\"([^\"]*)\"");
    m = doubleQuotePattern.matcher(field);
    if (m.find()) {
      String raw = m.group(1);
      raw = raw.replace("\\\"", "\"");
      return raw;
    }

    return null;
  }

  /**
   * Procesa un campo numérico que puede venir con sufijos como 'K' o ser 'N/A'
   * 
   * @param numericField Campo que puede ser "4.3K", "147", "N/A", etc.
   * @return El valor numérico convertido, o 0 si no es válido
   */
  private int procesarCampoNumerico(String numericField) {
    if (numericField == null || numericField.trim().isEmpty() || numericField.equals("N/A")) {
      return 0;
    }

    String field = numericField.trim();

    try {
      if (field.endsWith("K")) {
        // Convertir "4.3K" a 4300
        double value = Double.parseDouble(field.substring(0, field.length() - 1));
        return (int) (value * 1000);
      } else {
        return Integer.parseInt(field);
      }
    } catch (NumberFormatException e) {
      System.out.println("Error procesando campo numérico: " + field);
      return 0;
    }
  }

  /**
   * Procesa un campo de rating que puede ser "N/A" o un número
   * 
   * @param ratingField Campo de rating del CSV
   * @return El valor del rating como Double, o 0.0 si es "N/A"
   */
  private Double procesarCampoRating(String ratingField) {
    if (ratingField == null || ratingField.trim().isEmpty() || ratingField.equals("N/A")) {
      return 0.0;
    }

    try {
      return Double.parseDouble(ratingField.trim());
    } catch (NumberFormatException e) {
      System.out.println("Error procesando rating: " + ratingField);
      return 0.0;
    }
  }

  public List<Map.Entry<String, Integer>> top5GenerosMasJugados() {
    List<Juego> todosLosJuegos = this.juegoRepo.findAll();

    // Agrupar por género y sumar los valores de "jugando" para cada género
    Map<String, Integer> generosPorJugadores = todosLosJuegos.stream()
        .collect(Collectors.groupingBy(
            juego -> juego.getGenero() != null ? juego.getGenero().getNombre() : "Sin género",
            Collectors.summingInt(Juego::getJugando)));

    // Obtener el top 5 géneros más jugados (ordenar por suma descendente y limitar
    // a 5)
    return generosPorJugadores.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(5)
        .collect(Collectors.toList());
  }

  /**
   * Clase para encapsular los resultados del análisis de desarrolladores
   */
  public static class AnalisisDesarrolladores {
    public final List<Map.Entry<String, Long>> desarrolladoresCon30Plus;
    public final int juegosConMultiplesDesarrolladores;

    public AnalisisDesarrolladores(List<Map.Entry<String, Long>> desarrolladoresCon30Plus,
        int juegosConMultiplesDesarrolladores) {
      this.desarrolladoresCon30Plus = desarrolladoresCon30Plus;
      this.juegosConMultiplesDesarrolladores = juegosConMultiplesDesarrolladores;
    }
  }

  /**
   * Clase para encapsular la información del desarrollador mejor valorado
   */
  public static class DesarrolladorMejorValorado {
    public final String nombre;
    public final double ratingPromedio;
    public final long cantidadJuegos;

    public DesarrolladorMejorValorado(String nombre, double ratingPromedio, long cantidadJuegos) {
      this.nombre = nombre;
      this.ratingPromedio = ratingPromedio;
      this.cantidadJuegos = cantidadJuegos;
    }
  }

  public AnalisisDesarrolladores obtenerJuegosPorDesarrollador() {
    List<Juego> todosLosJuegos = this.juegoRepo.findAll();

    // Agrupar por desarrollador y contar
    Map<String, Long> juegosPorDesarrollador = todosLosJuegos.stream()
        .filter(juego -> juego.getDesarrollador() != null)
        .collect(Collectors.groupingBy(
            juego -> juego.getDesarrollador().getNombre(),
            Collectors.counting()));

    List<Map.Entry<String, Long>> desarrolladoresCon30Plus = juegosPorDesarrollador.entrySet().stream()
        .filter(entry -> entry.getValue() > 30)
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .collect(Collectors.toList());

    return new AnalisisDesarrolladores(desarrolladoresCon30Plus, juegosConMultiplesDesarrolladores);
  }

  public DesarrolladorMejorValorado obtenerDesarrolladorMejorValorado() {
    List<Juego> todosLosJuegos = this.juegoRepo.findAll();

    Map<String, List<Juego>> juegosPorDesarrollador = todosLosJuegos.stream()
        .filter(j -> j.getDesarrollador() != null)
        .filter(j -> j.getRating() != null && j.getRating() > 0)
        .collect(Collectors.groupingBy(j -> j.getDesarrollador().getNombre()));

    // Calcular promedio y seleccionar el mejor.
    // En caso de empate por promedio, desempatar por mayor cantidad de juegos,
    // luego por nombre (alfabético).
    return juegosPorDesarrollador.entrySet().stream()
        .map(entry -> {
          String nombre = entry.getKey();
          List<Juego> lista = entry.getValue();
          double promedio = lista.stream().mapToDouble(Juego::getRating).average().orElse(0.0);
          long cantidad = lista.size();
          return new java.util.AbstractMap.SimpleEntry<>(nombre, new double[] { promedio, (double) cantidad });
        })
        .max((e1, e2) -> {
          // comparar por promedio
          int cmp = Double.compare(e1.getValue()[0], e2.getValue()[0]);
          if (cmp != 0)
            return cmp;
          // empate: comparar por cantidad de juegos
          cmp = Double.compare(e1.getValue()[1], e2.getValue()[1]);
          if (cmp != 0)
            return cmp;
          // empate final: comparar por nombre inverso para que max devuelva el
          // alfabéticamente mayor
          return e1.getKey().compareTo(e2.getKey());
        })
        .map(best -> new DesarrolladorMejorValorado(
            best.getKey(),
            best.getValue()[0],
            (long) best.getValue()[1]))
        .orElse(null);
  }
}
