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

        /**
         * Clase que representa la lógica y almacenamiento de las ventas. Gestiona la
         * importación, consulta y resumen de ventas.
         */
        public class Ventas {

        // Lista principal donde se almacenan todas las ventas importadas
        private ArrayList<Venta> ventas = new ArrayList<>();
        // Mapa de tipos de productos conocidos, clave: descripción
        private Map<String, TipoProducto> tipos = new HashMap<>();

        /**
         * Devuelve una representación ordenada y resumida de las ventas.
         *
         * @return StringBuilder con el listado y totales
         */
        public StringBuilder sortedToString() {

                // Ordena las ventas por nombre de producto
                Iterable<Venta> listaOrdenada = this.ventas.stream()
                        .sorted(Comparator.comparing(Venta::getProducto))
                        .collect(Collectors.toList());

                // Calcula el total de cantidad vendida
                long total = this.ventas.stream()
                        .collect(Collectors.summingInt(Venta::getCantidadVendida));

                // Calcula el total vendido (importe)
                double totalVendido = this.ventas.stream()
                        .collect(Collectors.summingDouble(Venta::calcularPrecioVenta));

                StringBuilder sb = new StringBuilder("Listado de Ventas\n");
                sb.append("===========================================\n");
                listaOrdenada.forEach(v -> sb.append(v).append("\n"));
                sb.append("Cantidad total de Productos Vendidos: ").append(total).append("\n");

                // Redondea el total vendido a dos decimales
                totalVendido = NumberUtils.roundDouble(totalVendido, 2);
                sb.append("Importe total vendido: ").append(String.format("%.2f", totalVendido)).append("\n");
                return sb;
        }

        /**
         * Busca las ventas cuyo importe es mayor al promedio.
         *
         * @return Iterable con las ventas filtradas
         */
        public Iterable<Venta> buscarMayoresPromedio() {
                DoubleSummaryStatistics stats = this.ventas.stream()
                        .collect(Collectors.summarizingDouble(Venta::calcularPrecioVenta));

                double promedio = stats.getAverage();

                return this.ventas.stream().filter(v -> v.calcularPrecioVenta() > promedio)
                        .collect(Collectors.toList());

        }

        /**
         * Obtiene el total vendido agrupado por tipo de producto.
         *
         * @return Iterable con pares (tipo de producto, total vendido)
         */
        public Iterable<Map.Entry<String, Double>> totalVentasPorTipoProducto() {

                Map<String, Double> totalPorTipos = this.ventas.stream()
                        .collect(Collectors.groupingBy(p -> p.getTipoProducto().getDescripcion(),
                                Collectors.summingDouble(Venta::calcularPrecioVenta)));

                return totalPorTipos.entrySet().stream()
                        .sorted(Entry.<String, Double>comparingByValue().reversed())
                        .collect(Collectors.toSet());
        }

        /**
         * Busca ventas que tienen descuentos significativos.
         *
         * @return Iterable con las ventas que tienen descuento mayor a 1%
         */
        public Iterable<Venta> iformeMayoresDescuentos() {
                List<Venta> lista = this.ventas.stream()
                        .collect(Collectors.partitioningBy(p -> p.getDescuento() > .01))
                        .get(true);

                return lista.stream()
                        .sorted(Comparator.comparing(Venta::getCodigo))
                        .toList();
        }

        /**
         * Importa ventas desde los archivos CSV ubicados en el folder especificado.
         * Solo importa el archivo cuyo nombre contiene "cafelahumedad".
         *
         * @param folderPath ruta del folder donde buscar los archivos
         */
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

        /**
         * Genera un informe de ventas filtradas por tipo de producto, validando el
         * código.
         *
         * @param codigoTipoProducto código del tipo de producto a buscar
         * @return Iterable con las ventas filtradas
         * @throws IllegalArgumentException si el código está fuera del rango válido
         */
        public Iterable<Venta> infomeTipoProducto(int codigoTipoProducto) throws IllegalArgumentException {
                int[] rango = this.obtenerCotasTipoProducto();
                if (codigoTipoProducto <= rango[0] || codigoTipoProducto >= rango[1]) {
                throw new IllegalArgumentException(
                        "El codigo del tipo de producto no esta en el rango valido de [" + rango[0]
                        + "," + rango[1] + "]");
                }

                return this.generarInformeTipoProducto(codigoTipoProducto);

        }

        /**
         * Obtiene el rango de códigos válidos para los tipos de producto
         * registrados.
         *
         * @return array con el mínimo y máximo código
         */
        private int[] obtenerCotasTipoProducto() {
                int inf = Collections.min(this.tipos.values()).getCodigo();
                int sup = Collections.max(this.tipos.values()).getCodigo();

                int[] v = {inf, sup};
                return v;
        }

        /**
         * Genera el informe de ventas filtradas por tipo de producto y cantidad
         * vendida mayor al mínimo.
         *
         * @param codigoTipoProducto código del tipo de producto
         * @return Iterable con ventas filtradas
         */
        private Iterable<Venta> generarInformeTipoProducto(int codigoTipoProducto) {
                IntSummaryStatistics stats = this.ventas.stream()
                        .collect(Collectors.summarizingInt(Venta::getCantidadVendida));
                int menorCantidadVendida = stats.getMin();

                return this.ventas.stream()
                        .filter(v -> v.getTipoProducto().getCodigo() == codigoTipoProducto
                        && v.getCantidadVendida() > menorCantidadVendida)
                        .collect(Collectors.toList());

        }

        /**
         * Inserta todas las ventas presentes en el archivo CSV en la lista de
         * ventas.
         *
         * @param fileToImport archivo CSV a importar
         * @throws IOException si ocurre un error de lectura
         * @throws IllegalArgumentException si hay datos inválidos
         */
        private void bulkInsert(File fileToImport) throws IOException, IllegalArgumentException {

                // Lee el archivo línea por línea, omitiendo la cabecera
                Files.lines(Paths.get(fileToImport.toURI()))
                        .skip(1)
                        .forEach(linea -> transformarLinea(linea));

                }

        /**
         * Transforma una línea del archivo CSV en un objeto Venta y lo agrega a la
         * lista de ventas.
         *
         * @param linea línea de datos CSV
         */
        private void transformarLinea(String linea) {
                String[] items = linea.split(",");

                // Resuelve el tipo de producto (lo crea si no existe)
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
