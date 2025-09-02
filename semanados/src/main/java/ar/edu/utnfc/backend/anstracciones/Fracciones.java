package ar.edu.utnfc.backend.anstracciones;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Fracciones {
    private int numerador;
    private int denominador;

    public Fracciones suma(Fracciones otrFracciones) {
        Fracciones fracciones = new Fracciones();
        fracciones.numerador = this.numerador * otrFracciones.denominador + otrFracciones.numerador * this.denominador;
        fracciones.setDenominador(this.denominador * otrFracciones.denominador);
        return fracciones;
    }

    public Fracciones resta(Fracciones otrFracciones) {
        Fracciones fracciones = new Fracciones();
        fracciones.numerador = (this.numerador * otrFracciones.denominador) - (otrFracciones.numerador * this.denominador);
        fracciones.setDenominador(this.denominador * otrFracciones.denominador);
        return fracciones;
    }

    public Fracciones multiplicacion(Fracciones otrFracciones) {
        Fracciones fracciones = new Fracciones();
        fracciones.numerador = this.numerador * otrFracciones.numerador;
        fracciones.setDenominador(this.denominador * otrFracciones.denominador);
        return fracciones;
    }

    /* 
    public Fracciones() {
        this.numerador = 0;
        this.denominador = 1; // Denominador no puede ser cero
    }

    public Fracciones(int num, int deno){
        numerador = num;
        denominador = deno;
    }

    public int getNumerador (){
        return numerador;
    }

    public void setNumerador(int num) {
        numerador = num;
    }

    public int getDenominador (){
        return denominador;
    }

    public void setDenominador(int deno) {
        denominador = deno;
    }       

    @Override
    public String toString() {
        return "Fraccionario: {" + numerador + "/" + denominador + "}";
    }
    */
}
