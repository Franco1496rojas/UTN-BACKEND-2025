package backend.tpi_Napoli_Spadoni_Rojas.clientes.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contenedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false)
    private Double peso;

    @Column(nullable = false)
    private Double volumen;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
