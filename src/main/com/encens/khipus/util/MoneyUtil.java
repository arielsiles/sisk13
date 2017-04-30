package com.encens.khipus.util;

import com.encens.khipus.model.customers.ControlCode;
import org.osbosoftware.facturas.CodigoControl7;
import org.osbosoftware.facturas.core.AllegedRC4;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 10/10/13
 * Time: 20:01
 * To change this template use File | Settings | File Templates.
 */

/**
 * @web http://jc-mouse.blogspot.com/
 * @author Mouse
 */
public class MoneyUtil{

    private final String[] UNIDADES = {"", "un ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", "siete ", "ocho ", "nueve "};
    private final String[] DECENAS = {"diez ", "once ", "doce ", "trece ", "catorce ", "quince ", "dieciseis ",
            "diecisiete ", "dieciocho ", "diecinueve", "veinte ", "treinta ", "cuarenta ",
            "cincuenta ", "sesenta ", "setenta ", "ochenta ", "noventa "};
    private final String[] CENTENAS = {"", "ciento ", "doscientos ", "trecientos ", "cuatrocientos ", "quinientos ", "seiscientos ",
            "setecientos ", "ochocientos ", "novecientos "};

    public MoneyUtil() {
    }

    public String Convertir(String numero, boolean mayusculas) {
        String literal = "";
        String parte_decimal;
        //si el numero utiliza (.) en lugar de (,) -> se reemplaza
        numero = numero.replace(".", ",");
        //si el numero no tiene parte decimal, se le agrega ,00
        if(numero.indexOf(",")==-1){
            numero = numero + ",00";
        }
        //se valida formato de entrada -> 0,00 y 999 999 999,00
        if (Pattern.matches("\\d{1,9},\\d{1,2}", numero)) {
            //se divide el numero 0000000,00 -> entero y decimal
            String Num[] = numero.split(",");
            //de da formato al numero decimal
            parte_decimal = ( Integer.parseInt(Num[1]) < 10 ? "0" : "") + Num[1] + "/100 Bolivianos.";

            if ( (Integer.parseInt(Num[1]) < 10) && (Integer.parseInt(Num[1]) > 0) )
                parte_decimal = "0" + Num[1] + "/100 Bolivianos.";
            else
                parte_decimal = "" + Num[1] + "/100 Bolivianos.";

            //se convierte el numero a literal
            if (Integer.parseInt(Num[0]) == 0) {//si el valor es cero
                literal = "cero ";
            } else if (Integer.parseInt(Num[0]) > 999999) {//si es millon
                literal = getMillones(Num[0]);
            } else if (Integer.parseInt(Num[0]) > 999) {//si es miles
                literal = getMiles(Num[0]);
            } else if (Integer.parseInt(Num[0]) > 99) {//si es centena
                literal = getCentenas(Num[0]);
            } else if (Integer.parseInt(Num[0]) > 9) {//si es decena
                literal = getDecenas(Num[0]);
            } else {//sino unidades -> 9
                literal = getUnidades(Num[0]);
            }
            //devuelve el resultado en mayusculas o minusculas
            if (mayusculas) {
                return (literal + " " + parte_decimal).toUpperCase();
            } else {
                return (literal + " " + parte_decimal);
            }
        } else {//error, no se puede convertir
            return literal = null;
        }
    }

    /* funciones para convertir los numeros a literales */

    private String getUnidades(String numero) {// 1 - 9
        //si tuviera algun 0 antes se lo quita -> 09 = 9 o 009=9
        String num = numero.substring(numero.length() - 1);
        return UNIDADES[Integer.parseInt(num)];
    }

    private String getDecenas(String num) {// 99
        int n = Integer.parseInt(num);
        if (n < 10) {//para casos como -> 01 - 09
            return getUnidades(num);
        } else if (n > 19) {//para 20...99
            String u = getUnidades(num);
            if (u.equals("")) { //para 20,30,40,50,60,70,80,90
                return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8];
            } else {
                return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8] + "y " + u;
            }
        } else {//numeros entre 11 y 19
            return DECENAS[n - 10];
        }
    }

    private String getCentenas(String num) {// 999 o 099
        if( Integer.parseInt(num)>99 ){//es centena
            if (Integer.parseInt(num) == 100) {//caso especial
                return " cien ";
            } else {
                return CENTENAS[Integer.parseInt(num.substring(0, 1))] + getDecenas(num.substring(1));
            }
        }else{//por Ej. 099
            //se quita el 0 antes de convertir a decenas
            return getDecenas(Integer.parseInt(num)+"");
        }
    }

    private String getMiles(String numero) {// 999 999
        //obtiene las centenas
        String c = numero.substring(numero.length() - 3);
        //obtiene los miles
        String m = numero.substring(0, numero.length() - 3);
        String n="";
        //se comprueba que miles tenga valor entero
        if (Integer.parseInt(m) > 0) {
            n = getCentenas(m);
            return n + "mil " + getCentenas(c);
        } else {
            return "" + getCentenas(c);
        }

    }

    private String getMillones(String numero) { //000 000 000
        //se obtiene los miles
        String miles = numero.substring(numero.length() - 6);
        //se obtiene los millones
        String millon = numero.substring(0, numero.length() - 6);
        String n = "";
        if(millon.length()>1){
            n = getCentenas(millon) + "millones ";
        }else{
            n = getUnidades(millon) + "millon ";
        }
        return n + getMiles(miles);
    }

    public String getCodigoDeControl(String dosificacion)
    {
        String codigo= "";
        try {
            AllegedRC4 allegedRC4 = new AllegedRC4();
            codigo = allegedRC4.encripta("18isw",dosificacion);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return codigo;
    }

    public String getLlaveQR(String numeroAutorizacion,BigDecimal numeroFactura,String nitCi, Date fecha, Double monto,String llaveDosificacion)
    {
        String llave= "";
        try {
            CodigoControl7 cc7 = new CodigoControl7();
            cc7.setNumeroAutorizacion(numeroAutorizacion);
            cc7.setNumeroFactura(numeroFactura.longValue());
            cc7.setNitci(nitCi);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date fechaEmision = new Date();
            try {
               fechaEmision = dateFormat.parse(dateFormat.format(fecha));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cc7.setFechaTransaccion(fechaEmision );
            cc7.setMonto(monto.intValue());
            cc7.setLlaveDosificacion(llaveDosificacion);
            llave =cc7.obtener();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return llave;
    }

    public void getLlaveQR(ControlCode controlCode,String llaveDosificacion)
    {
        String llave= "";
        try {
            CodigoControl7 cc7 = new CodigoControl7();
            cc7.setNumeroAutorizacion(controlCode.getNumeroAutorizacion());
            cc7.setNumeroFactura(controlCode.getNumberInvoice());
            cc7.setNitci(controlCode.getNitCliente());
            cc7.setFechaTransaccion(controlCode.getFechaEmision());
            cc7.setMonto(controlCode.getTotal().intValue());
            cc7.setLlaveDosificacion(llaveDosificacion);
            controlCode.setCodigoControl(cc7.obtener());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

}
