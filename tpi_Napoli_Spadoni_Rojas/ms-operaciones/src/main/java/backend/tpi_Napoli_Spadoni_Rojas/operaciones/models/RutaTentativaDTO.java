package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import lombok.*;
import java.util.List;

/**
 * DTO para representar una ruta tentativa con sus tramos estimados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaTentativaDTO {

    private String nombre; // Ej: "Ruta Directa", "Ruta por Depósito X"
    private String descripcion;
    private List<TramoEstimadoDTO> tramos;

    // Totales de la ruta
    private Double distanciaTotalKm;
    private Double costoTotalEstimado;
    private Integer tiempoTotalMinutos;
}