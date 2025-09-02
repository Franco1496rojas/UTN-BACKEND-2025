    package ar.edu.utnfc.backend.app;

    import java.io.File;
    import java.io.FileNotFoundException;
    import java.net.URISyntaxException;
    import java.net.URL;
    import java.util.Scanner;

    import ar.edu.utnfc.backend.bulkdata.ListaSimple;
    import ar.edu.utnfc.backend.entidades.Alquiler;
    import ar.edu.utnfc.backend.entidades.Cliente;
    import ar.edu.utnfc.backend.entidades.Localizacion;

    public class App {

        public static void main(String[] args) {
            ListaSimple lista = new ListaSimple();
            URL location = App.class.getResource("/alquileres_verano.csv");
            Scanner lector = null;

            try {
                File archivoCsv = new File(location.toURI());
                lector = new Scanner(archivoCsv);
                lector.nextLine(); // Salteo la primer linea (headers)
                while (lector.hasNext()) {
                    String linea = lector.nextLine();
                    String[] items = linea.split(";");
                    String codigo = items[0];

                    //cliente
                    String dni = items[1];
                    String nombre = items[2];
                    String telefono = items[3];
                    String email = items[4];

                    Cliente cliente = new Cliente(dni, nombre, telefono, email);

                    //localizacion
                    String direccion = items[5];
                    String ciudad = items[6];
                    String codigoPostal = items[7];

                    Localizacion localizacion = new Localizacion(direccion, ciudad, codigoPostal);

                    //alquiler
                    int huespedes = Integer.parseInt(items[8]);
                    int diasAlquiler = Integer.parseInt(items[9]);
                    double costoPorDia = Double.parseDouble(items[10]);

                    Alquiler alquiler = new Alquiler(codigo, cliente, huespedes, diasAlquiler, costoPorDia, localizacion);
                    
                    lista.append(alquiler);
                }

            } catch (URISyntaxException e) {
                System.out.println("main: la ubicacion del csv no genero una ruta correcta");

            } catch (FileNotFoundException e) {
                System.out.println("main: el archivo que desea importar no existe");

            } catch (NumberFormatException e) {
                System.out.println("main: error al importar datos en la conversion a numeros");
            } finally {
                if (lector != null) {
                    lector.close();
                }
            }
            System.out.println("Cantidad de alquileres en Mendoza: " + lista.contarProvincia("Mendoza"));
            System.out.println("Costo total de alquileres: " + lista.calcularTotalAlquileres());
            System.out.println("Cantidad de alquileres importados: " + lista.size());
        }
    }
