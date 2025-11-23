package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositoDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String coordenadas; // Formato: "lat, lon"
    private Double costoEstadiaDiaria; // Costo por día de estadía en el depósito
}
