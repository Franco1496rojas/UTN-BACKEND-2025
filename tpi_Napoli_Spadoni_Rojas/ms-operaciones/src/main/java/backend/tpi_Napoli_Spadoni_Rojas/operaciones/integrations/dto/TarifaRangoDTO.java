package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.Data;

@Data
public class TarifaRangoDTO {
    private Long id;
    private Double volumenMin;
    private Double volumenMax;
    private Double pesoMin;
    private Double pesoMax;
    private Double costoKmBase;
    private Double costoKm; // Alias para compatibilidad
    public Double getCostoKm() {
        return costoKm != null ? costoKm : costoKmBase;
    }
}
