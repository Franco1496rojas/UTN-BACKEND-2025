package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import lombok.*;
import java.util.List;

/**
 * DTO para recibir la ruta seleccionada por el usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeleccionRutaDTO {

    private String nombreRuta; // Identificador de la ruta seleccionada
    private List<TramoEstimadoDTO> tramos; // Lista de tramos a crear

    // Totales de la ruta seleccionada
    private Double distanciaTotalKm;
    private Double costoTotalEstimado;
    private Integer tiempoTotalMinutos;
}