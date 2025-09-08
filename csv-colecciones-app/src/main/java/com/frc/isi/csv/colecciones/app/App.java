package com.frc.isi.csv.colecciones.app;

import java.net.URL;
import java.util.Scanner;

import com.frc.isi.csv.colecciones.entidades.Ventas;
import com.frc.isi.csv.colecciones.menu.Menu;

/**
 * Clase principal de la aplicación.
 * Permite gestionar ventas a través de un menú interactivo en consola.
 */
public class App {

    // Scanner para leer entrada del usuario desde consola
    private static final Scanner lector = new Scanner(System.in);

    /**
     * Método principal que inicia la aplicación.
     * Configura el menú y gestiona las opciones seleccionadas por el usuario.
     */
    public static void main(String[] args) {
        // Se crea un menú genérico para manejar objetos de tipo Ventas
        Menu<Ventas> menuOpciones = new Menu<>();
        Ventas ventas = new Ventas();

        // Obtiene la ruta del folder de datos donde están los archivos CSV
        URL folderPath = App.class.getResource("../resources/data");
        if (folderPath == null) {
            System.out.println("No se encontro el folder data");
            System.exit(0);
        }

        // Registro de las opciones del menú y las acciones asociadas
        menuOpciones.registrarOpcion(1, "Importar ventas", p -> p.importarVentas(folderPath));
        menuOpciones.registrarOpcion(2, "Mostrar ventas", p -> System.out.println(p.sortedToString()));
        menuOpciones.registrarOpcion(3, "Informar Ventas Mayores al Promedio", p -> p.buscarMayoresPromedio().forEach(System.out::println));
        menuOpciones.registrarOpcion(4, "Informe segun Tipo Producto", p -> buscarPorTipoProducto(p));
        menuOpciones.registrarOpcion(5, "Total Vendido por Tipo Producto", p -> p.totalVentasPorTipoProducto().forEach(System.out::println));
        menuOpciones.registrarOpcion(6, "Ventas mayores descuentos", p -> p.iformeMayoresDescuentos().forEach(System.out::println));
        menuOpciones.registrarOpcion(0, "Salir", p -> System.exit(1));

        int opcion = -1;
        // Bucle principal del menú, se repite hasta que el usuario elige salir
        do {
            menuOpciones.invocarAccion(ventas, lector);
            opcion = menuOpciones.getOpcion();
        } while (opcion != 0);
    }

    /**
     * Permite al usuario buscar ventas por tipo de producto solicitando el código correspondiente.
     * @param p objeto Ventas sobre el cual se realiza la búsqueda
     */
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