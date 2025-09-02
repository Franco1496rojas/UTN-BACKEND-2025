package ar.edu.utnfc.backend.app;

import ar.edu.utnfc.backend.anstracciones.Fracciones;
import ar.edu.utnfc.backend.anstracciones.ProcesadorFracciones;


public class App 
{
    public static void main( String[] args )
    { 
        int tam = (int) ((Math.random() * 10) + 1);
        ProcesadorFracciones procesador = new ProcesadorFracciones(tam);
        for (int i = 0; i < tam; i++){
            int num = (int) ((Math.random() * 10) + 1);
            int deno = (int) ((Math.random() * 10) + 1);
            Fracciones frac2 = new Fracciones(num, deno);
            procesador.addFraccion(frac2);
        }

        Fracciones promedio = procesador.promedio();
        System.out.println(procesador);
        System.out.println("Promedio: " + promedio);
        // Fracciones frac1 = new Fracciones();
        // int num = (int) ((Math.random() * 10) + 1);
        // int deno = (int) ((Math.random() * 10) + 1);
        // Fracciones frac2 = new Fracciones(num, deno);

        // num = (int) ((Math.random() * 10) + 1);
        // frac1.setNumerador(num);
        // deno = (int) ((Math.random() * 10) + 1);
        // frac1.setDenominador(deno); 

        // System.out.println("Fracción: " + frac1);
        // System.out.println("Fracción: " + frac2);

        // Fracciones resultado = frac1.suma(frac2);
        // System.out.println("Resultado de la suma: " + resultado);

        // Fracciones resultadoResta = frac1.resta(frac2);
        // System.out.println("Resultado de la resta: " + resultadoResta);

        // Fracciones resultadoMultiplicacion = frac1.multiplicacion(frac2);
        // System.out.println("Resultado de la multiplicación: " + resultadoMultiplicacion);
    }
}
