package com.frc.isi.museo.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.frc.isi.museo.entidades.ObraArtistica;
import com.frc.isi.museo.menu.ApplicationContext;
import com.frc.isi.museo.servicios.EstiloArtisticoService;
import com.frc.isi.museo.servicios.ObraArstiticaService;

public class Acciones {
    // public void nombreMetodo(ApplicationContext context) cada metodo de esta
    // clase la firma debe ser esta

    public void informeObrasExhibidas(ApplicationContext context) {
        var lector = (Scanner) context.get("lector");
        var service = context.getService(ObraArstiticaService.class);
        final String salida = "|%50s|%10s|$%-10.2f|%4s|%20s|%20s|";

        System.out.print("Ingrese el nombre del museo a buscar: ");
        String museo = lector.next();

        try {
            List<ObraArtistica> lista = service.getObrasByNombreMuseo(museo);

            System.out.printf("Lista de Obras exhibidas en %s%n", museo);
            System.out.println("=========================================================");
            System.out.printf("|%50s|%10s|%10s|%4s|%20s|%20s|%n", "Nombre", "Creada en", "M. Asegur.", "Total", "Autor",
                    "Estilo");

            String lineas = lista.stream()
                    .map(p -> String.format(salida, p.getNombre(), p.getAnio(), p.getMontoAsegurado(),
                            p.isSeguroTotal() ? "Si" : "No", p.getAutor().getNombre(),
                            p.getEstiloArtistico().getNombre()))
                    .collect(Collectors.joining("\n"));

            System.out.println(lineas);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void informeObrasMayorPromedio(ApplicationContext context) {
        var servicio = context.getService(ObraArstiticaService.class);
        var obras = servicio.getObrasSeguroParcialMayoresPromedio();
        System.out.println("Listado de Obras Artisticas con Seguro Parcial");
        obras.stream().forEach(System.out::println);
    }

    public void generarTextoCantidadPorEstilo(ApplicationContext context) {
        var servicio = context.getService(EstiloArtisticoService.class);
        Map<String, Long> obrasPorEstilo = servicio.getCantidadObrasPorEstilo();

        List<String> lineas = new ArrayList<>();

        lineas.add("Estilo,Cantidad");

        lineas.addAll(obrasPorEstilo.entrySet()
                .stream()
                .map(p -> String.format("%s,%d", p.getKey(), p.getValue()))
                .collect(Collectors.toList()));

        try {
            var pathToExport = (URL) context.get("path");
            Path path = Paths.get(pathToExport.toURI()).resolve("cantidadPorEstilo.csv");
            Files.write(path, lineas);
            System.out.println("Archivo generado exitosamente.....");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void informarTotalesAsegurados(ApplicationContext context) {
        var servicio = context.getService(ObraArstiticaService.class);
        servicio.informarTotalesAsegurados().forEach(System.out::println);
    }

    public void importarObras(ApplicationContext context) {
        var pathToImport = (URL) context.get("path");

        try (var paths = Files.walk(Paths.get(pathToImport.toURI()))) {
            var csvFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .map(path -> path.toFile())
                    .toList();

            csvFiles.stream()
                    .filter(f -> f.getName()
                            .contains("obras"))
                    .findFirst()
                    .ifPresentOrElse(f -> {
                        var service = context.getService(ObraArstiticaService.class);
                        try {
                            service.bulkInsert(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    },
                            () -> new IllegalArgumentException("Archivo inexistente"));

        } catch (IOException | URISyntaxException e) {

            e.printStackTrace();
        }

    }
}
