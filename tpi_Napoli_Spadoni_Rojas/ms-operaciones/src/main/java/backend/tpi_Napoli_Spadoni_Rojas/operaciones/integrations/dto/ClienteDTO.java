package backend.tpi_Napoli_Spadoni_Rojas.operaciones.integrations.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
    private Long id;
    private String dni;
    private String email;
    private String nombre;
    private String telefono;
    private String direccion;
}
