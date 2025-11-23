package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.Data;

@Data
public class DistanciaDTO {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
}
