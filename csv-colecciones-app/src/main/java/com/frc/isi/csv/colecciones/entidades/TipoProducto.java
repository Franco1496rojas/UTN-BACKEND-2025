package com.frc.isi.csv.colecciones.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TipoProducto implements Comparable<TipoProducto> {
    private int codigo;
    private String descripcion;

    private static int NextId = 0;

    public TipoProducto(String tipoProducto) {
        this.codigo = TipoProducto.NextId++;
        this.descripcion = tipoProducto;
    }

    @Override
    public int compareTo(TipoProducto o) {
        return this.codigo - o.codigo;
    }
}