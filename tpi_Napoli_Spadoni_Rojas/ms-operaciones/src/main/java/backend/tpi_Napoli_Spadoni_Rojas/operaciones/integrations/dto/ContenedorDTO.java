package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDTO {
    private Long id;
    private String codigo;
    private String tipo;
    private Double peso;
    private Double volumen;
    private String estado;
    private Long clienteId;
}
