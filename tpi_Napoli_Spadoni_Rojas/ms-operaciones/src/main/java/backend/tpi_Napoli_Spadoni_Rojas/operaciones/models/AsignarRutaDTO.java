package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import lombok.*;

/**
 * DTO para recibir la solicitud de asignación de ruta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarRutaDTO {

    private String tipo; // "directa" o "indirecta"
}
