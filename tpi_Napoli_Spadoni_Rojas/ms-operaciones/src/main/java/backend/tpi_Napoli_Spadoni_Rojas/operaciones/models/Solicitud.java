package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IDs externos (sin relación directa JPA)
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "contenedor_id", nullable = false)
    private Long contenedorId;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "costo_estimado")
    private Double costoEstimado;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "tiempo_estimado_min")
    private Integer tiempoEstimadoMin;

    @Column(name = "tiempo_real_min")
    private Integer tiempoRealMin;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estadoActual;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties(value = { "solicitud", "tramos", "hibernateLazyInitializer", "handler" }, allowSetters = true)
    private List<Ruta> rutas;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties(value = { "solicitud", "hibernateLazyInitializer", "handler" }, allowSetters = true)
    private List<CambioEstadoSolicitud> historialEstados;
}
