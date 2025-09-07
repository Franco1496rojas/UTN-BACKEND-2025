package com.frc.isi.csv.colecciones.menu;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemMenu {

    private int codigo;
    private String descripcion;

    @Override
    public String toString() {
        return this.codigo + " ---------------- " + this.descripcion;
    }
}
