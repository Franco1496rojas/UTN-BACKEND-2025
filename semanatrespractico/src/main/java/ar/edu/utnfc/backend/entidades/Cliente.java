package ar.edu.utnfc.backend.entidades;

//import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor

public class Cliente {
    private String dni;
    private String nombre;
    private String telefono;
    private String email;

    public Cliente(String dni, String nombre, String telefono, String email) {
        this.dni = dni;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }
}
