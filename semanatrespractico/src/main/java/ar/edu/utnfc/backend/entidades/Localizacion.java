package ar.edu.utnfc.backend.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Localizacion {
    private int codigo;
    private String codigoPostal;
    private String ciudad;
    private String direccion;

    private static int PROXIMO_CODIGO = 1;

    public Localizacion(String codigoPostal, String ciudad, String direccion) {
        this.codigo = PROXIMO_CODIGO++;
        this.codigoPostal = codigoPostal;
        this.ciudad = ciudad;
        this.direccion = direccion;
    }


}
