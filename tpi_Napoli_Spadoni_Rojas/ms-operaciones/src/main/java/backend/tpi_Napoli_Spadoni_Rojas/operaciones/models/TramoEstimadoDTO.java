package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import lombok.*;

/**
 * DTO para representar un tramo estimado dentro de una ruta tentativa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoEstimadoDTO {

    private String origen;
    private String destino;
    private Double distanciaKm;
    private Double costoEstimado;
    private Integer duracionMinutos;

    // IDs de depósitos si aplica
    private Long depositoOrigenId;
    private Long depositoDestinoId;

    // Tipo de tramo
    private String tipo; // ORIGEN_DESTINO, ORIGEN_DEPOSITO, DEPOSITO_DESTINO, DEPOSITO_DEPOSITO
}