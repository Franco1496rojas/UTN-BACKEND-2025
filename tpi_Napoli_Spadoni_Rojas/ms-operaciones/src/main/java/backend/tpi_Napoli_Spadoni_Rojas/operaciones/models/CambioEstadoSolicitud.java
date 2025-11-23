package backend.tpi_Napoli_Spadoni_Rojas.operaciones.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cambios_estado_solicitud")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    @JsonIgnore
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Estado estadoAnterior; // Puede ser null en el primer cambio (creación de la solicitud)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    @Column(length = 200)
    private String observaciones;
}
