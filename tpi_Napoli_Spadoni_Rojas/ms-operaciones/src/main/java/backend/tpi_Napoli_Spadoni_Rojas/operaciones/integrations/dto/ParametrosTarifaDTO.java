package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.Data;

@Data
public class ParametrosTarifaDTO {
    private Long id;
    private Double precioLitroCombustible;
    private Double cargoFijoTramo;
    private Double costoBase;
    private Double velocidadPromedio;
}
