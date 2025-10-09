package backend.simulacro.app;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import backend.simulacro.config.InicializadorBd;
import backend.simulacro.menu.ApplicationContext;
import backend.simulacro.menu.ItemMenu;
import backend.simulacro.menu.Menu;
import backend.simulacro.services.DesarrolladorService;
import backend.simulacro.services.GeneroService;
import backend.simulacro.services.JuegoService;
import backend.simulacro.services.PlataformaService;

public class App {

    public static void main(String[] args) {
        // Inicializar la base de datos
        try {
            InicializadorBd.run();
        } catch (SQLException | IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        Acciones acciones = new Acciones();
        Menu menu = new Menu();
        menu.setTitulo(" -------------- Menu Principal: App de Juegos --------------");

        var ctx = ApplicationContext.getInstance();
        ctx.registerService(DesarrolladorService.class, new DesarrolladorService());
        ctx.registerService(GeneroService.class, new GeneroService());
        ctx.registerService(JuegoService.class, new JuegoService());
        ctx.registerService(PlataformaService.class, new PlataformaService());

        URL folderPath = App.class.getResource("/files");
        ctx.put("folderPath", folderPath);

        menu.addOpcion(new ItemMenu(1, "Cargar Archivo juegos.csv", acciones::cargarArchivoCsv));
        menu.addOpcion(new ItemMenu(2, "Top 5 generos mas jugados", acciones::top5GenerosMasJugados));
        menu.addOpcion(new ItemMenu(3, "Determinar cantidad  de juegos por desarrollador",
                acciones::mostrarJuegosPorDesarrollador));
        menu.addOpcion(new ItemMenu(4, "Desarrollador mejor valorado", acciones::desarrolladorMejorValorado));

        // menu.addOpcion(new ItemMenu(2, "Mostrar juegos", acciones::mostrarJuegos));
        // menu.addOpcion(new ItemMenu(3, "Mostrar Plataformas",
        // acciones::mostrarPlataformas));
        // menu.addOpcion(new ItemMenu(4, "Mostrar Desarrolladores",
        // acciones::mostrarDesarrolladores));
        // menu.addOpcion(new ItemMenu(5, "Verificar Relaciones",
        // acciones::verificarRelaciones));

        menu.ejecutar(ctx);

    }
}
