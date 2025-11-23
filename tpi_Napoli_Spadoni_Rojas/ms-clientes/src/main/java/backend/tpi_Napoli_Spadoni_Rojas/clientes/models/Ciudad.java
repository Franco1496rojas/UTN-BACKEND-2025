package backend.tpi_Napoli_Spadoni_Rojas.clientes.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ciudades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "codigo_postal", nullable = false, length = 20)
    private String codigoPostal;

    @ManyToOne
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;
}
