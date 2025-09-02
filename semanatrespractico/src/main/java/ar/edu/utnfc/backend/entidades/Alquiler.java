package ar.edu.utnfc.backend.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Alquiler {

    private String codigo;
    private int huespedes;
    private int diasAlquiler;
    private double costoPorDia;
    private Cliente cliente;
    private Localizacion direccionPropiedad;

    public Alquiler(String codigo, Cliente cliente, int huespedes, int diasAlquiler, double costoPorDia, Localizacion direccionPropiedad) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.huespedes = huespedes;
        this.diasAlquiler = diasAlquiler;
        this.costoPorDia = costoPorDia;
        this.direccionPropiedad = direccionPropiedad;
    }

    public double calcularAlquiler() {
        return this.costoPorDia * this.diasAlquiler;
    }

}
