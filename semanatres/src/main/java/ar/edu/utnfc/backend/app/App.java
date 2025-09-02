package ar.edu.utnfc.backend.app;

import ar.edu.utnfc.backend.abstracciones.ListaArray;

public class App {

    public static void main(String[] args) {
        ListaArray lista = new ListaArray();
        lista.add("uno");
        lista.add("dos");
        lista.add("tres");

        System.err.println(lista);
        
        for (Object elem : lista) {
            System.out.println(elem);
        }
    }
}
