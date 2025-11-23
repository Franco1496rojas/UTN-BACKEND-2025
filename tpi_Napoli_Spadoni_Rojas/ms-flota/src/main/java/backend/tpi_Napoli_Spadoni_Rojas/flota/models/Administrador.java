package backend.tpi_Napoli_Spadoni_Rojas.flota.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer dni;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 50)
    private String telefono;

    @Column(nullable = false, length = 100)
    private String domicilio;

    @Column(name = "keycloak_id", nullable = false, length = 50)
    private String keycloakId;

    @Column(nullable = false, length = 50)
    private String ciudad;
}
