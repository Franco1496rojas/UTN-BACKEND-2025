package ar.edu.utnfc.backend.anstracciones;

import lombok.ToString;

@ToString

public class ProcesadorFracciones {
    private  final Fracciones[] numeros;
    private int ultimo;

    public ProcesadorFracciones() {
        numeros = new Fracciones[10];
        ultimo = 0;
    }

    public ProcesadorFracciones(int tam) {
        numeros = new Fracciones[tam];
        ultimo = 0;
    }

    public void addFraccion(Fracciones f){
        if (ultimo < numeros.length) {
            this.numeros[ultimo] = f;
            ultimo++;
        }
    }

    public Fracciones promedio(){
        int tam = this.numeros.length;
        if (tam > 0){
            Fracciones acumulador = this.numeros[0];
            Fracciones divisor = new Fracciones(1, tam);
            for (int i = 1; i < tam; i++){
                acumulador = acumulador.suma(this.numeros[i]);
            }
            Fracciones res = acumulador.multiplicacion(divisor);
            return res;
        }
        return new Fracciones();
    }
}
