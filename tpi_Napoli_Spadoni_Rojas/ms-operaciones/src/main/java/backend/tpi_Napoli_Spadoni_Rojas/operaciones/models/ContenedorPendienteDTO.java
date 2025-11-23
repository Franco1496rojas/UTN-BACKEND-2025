package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorPendienteDTO {
    private Long contenedorId;
    private Long solicitudId;
    private Estado estadoActual;
    private String origen;
    private String destino;
    private String ubicacionActual; // Texto descriptivo
    private String tipoUbicacion;   // ORIGEN | DEPOSITO | EN_TRANSITO | DESTINO | DESCONOCIDA
    private Integer tramoOrdenActual; // Orden del tramo en curso (si aplica)
}
