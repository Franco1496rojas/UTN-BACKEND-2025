package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.Data;

@Data
public class CamionDTO {
    private Long id;
    private String dominio;
    private Double capacidadPeso;
    private Double capacidadVolumen;
    private Double costoKmBase;
    private Double consumoLitroKm;
    private Boolean disponible;
    private TransportistaDTO transportista;

    @Data
    public static class TransportistaDTO {
        private Long id;
        private String nombre;
        private String apellido;
    }
}
