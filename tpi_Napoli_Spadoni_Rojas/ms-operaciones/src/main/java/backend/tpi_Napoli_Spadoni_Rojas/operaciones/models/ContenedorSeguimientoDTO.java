package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContenedorSeguimientoDTO {
    private Long contenedorId;
    private Long solicitudId;
    private Estado estadoActual;
    private List<CambioEstadoSolicitud> historial; // Ordenado asc por fechaCambio
}
