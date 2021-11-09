package com.encens.khipus.action.restful;

import com.encens.khipus.action.restful.pojo.UserPOJO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Name("restClientAction")
@Scope(ScopeType.PAGE)
public class RestClientAction {


    public void executeService4() throws IOException {


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

    public void executeService3(){

        HttpURLConnection connection = null;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {

            URL url = new URL("https://jsonplaceholder.typicode.com/users");
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

            //System.out.println(responseContent.toString());
            String userJsonSource = responseContent.toString();
            JsonNode node = Json.parse(userJsonSource);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            UserPOJO[] users = objectMapper.readValue(userJsonSource, UserPOJO[].class);
            //List<UserPOJO> userPOJOList = new ArrayList(Arrays.asList(users));
            List<UserPOJO> userPOJOList = objectMapper.readValue(userJsonSource, new TypeReference<List<UserPOJO>>(){});

            System.out.println(Json.prettyPrint(node));

            //userPOJOList.forEach(x -> System.out.println(x.toString()));
            for (int i = 0 ; i < userPOJOList.size() ; i++){
                UserPOJO userPOJO = userPOJOList.get(i);
                System.out.println(userPOJO.getId() + " --> " + userPOJO.getName() + " - " + userPOJO.getEmail());
            }

            /*UserPOJO userPOJO = Json.fromJson(node, UserPOJO.class);

            System.out.println("Id: " + userPOJO.getId());
            System.out.println("Name: " + userPOJO.getName());
            System.out.println("UserName: " + userPOJO.getUsername());
            System.out.println("Email: " + userPOJO.getEmail());*/

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

    }

    public void executeService2() throws IOException {


        URL url = new URL ("https://jsonplaceholder.typicode.com/users");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        String jsonInputString = "{\"name\": \"Upendra\", \"job\": \"Programmer\"}";


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
