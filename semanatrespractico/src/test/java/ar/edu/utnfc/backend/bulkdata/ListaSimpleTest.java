package ar.edu.utnfc.backend.bulkdata;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ar.edu.utnfc.backend.entidades.Alquiler;

@DisplayName("Pruebas para la clase ListaSimple")
public class ListaSimpleTest {

    private ListaSimple lista;

    @BeforeEach
    public void setUp() {
        lista = new ListaSimple(2);
    }

    @Test
    @DisplayName("se pide un elemento en una posicion inexistente y se dispara exception")
    public void GetItemManejoException() {
        //Arrange
        Alquiler alquiler = new Alquiler();
        alquiler.setCodigo("12345");
        lista.append(alquiler);

        //Act y Assert
        assertThrows(NoSuchElementException.class, () -> {
            //Object obtenido = lista.get(3);
            lista.get(3);
        });
    }

    @Test
    @DisplayName("Se pide un elemento en una posicion y existoso")
    public void GetItemRetornaElemento() {
        //Arrange
        Alquiler alquiler = new Alquiler();
        alquiler.setCodigo("12345");
        lista.append(alquiler);

        //Act
        Object obtenido = lista.get(0);

        //Assert
        assertNotNull(obtenido);
        assertInstanceOf(Alquiler.class, obtenido);
        assertEquals(((Alquiler) obtenido).getCodigo(), "12345");
    }

    @Test
    @DisplayName("Se agrega elemento en un tamanio inicial")
    public void AppendExitosamenteConTamanioInicial() {
        // Arrange
        Alquiler alquiler = new Alquiler();
        alquiler.setCodigo("12345");

        // Act
        lista.append(alquiler);

        // Assert
        assertNotNull(lista.get(0));
        assertInstanceOf(Alquiler.class, lista.get(0));
        assertEquals(alquiler.getCodigo(), ((Alquiler) lista.get(0)).getCodigo());

    }

    @Test
    @DisplayName("Se agrega elemento cuando excedio la capacidad")
    public void AppendExistosamenteConDesborde() {
        // Arrange
        for (int i = 0; i < 2; i++) {
            Alquiler alquiler = new Alquiler();
            alquiler.setCodigo(Integer.toString(i + 1));
            lista.append(alquiler);
        }

        Alquiler nuevo = new Alquiler();
        nuevo.setCodigo("125");

        // Act
        lista.append(nuevo);

        // Assert
        assertEquals(lista.size(), 3);
        assertNotNull(lista.get(2));
        assertInstanceOf(Alquiler.class, lista.get(2));
        assertEquals(nuevo.getCodigo(), ((Alquiler) lista.get(2)).getCodigo());
    }

}
