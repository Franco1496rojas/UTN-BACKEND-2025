package ar.edu.utnfc.backend.abstracciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
//import java.util.NoSuchElementException;

@DisplayName("Pruebas para la clase ListaArray")
public class ListaArrayTest {

    private ListaArray lista;

    @BeforeEach
    public void setUp() {
        lista = new ListaArray(2); // Capacidad inicial 2
    }

    @Test
    @DisplayName("Se pide un elemento en una posición inexistente y se dispara exception")
    public void GetItemManejoException() {
        lista.add("Elemento 1");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            lista.get(3);
        });
    }

    @Test
    @DisplayName("Se pide un elemento en una posición y es exitoso")
    public void GetItemRetornaElemento() {
        lista.add("Elemento 1");
        Object obtenido = lista.get(0);
        assertNotNull(obtenido);
        assertEquals("Elemento 1", obtenido);
    }

    @Test
    @DisplayName("Se agrega elemento en un tamaño inicial")
    public void AppendExitosamenteConTamanioInicial() {
        lista.add("Elemento 1");
        assertNotNull(lista.get(0));
        assertEquals("Elemento 1", lista.get(0));
    }

    @Test
    @DisplayName("Se agrega elemento cuando excedió la capacidad")
    public void AppendExistosamenteConDesborde() {
        lista.add("Elemento 1");
        lista.add("Elemento 2");
        lista.add("Elemento 3"); // Desborda capacidad inicial

        assertEquals(3, lista.size());
        assertNotNull(lista.get(2));
        assertEquals("Elemento 3", lista.get(2));
    }

    @Test
    @DisplayName("Se elimina un elemento y los posteriores corren su lugar")
    public void RemoveTest() {
        lista.add("A");
        lista.add("B");
        lista.add("C");
        Object eliminado = lista.remove(1); // Elimina "B"
        assertEquals("B", eliminado);
        assertEquals("C", lista.get(1)); // Ahora "C" está en la posición 1
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Set reemplaza correctamente el elemento en la posición")
    public void SetTest() {
        lista.add("Uno");
        lista.add("Dos");
        Object viejo = lista.set(1, "Nuevo");
        assertEquals("Dos", viejo);
        assertEquals("Nuevo", lista.get(1));
    }
}