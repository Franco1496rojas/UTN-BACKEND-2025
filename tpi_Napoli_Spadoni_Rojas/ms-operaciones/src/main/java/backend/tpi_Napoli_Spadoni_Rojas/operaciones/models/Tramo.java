package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    @JsonIgnore
    private Ruta ruta;

    @Column(nullable = false, length = 100)
    private String origen;

    @Column(nullable = false, length = 100)
    private String destino;

    @Column(nullable = false)
    private Double distanciaKm;

    @Column(nullable = false)
    private Double costo;

    // IDs externos (de ms-flota)
    @Column(name = "camion_id", nullable = false)
    private Long camionId;

    @Column(name = "transportista_id")
    private Long transportistaId;

    @Column(name = "deposito_origen_id")
    private Long depositoOrigenId;

    @Column(name = "deposito_destino_id")
    private Long depositoDestinoId;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFinEstimada;

    // Campos para valores reales
    @Column(name = "fecha_hora_inicio_real")
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real")
    private LocalDateTime fechaHoraFinReal;

    @Column(name = "distancia_km_real")
    private Double distanciaKmReal;

    @Column(name = "duracion_min_real")
    private Integer duracionMinReal;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "dias_estadia")
    private Integer diasEstadia;

    @Column(name = "costo_estadia")
    private Double costoEstadia;

    // Estado y tipo del tramo
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTramo estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTramo tipo;

    @Column(nullable = false)
    private Integer orden;
}
