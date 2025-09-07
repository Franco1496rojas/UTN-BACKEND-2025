package com.frc.isi.csv.colecciones.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {
    public static double roundDouble(double valor, int decimales) {
        BigDecimal decimal = BigDecimal.valueOf(valor);
        decimal = decimal.setScale(2, RoundingMode.HALF_UP);
        double valorFinal = decimal.doubleValue();
        return valorFinal;
    }
}

