package com.encens.khipus.action.restful;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Name("restClientAction")
@Scope(ScopeType.PAGE)
public class RestClientAction {


    public void executeService1(){
        /*Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("https://jsonplaceholder.typicode.com/posts");
        String content = webTarget.request("text/html").get(String.class);
        System.out.println(content);*/

        HttpURLConnection connection = null;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {

            URL url = new URL("https://jsonplaceholder.typicode.com/albums");
            connection = (HttpURLConnection) url.openConnection();

            // Request Setup
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }

            System.out.println(responseContent.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

    }

}
