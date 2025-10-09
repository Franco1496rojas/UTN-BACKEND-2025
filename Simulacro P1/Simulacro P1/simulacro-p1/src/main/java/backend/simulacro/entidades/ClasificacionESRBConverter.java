package backend.simulacro.entidades;

import jakarta.persistence.AttributeConverter;

public class ClasificacionESRBConverter implements AttributeConverter<ClasificacionESRB, String> {

  @Override
  public String convertToDatabaseColumn(ClasificacionESRB attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.getCodigo();
  }

  @Override
  public ClasificacionESRB convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return null;
    }
    // Intenta convertir usando el código primero
    try {
      return ClasificacionESRB.fromCodigo(dbData);
    } catch (IllegalArgumentException ex) {
      // Si falla, intenta convertir usando la clasificación
      return ClasificacionESRB.fromClasificacion(dbData);
    }
  }
}
