package utnfc.isi.back.menu;

import java.util.List;
import java.util.Scanner;

public class Menu {
    private final String titulo;
    private final List<MenuOption> opciones;

    public Menu(String titulo, List<MenuOption> opciones) {
        this.titulo = titulo;
        this.opciones = opciones;
    }

    public void run(ApplicationContext ctx) {
        try (Scanner scanner = new Scanner(System.in)) {
            ctx.put("in", scanner); // Inyecta el Scanner en el contexto

            while (true) {
                System.out.println("\n" + titulo);
                for (MenuOption opt : opciones) {
                    System.out.printf("%d — %s\n", opt.getCode(), opt.getLabel());
                }
                System.out.print("Seleccione opción (0 para salir): ");

                // Leer como String y validar que sea número
                String input = scanner.nextLine();
                int sel;
                try {
                    sel = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Ingrese un número.\n");
                    continue;
                }

                if (sel == 0) break;

                MenuOption opt = opciones.stream()
                        .filter(o -> o.getCode() == sel)
                        .findFirst()
                        .orElse(null);
                if (opt != null) {
                    opt.execute(ctx); // Ejecuta la acción con el contexto
                } else {
                    System.out.println("Opción inválida.");
                }
            }
            ctx.remove("in"); // Limpia el Scanner del contexto
        }
    }
    // private final String title;
    // private final List<MenuOption> options;

    // public Menu(String title, List<MenuOption> options) {
    //     this.title = title;
    //     this.options = options;
    // }

    // public void run() {
    //     try (Scanner scanner = new Scanner(System.in)) {
    //         int choice = -1;
    //         while (choice != 0) {
    //             // Mostrar menú directamente aquí
    //             System.out.println("\n" + title);
    //             options.stream()
    //                     .sorted((a, b) -> Integer.compare(a.code(), b.code()))
    //                     .forEach(o -> System.out.println(o.code() + " - " + o.label()));
    //             System.out.println("0 - Salir");

    //             System.out.print("Ingrese una opción: ");
    //             String input = scanner.nextLine();

    //             try {
    //                 choice = Integer.parseInt(input);
    //             } catch (NumberFormatException e) {
    //                 System.out.println("Entrada inválida. Ingrese un número.\n");
    //                 continue;
    //             }

    //             if (choice == 0) {
    //                 System.out.println("Saliendo...");
    //                 break;
    //             }

    //             final int finalChoice = choice;
    //             var option = options.stream()
    //                     .filter(o -> o.code() == finalChoice)
    //                     .findFirst();

    //             if (option.isPresent()) {
    //                 option.get().action().run();
    //             } else {
    //                 System.out.println("Opción inválida.\n");
    //             }
    //         }
    //     }
    // }
}
