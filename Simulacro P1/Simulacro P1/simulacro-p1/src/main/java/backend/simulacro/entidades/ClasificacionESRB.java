package backend.simulacro.entidades;

import java.util.Optional;

public enum ClasificacionESRB {
  E("E", "Everyone", "Apto para todo público"),
  E10Plus("E10+", "Everyone 10+", "Apto para mayores de 10 años"),
  T("T", "Teen", "Apto para adolescentes (13+)"),
  M("M", "Mature", "Apto para adultos (17+)"),
  AO("AO", "Adults Only", "Solo para adultos (18+)"),
  RP("RP", "Rating Pending", "Clasificación pendiente"),
  UR("UR", "Unrated", "Sin clasificar");

  private final String codigo;
  private final String clasificacion;
  private final String descripcion;

  ClasificacionESRB(String codigo, String clasificacion, String descripcion) {
    this.codigo = codigo;
    this.clasificacion = clasificacion;
    this.descripcion = descripcion;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getClasificacion() {
    return clasificacion;
  }

  public String getDescripcion() {
    return descripcion;
  }

  @Override
  // public String toString() {
  // return name() + " (" + codigo + "): " + clasificacion + " - " + descripcion;
  // }
  public String toString() {
    return codigo + " (" + clasificacion + ")";
  }

  // Metodo por si la ESRB en el csv viene como codigo
  public static ClasificacionESRB fromCodigo(String codigo) {
    for (ClasificacionESRB clasificacion : ClasificacionESRB.values()) {
      if (clasificacion.getCodigo().equalsIgnoreCase(codigo)) {
        return clasificacion;
      }
    }
    throw new IllegalArgumentException("Código ESRB inválido: " + codigo);
  }

  // Metodo por si la ESRB en el csv viene como clasificacio
  public static ClasificacionESRB fromClasificacion(String clasificacion) {
    for (ClasificacionESRB clasificacionESRB : ClasificacionESRB.values()) {
      if (clasificacionESRB.getClasificacion().equalsIgnoreCase(clasificacion)) {
        return clasificacionESRB;
      }
    }
    throw new IllegalArgumentException("Clasificación ESRB inválida: " + clasificacion);
  }

  public static Optional<ClasificacionESRB> findByCodigo(String codigo) {
    for (ClasificacionESRB clasificacion : ClasificacionESRB.values()) {
      if (clasificacion.getCodigo().equalsIgnoreCase(codigo)) {
        return Optional.of(clasificacion);
      }
    }
    return Optional.empty();
  }

  public static Optional<ClasificacionESRB> findByClasificacion(String clasificacion) {
    if (clasificacion == null)
      return Optional.empty();
    String c = clasificacion.trim();
    for (ClasificacionESRB e : values()) {
      if (e.getClasificacion().equalsIgnoreCase(c)) {
        return Optional.of(e);
      }
    }
    return Optional.empty();
  }

}
