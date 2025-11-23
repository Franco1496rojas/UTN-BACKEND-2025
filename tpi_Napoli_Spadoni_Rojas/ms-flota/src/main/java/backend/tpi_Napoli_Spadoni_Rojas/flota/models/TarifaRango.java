package backend.tpi_Napoli_Spadoni_Rojas.flota.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tarifas_rango_volumen_peso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaRango {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double volumenMin;

    @Column(nullable = false)
    private Double volumenMax;

    @Column(nullable = false)
    private Double pesoMin;

    @Column(nullable = false)
    private Double pesoMax;

    @Column(nullable = false)
    private Double costoKmBase;
}
