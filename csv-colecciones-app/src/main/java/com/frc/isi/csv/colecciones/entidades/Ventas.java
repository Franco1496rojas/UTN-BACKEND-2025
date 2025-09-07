package com.frc.isi.csv.colecciones.entidades;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.frc.isi.csv.colecciones.utils.NumberUtils;

public class Ventas {
        private ArrayList<Venta> ventas = new ArrayList<>();
        private Map<String, TipoProducto> tipos = new HashMap<>();

        public StringBuilder sortedToString() {

                Iterable<Venta> listaOrdenada = this.ventas.stream()
                                .sorted(Comparator.comparing(Venta::getProducto))
                                .collect(Collectors.toList());

                long total = this.ventas.stream()
                                .collect(Collectors.summingInt(Venta::getCantidadVendida));

                double totalVendido = this.ventas.stream()
                                .collect(Collectors.summingDouble(Venta::calcularPrecioVenta));

                StringBuilder sb = new StringBuilder("Listado de Ventas\n");
                sb.append("===========================================\n");
                listaOrdenada.forEach(v -> sb.append(v).append("\n"));
                sb.append("Cantidad total de Productos Vendidos: ").append(total).append("\n");

                totalVendido = NumberUtils.roundDouble(totalVendido, 2);
                sb.append("Importe total vendido: ").append(String.format("%.2f", totalVendido)).append("\n");
                return sb;
        }

        public Iterable<Venta> buscarMayoresPromedio() {
                DoubleSummaryStatistics stats = this.ventas.stream()
                                .collect(Collectors.summarizingDouble(Venta::calcularPrecioVenta));

                double promedio = stats.getAverage();

                return this.ventas.stream().filter(v -> v.calcularPrecioVenta() > promedio)
                                .collect(Collectors.toList());

        }

        public Iterable<Map.Entry<String, Double>> totalVentasPorTipoProducto() {

                Map<String, Double> totalPorTipos = this.ventas.stream()
                                .collect(Collectors.groupingBy(p -> p.getTipoProducto().getDescripcion(),
                                                Collectors.summingDouble(Venta::calcularPrecioVenta)));

                return totalPorTipos.entrySet().stream()
                                .sorted(Entry.<String, Double>comparingByValue().reversed())
                                .collect(Collectors.toSet());
        }

        public Iterable<Venta> iformeMayoresDescuentos() {
                List<Venta> lista = this.ventas.stream()
                                .collect(Collectors.partitioningBy(p -> p.getDescuento() > .01))
                                .get(true);

                return lista.stream()
                                .sorted(Comparator.comparing(Venta::getCodigo))
                                .toList();
        }

        public void importarVentas(URL folderPath) {
                try (Stream<Path> paths = Files.walk(Paths.get(folderPath.toURI()))) {
                        List<File> csvFiles = paths
                                        .filter(Files::isRegularFile)
                                        .filter(path -> path.toString().endsWith(".csv"))
                                        .map(path -> path.toFile())
                                        .collect(Collectors.toList());

                        Optional<File> fileToImport = csvFiles.stream()
                                        .filter(p -> p.getName().contains("cafelahumedad"))
                                        .findFirst();

                        fileToImport.ifPresentOrElse(
                                        f -> {
                                                try {
                                                        bulkInsert(f); // aquí puede lanzar IOException
                                                } catch (IOException e) {
                                                        System.out.println("Error al procesar el archivo: "
                                                                        + e.getMessage());
                                                }
                                        },
                                        () -> System.out.println("Archivo cafelahumedad no encontrado en disco"));

                } catch (IOException | URISyntaxException | IllegalArgumentException e) {
                        System.out.println("Acciones::importarVentas = Se produjo un error " + e.getMessage());
                }
        }

        public Iterable<Venta> infomeTipoProducto(int codigoTipoProducto) throws IllegalArgumentException {
                int[] rango = this.obtenerCotasTipoProducto();
                if (codigoTipoProducto <= rango[0] || codigoTipoProducto >= rango[1])
                        throw new IllegalArgumentException(
                                        "El codigo del tipo de producto no esta en el rango valido de [" + rango[0]
                                                        + "," + rango[1] + "]");

                return this.generarInformeTipoProducto(codigoTipoProducto);

        }

        private int[] obtenerCotasTipoProducto() {
                int inf = Collections.min(this.tipos.values()).getCodigo();
                int sup = Collections.max(this.tipos.values()).getCodigo();

                int[] v = { inf, sup };
                return v;
        }

        private Iterable<Venta> generarInformeTipoProducto(int codigoTipoProducto) {
                IntSummaryStatistics stats = this.ventas.stream()
                                .collect(Collectors.summarizingInt(Venta::getCantidadVendida));
                int menorCantidadVendida = stats.getMin();

                return this.ventas.stream()
                                .filter(v -> v.getTipoProducto().getCodigo() == codigoTipoProducto
                                                && v.getCantidadVendida() > menorCantidadVendida)
                                .collect(Collectors.toList());

        }

        private void bulkInsert(File fileToImport) throws IOException, IllegalArgumentException {

                Files.lines(Paths.get(fileToImport.toURI()))
                                .skip(1)
                                .forEach(linea -> transformarLinea(linea));

        }

        private void transformarLinea(String linea) {
                String[] items = linea.split(",");

                // primero resuelvo el tipo de producto
                String tipoProducto = items[2];
                TipoProducto valor = tipos.computeIfAbsent(tipoProducto, k -> new TipoProducto(tipoProducto));

                String codigo = items[0];
                String descripcion = items[1];
                int cantidad = Integer.parseInt(items[3]);
                double precio = Double.parseDouble(items[4]);
                double descuento = Double.parseDouble(items[5]) / 100;

                Venta venta = new Venta(codigo, descripcion, cantidad, precio, descuento, valor);
                ventas.add(venta);

        }
    }
