package ar.edu.utnfc.backend.bulkdata;

import java.util.NoSuchElementException;

import ar.edu.utnfc.backend.entidades.Alquiler;
import ar.edu.utnfc.backend.entidades.Localizacion;

public class ListaSimple {

    private Object[] items;
    private int size; // cantidad de elementos reales

    public ListaSimple() {
        items = new Object[10];
        size = 0;
    }

    public ListaSimple(int capacidad) {
        this.items = new Object[capacidad];
        this.size = 0;
    }

    public void append(Object alquiler) {
        if (this.size >= this.items.length) {
            asegurarCapacidad();
        }
        this.items[this.size] = alquiler;
        this.size++;
    }

    private void asegurarCapacidad() {
        Object[] nuevo = new Object[this.items.length * 2];
        System.arraycopy(this.items, 0, nuevo, 0, this.items.length);
        this.items = nuevo;
    }

    public Object remove(int pos) throws NoSuchElementException {
        if (pos >= this.size) {
            throw new NoSuchElementException("No element found at the vector: ");
        }
        Object deleted = this.items[pos];
        mantenerIntegridad(pos);
        return deleted;
    }

    private void mantenerIntegridad(int pos) {
        for (int i = pos; i < this.size - 1; i++) {
            this.items[i] = this.items[i + 1];
        }
        this.items[this.size - 1] = null;
        this.size--;
    }

    public Object get(int pos) throws NoSuchElementException {
        if (pos >= this.size) {
            throw new NoSuchElementException("No element found at the vector: ");
        }
        return this.items[pos];
    }

    public int size() {
        return this.size;
    }

    public int contarProvincia(String ciudad) {
        int contador = 0;
        for (int i = 0; i < this.size; i++) {
            Alquiler alquiler = (Alquiler) this.items[i];
            Localizacion localizacion = alquiler.getDireccionPropiedad();
            if (localizacion.getCiudad().equals(ciudad)) {
                contador++;
            }
        }
        return contador;
    }

    public double calcularTotalAlquileres() {
        double total = 0;
        for (int i = 0; i < this.size; i++) {
            Alquiler alquiler = (Alquiler) this.items[i];
            total += alquiler.calcularAlquiler();
        }
        return total;
    }
}
