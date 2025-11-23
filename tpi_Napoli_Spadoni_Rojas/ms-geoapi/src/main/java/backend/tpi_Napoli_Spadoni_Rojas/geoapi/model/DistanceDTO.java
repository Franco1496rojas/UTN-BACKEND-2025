package backend.tpi_Napoli_Spadoni_Rojas.geoapi.model;

import lombok.Data;

@Data
public class DistanceDTO {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
}
