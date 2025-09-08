package com.frc.isi.csv.colecciones.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Implementación genérica de un menú interactivo en consola.
 * Permite registrar opciones y ejecutar acciones asociadas.
 * @param <T> tipo de contexto sobre el que se ejecutan las acciones
 */
@NoArgsConstructor
public class Menu<T> implements IMenu<T> {

    // Mapa que asocia el número de opción a la acción a ejecutar
    private Map<Integer, OpcionDeMenu<T>> acciones = new HashMap<>();
    // Lista de items que representa las opciones del menú para mostrar al usuario
    private ArrayList<ItemMenu> opciones = new ArrayList<>();

    // Última opción seleccionada por el usuario
    @Getter
    private int op = -1;

    /**
     * Registra una opción en el menú junto con el texto descriptivo y la acción a ejecutar.
     * @param opcion número identificador de la opción
     * @param textoAMostrar texto que se muestra al usuario en el menú
     * @param action acción que se ejecuta al seleccionar la opción
     */
    @Override
    public void registrarOpcion(int opcion, String textoAMostrar, OpcionDeMenu<T> action) {
        this.acciones.put(opcion, action);
        this.opciones.add(new ItemMenu(opcion, textoAMostrar));
    }

    /**
     * Muestra las opciones al usuario y ejecuta la acción seleccionada.
     * @param contexto el objeto sobre el que se ejecuta la acción
     * @param lector Scanner para leer la opción elegida por el usuario
     */
    @Override
    public void invocarAccion(T contexto, Scanner lector) {
        System.out.println("Seleccione una opcion:");
        // Se muestran todas las opciones disponibles
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

    /**
     * Devuelve la última opción seleccionada por el usuario.
     * @return número de opción
     */
    public int getOpcion() {
        return op;
    }
}