package utnfc.isi.back;

import java.util.List;

import utnfc.isi.back.menu.ApplicationContext;
import utnfc.isi.back.menu.Menu;
import utnfc.isi.back.menu.MenuOption;


public class App {
    public static void main(String[] args) {
        ApplicationContext ctx = ApplicationContext.getInstance(); // Obtiene el contexto único
        ctx.put("instrucciones", "Demo etapa 2");

        var opciones = List.of(
            new MenuOption(1, "Hola mundo", c -> System.out.println("¡Hola!")),
            new MenuOption(2, "Mostrar hora", c -> System.out.println(java.time.LocalTime.now()))
        );
        new Menu("Menú Funcional — Etapa 2", opciones).run(ctx);
        // var opciones = List.of(
        //     new MenuOption(1, "Hola mundo", () -> System.out.println("¡Hola!")),
        //     new MenuOption(2, "Mostrar hora", () -> System.out.println(java.time.LocalTime.now()))
        // );
        // new Menu("Menú Funcional — Etapa 1", opciones).run();
    }
}
