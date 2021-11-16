 package com.encens.khipus.util;


/*import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;*/

 import java.util.Date;

 class RestHttpClient {


     public static void main(String[] args){

         Date date = new Date(new Long("1636818442561"));
         System.out.println("Fecha: " + date);

         String urlcode = "https://pilotosiat.impuestos.gob.bo/consulta/QR?nit=valorNit&cuf=valorCuf&numero=valorNroFactura&t=2";

         String paramNit = "valorNit";
         String paramCuf = "valorCuf";
         String paramNroFactura = "valorNroFactura";

         String valorNit = "1008741021";
         String valorCuf = "4505572E36725E5655372E9E23D87A1367F845DF6379F85BA6F6EDC74";
         String valorNroFactura = "111";

         String newUrlCode = urlcode.replaceAll(paramNit, valorNit);
         newUrlCode = newUrlCode.replaceAll(paramCuf, valorCuf);
         newUrlCode = newUrlCode.replaceAll(paramNroFactura, valorNroFactura);

         System.out.println("---> " + urlcode);
         System.out.println("---> " + newUrlCode);

     }
 }