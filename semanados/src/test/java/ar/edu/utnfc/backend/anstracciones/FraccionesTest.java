package ar.edu.utnfc.backend.anstracciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class FraccionesTest {
    @Test
    public void sumaFraccionesEsExitosa(){
        //Arrange
        Fracciones f1 = new Fracciones(7,8);
        Fracciones f2 = new Fracciones(1,2);

        //Act
        Fracciones suma = f1.suma(f2);

        //Assert
        assertNotNull(suma);
        assertEquals(suma.getNumerador(), 22);
        assertEquals(suma.getDenominador(), 16);


    }
}
