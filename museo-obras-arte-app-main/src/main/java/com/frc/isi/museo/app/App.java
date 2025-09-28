package com.frc.isi.museo.app;

import java.net.URL;

import com.frc.isi.museo.menu.ApplicationContext;
import com.frc.isi.museo.menu.ItemMenu;
import com.frc.isi.museo.menu.Menu;
import com.frc.isi.museo.servicios.EstiloArtisticoService;
import com.frc.isi.museo.servicios.ObraArstiticaService;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

        var ctx = ApplicationContext.getInstance();
        Menu menu = new Menu();
        menu.setTitulo("Menu de Opciones para Museo");

        Acciones acciones = new Acciones();

        URL folderPath = App.class.getResource("/files");
        ctx.put("path", folderPath);
        ctx.registerService(ObraArstiticaService.class, new ObraArstiticaService());
        ctx.registerService(EstiloArtisticoService.class, new EstiloArtisticoService());

        // Las otras acciones van aqui
        menu.addOpcion(new ItemMenu(1, "Cargar Obras Arstisticas", acciones::importarObras));
        menu.addOpcion(new ItemMenu(2, "Informar Montos Totales Asegurador", acciones::informarTotalesAsegurados));
        menu.addOpcion(new ItemMenu(3, "Generar Archivo Texto Cantidad Obras por Estilo Artistico",
                acciones::generarTextoCantidadPorEstilo));
        menu.addOpcion(new ItemMenu(4, "Informe Obras mayores al promedio", acciones::informeObrasMayorPromedio));
        menu.addOpcion(new ItemMenu(5, "Informe Obras Artisticas exhibidas", acciones::informeObrasExhibidas));
        menu.addOpcion(new ItemMenu(0, "Salir", p -> System.exit(1)));

        menu.ejecutar(ctx);

    }
}
