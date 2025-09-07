package com.frc.isi.csv.colecciones.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Menu<T> implements IMenu<T> {

    private Map<Integer, OpcionDeMenu<T>> acciones = new HashMap<>();
    //para poner items en el menu
    private ArrayList<ItemMenu> opciones = new ArrayList<>();

    @Getter
    private int op = -1;

    @Override
    public void registrarOpcion(int opcion, String textoAMostrar, OpcionDeMenu<T> action) {
        this.acciones.put(opcion, action);
        this.opciones.add(new ItemMenu(opcion, textoAMostrar));
    }

    @Override
    public void invocarAccion(T contexto, Scanner lector) {
        System.out.println("Seleccione una opcion:");
        //usamos item del menu
        this.opciones.forEach(System.out::println);
        System.out.print("Ingrese una opcion: ");
        op = lector.nextInt();
        OpcionDeMenu<T> accion = this.acciones.get(op);
        if (accion != null) {
            accion.ejecutar(contexto);
        } else if (op < 0 || op > this.opciones.size() - 1) {
            System.out.println("Opcion invalida");
        }
    }

    public int getOpcion() {
        return op;
    }
}
