package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarSolicitudDTO {

    // Datos del cliente
    private String clienteDni;
    private String clienteEmail;
    private String clienteNombre;
    private String clienteTelefono;

    // Datos del contenedor
    private Double contenedorPeso;
    private Double contenedorVolumen;
    private String contenedorCodigo;
    private String contenedorTipo;

    // Datos de la solicitud
    private String origen;
    private String destino;

    // Observaciones opcionales
    private String observaciones;
}