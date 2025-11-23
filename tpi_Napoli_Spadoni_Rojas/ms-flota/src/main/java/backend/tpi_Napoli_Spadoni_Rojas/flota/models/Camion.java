package backend.tpi_Napoli_Spadoni_Rojas.flota.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dominio;

    @Column(nullable = false)
    private Double capacidadPeso;

    @Column(nullable = false)
    private Double capacidadVolumen;

    @Column(nullable = false)
    private Boolean disponibilidad;

    @Column(nullable = false)
    private Double costoKmBase;

    @Column(nullable = false)
    private Double consumoLitroKm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Transportista transportista;
}
