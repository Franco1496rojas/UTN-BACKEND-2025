
package ar.edu.utnfc.backend.abstracciones;

import java.util.AbstractList;
//import java.util.Iterator;
//mport java.util.NoSuchElementException;


public class ListaArray extends AbstractList<Object> {

    private Object[] elementos;
    private int tamaño;

    public ListaArray() {
        elementos = new Object[10]; // Capacidad inicial
        tamaño = 0;
    }

    public ListaArray(int tam) {
        elementos = new Object[tam]; // Capacidad inicial
        tamaño = 0;
    }

    @Override
    public Object get(int indice) {
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        return elementos[indice];
    }

    @Override
    public int size() {
        return tamaño;
    }

    @Override
    public boolean add(Object elemento) {
        if (tamaño == elementos.length) {
            Object[] nuevoArray = new Object[elementos.length * 2];
            System.arraycopy(elementos, 0, nuevoArray, 0, elementos.length);
            elementos = nuevoArray;
        }
        elementos[tamaño++] = elemento;
        return true;
    }

    @Override
    public Object set(int index, Object elemento) {
        if (index < 0 || index >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        Object antiguo = elementos[index];
        elementos[index] = elemento;
        return antiguo;
    }

    @Override
    public Object remove(int index) {
        if (index < 0 || index >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        Object eliminado = elementos[index];
        for (int i = index; i < tamaño - 1; i++) {
            elementos[i] = elementos[i + 1];
        }
        elementos[--tamaño] = null;
        return eliminado;
    }

    @Override
    public String toString() {
        String[] a = new String[tamaño];
        for (int i = 0; i < tamaño; i++) {
            a[i] = elementos[i].toString();
        }
        return String.join(", ", a);
    }
}
// public class ListaArray implements Iterable<Object> {

//     private Object[] elementos;
//     private int tamaño;
//     private int actual;

//     public ListaArray() {
//         elementos = new Object[10]; // Capacidad inicial
//         tamaño = 0;
//         actual = 0;
//     }

//     public ListaArray(int tam) {
//         elementos = new Object[tam]; // Capacidad inicial
//         tamaño = 0;
//         actual = 0;
//     }

//     public void add(Object elemento) {
//         if (tamaño == elementos.length) {
//             Object[] nuevoArray = new Object[elementos.length * 2];
//             System.arraycopy(elementos, 0, nuevoArray, 0, elementos.length);
//             elementos = nuevoArray;
//         }
//         elementos[tamaño] = elemento;
//         tamaño++;
//     }

//     public int size() {
//         return tamaño;
//     }

//     public Object get(int indice) {
//         if (indice < 0 || indice >= tamaño) {
//             throw new IndexOutOfBoundsException("Índice fuera de rango");
//         }
//         return elementos[indice];
//     }

//     @Override
//     public String toString() {
//         String[] a = new String[tamaño];
//         for (int i = 0; i < tamaño; i++) {
//             a[i] = elementos[i].toString();
//         }
//         return String.join(", ", a);
//     }

//     public void reiniciarIterador() {
//         actual = 0;
//     }

//     public boolean haySiguiente() {
//         //return !isEmpty() && actual < size() - 1;
//         return !isEmpty() && actual < size();
//     }

//     public boolean isEmpty() {
//         return size() == 0;
//     }

//     public Object siguiente() {
//         if (!haySiguiente()) {
//             throw new NoSuchElementException("No hay siguiente elemento");
//         }
//         //actual++;
//         return elementos[actual++];
//     }

//     @Override
//     public Iterator<Object> iterator() {
//         return new ListaArrayIterator();
//     }
//     // Clase interna para el iterador
//     private class ListaArrayIterator implements Iterator<Object> {
//         private int pos = 0;

//         @Override
//         public boolean hasNext() {
//             return pos < size();
//         }

//         @Override
//         public Object next() {
//             if (!hasNext()) {
//                 throw new NoSuchElementException("No hay siguiente elemento");
//             }
//             return elementos[pos++];
//         }
//     }

// }
