package com.encens.khipus.util;

import java.math.BigDecimal;

public class MoneyNumberUtil {

    private static final String[] UNIDADES = {"", "Uno", "Dos", "Tres", "Cuatro", "Cinco", "Seis", "Siete", "Ocho", "Nueve"};
    private static final String[] ESPECIALES = {"Diez", "Once", "Doce", "Trece", "Catorce", "Quince", "Dieciséis", "Diecisiete", "Dieciocho", "Diecinueve"};
    private static final String[] DECENAS = {"", "", "Veinte", "Treinta", "Cuarenta", "Cincuenta", "Sesenta", "Setenta", "Ochenta", "Noventa"};
    private static final String[] CENTENAS = {"", "Ciento", "Doscientos", "Trescientos", "Cuatrocientos", "Quinientos", "Seiscientos", "Setecientos", "Ochocientos", "Novecientos"};

    public static String convertirNumeroALetras(BigDecimal numero) {
        // Separar la parte entera y la parte decimal
        String[] partes = numero.setScale(2, BigDecimal.ROUND_HALF_UP).toString().split("\\.");
        int parteEntera = Integer.parseInt(partes[0]);
        int parteFraccionaria = Integer.parseInt(partes[1]);

        // Convertir la parte entera a letras
        String resultado = convertirParteEnteraALetras(parteEntera);

        // Añadir los centavos como fracción de 100
        if (parteFraccionaria > 0) {
            resultado += " " + parteFraccionaria + "/100";
        }

        return resultado;
    }

    private static String convertirParteEnteraALetras(int numero) {
        if (numero == 0) {
            return "Cero";
        }

        String palabras = "";
        int miles = 0;

        while (numero > 0) {
            int grupo = numero % 1000;
            if (grupo > 0) {
                String grupoPalabras = convertirGrupoALetras(grupo);

                if (miles == 1) {
                    palabras = (grupo == 1 ? "Mil " : grupoPalabras + " Mil ") + palabras;
                } else if (miles == 2) {
                    palabras = (grupo == 1 ? "Un millón " : grupoPalabras + " Millones ") + palabras;
                } else {
                    palabras = grupoPalabras + " " + palabras;
                }
            }

            numero /= 1000;
            miles++;
        }

        return palabras.trim();
    }

    private static String convertirGrupoALetras(int numero) {
        String palabras = "";

        if (numero >= 100) {
            if (numero == 100) {
                palabras += "Cien";
            } else {
                palabras += CENTENAS[numero / 100] + " ";
            }
            numero %= 100;
        }

        if (numero >= 20) {
            palabras += DECENAS[numero / 10];
            if (numero % 10 != 0) {
                palabras += " y " + UNIDADES[numero % 10];
            }
        } else if (numero >= 10) {
            palabras += ESPECIALES[numero - 10];
        } else {
            palabras += UNIDADES[numero];
        }

        return palabras.trim();
    }

}
