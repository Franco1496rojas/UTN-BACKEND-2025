package com.frc.isi.csv.colecciones.menu;

import java.util.Scanner;



public interface IMenu<T> {

    void registrarOpcion(int opcion, String textoAMostrar, OpcionDeMenu<T> action) ;

    void invocarAccion(T contexto, Scanner lector);
}
