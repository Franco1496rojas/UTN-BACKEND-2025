package com.frc.isi.csv.colecciones.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Venta {

    private String codigo;
    private String producto;
    private Integer cantidadVendida;
    private Double precioUnitario;
    private Double descuento;
    private TipoProducto tipoProducto;

    public double calcularPrecioVenta() {
        double total = this.cantidadVendida * this.precioUnitario;
        if (descuento > 0) {
            total -= this.precioUnitario * this.descuento;
        }
        return total;
    }

}
