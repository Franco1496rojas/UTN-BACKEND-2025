package backend.tpi_Napoli_Spadoni_Rojas.flota.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parametros_tarifa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametrosTarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double precioLitroCombustible;

    @Column(nullable = false)
    private Double cargoFijoTramo;
}
