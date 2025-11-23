package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    @JsonIgnore
    private Solicitud solicitud;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFinEstimada;

    @Column(nullable = false)
    private Double distanciaTotalKm;

    @Column(nullable = false)
    private Double costoTotal;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties(value = { "ruta", "hibernateLazyInitializer", "handler" }, allowSetters = true)
    private List<Tramo> tramos;
}
