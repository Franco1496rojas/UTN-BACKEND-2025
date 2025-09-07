package com.frc.isi.csv.colecciones.menu;

@FunctionalInterface
public interface OpcionDeMenu<T> {
    void ejecutar(T contexto);
}
