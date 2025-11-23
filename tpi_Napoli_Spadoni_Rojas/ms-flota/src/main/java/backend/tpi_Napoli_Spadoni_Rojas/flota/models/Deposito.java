package backend.tpi_Napoli_Spadoni_Rojas.flota.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "depositos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private Double costoEstadiaDiaria;

    @Column(nullable = false)
    private Boolean estado;

    @Column(nullable = false)
    private Integer capacidadMaxima;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidadOcupada = 0;

    /**
     * Verifica si el depósito tiene capacidad disponible
     */
    public boolean tieneCapacidadDisponible() {
        return cantidadOcupada < capacidadMaxima;
    }

    /**
     * Verifica si el depósito tiene espacio para N contenedores
     */
    public boolean tieneCapacidadPara(int cantidad) {
        return (cantidadOcupada + cantidad) <= capacidadMaxima;
    }

    /**
     * Obtiene la capacidad disponible actual
     */
    public int getCapacidadDisponible() {
        return capacidadMaxima - cantidadOcupada;
    }

    /**
     * Incrementa la cantidad ocupada
     */
    public void incrementarOcupacion(int cantidad) {
        if (!tieneCapacidadPara(cantidad)) {
            throw new IllegalStateException(
                String.format("No hay capacidad suficiente. Disponible: %d, Solicitado: %d", 
                    getCapacidadDisponible(), cantidad)
            );
        }
        this.cantidadOcupada += cantidad;
    }

    /**
     * Decrementa la cantidad ocupada
     */
    public void decrementarOcupacion(int cantidad) {
        if (this.cantidadOcupada - cantidad < 0) {
            throw new IllegalStateException("La cantidad ocupada no puede ser negativa");
        }
        this.cantidadOcupada -= cantidad;
    }
}
