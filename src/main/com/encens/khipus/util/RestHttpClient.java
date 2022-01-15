 package com.encens.khipus.util;


/*import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;*/

 import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

 class RestHttpClient {


     public static void main(String[] args) throws IOException {

         /*
         Date date = new Date(new Long("1636818442561"));
         System.out.println("Fecha: " + date);

         //String urlcode = "https://pilotosiat.impuestos.gob.bo/consulta/QR?nit=valorNit&cuf=valorCuf&numero=valorNroFactura&t=2";
         String urlcode = "https://pilotosiat.impuestos.gob.bo/facturacionv2/public/Qr.xhtml?nit=valorNit&cuf=valorCuf&numero=valorNroFactura&t=2";

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

         */
         URL url = new URL ("https://jsonplaceholder.typicode.com/posts");
         HttpURLConnection con = (HttpURLConnection) url.openConnection();
         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/json; utf-8");
         con.setRequestProperty("Accept", "application/json");
         con.setDoOutput(true);

         String jsonInputString = "{\n" +
                 "    \"title\": \"Foo\",\n" +
                 "    \"body\": \"bar\",\n" +
                 "    \"userId\": \"1\"\n" +
                 "}";


         OutputStream os = con.getOutputStream();
         byte[] input = jsonInputString.getBytes("utf-8");
         os.write(input, 0, input.length);

         BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
         StringBuilder response = new StringBuilder();
         String responseLine = null;
         while ((responseLine = br.readLine()) != null) {
             response.append(responseLine.trim());
         }
         System.out.println(response.toString());

     }
 }