package com.frc.isi.csv.colecciones.app;


import java.net.URL;
import java.util.Scanner;

import com.frc.isi.csv.colecciones.entidades.Ventas;
import com.frc.isi.csv.colecciones.menu.Menu;

public class App {

    private static final Scanner lector = new Scanner(System.in);

    public static void main(String[] args) {
        Menu<Ventas> menuOpciones = new Menu<>();
        Ventas ventas = new Ventas();

        URL folderPath = App.class.getResource("../resources/data");
        if (folderPath == null) {
            System.out.println("No se encontro el folder data");
            System.exit(0);
        }

        //opciones del menu
        menuOpciones.registrarOpcion(1, "Importar ventas", p -> p.importarVentas(folderPath));
        menuOpciones.registrarOpcion(2, "Mostrar ventas", p -> System.out.println(p.sortedToString()));
        menuOpciones.registrarOpcion(3, "Informar Ventas Mayores al Promedio", p -> p.buscarMayoresPromedio().forEach(System.out::println));
        menuOpciones.registrarOpcion(4, "Informe segun Tipo Producto", p -> buscarPorTipoProducto(p));
        menuOpciones.registrarOpcion(5, "Total Vendido por Tipo Producto", p -> p.totalVentasPorTipoProducto().forEach(System.out::println));
        menuOpciones.registrarOpcion(6, "Ventas mayores descuentos", p -> p.iformeMayoresDescuentos().forEach(System.out::println));
        menuOpciones.registrarOpcion(0, "Salir", p -> System.exit(1));

        int opcion = -1;
        do {
            menuOpciones.invocarAccion(ventas, lector);
            opcion = menuOpciones.getOpcion();
        } while (opcion != 0);
    }

    private static void buscarPorTipoProducto(Ventas p) {
        int codigoTipoProducto = 0;

        try {
            System.out.print("Ingrese el código del tipo de producto: ");
            codigoTipoProducto = lector.nextInt();
            p.infomeTipoProducto(codigoTipoProducto).forEach(System.out::println);
        } catch (IllegalArgumentException | NoSuchFieldError e) {
            System.out.println("acciones::buscarPorTipoProducto: " + e.getMessage());
        }
    }
}
