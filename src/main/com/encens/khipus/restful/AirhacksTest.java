package com.encens.khipus.restful;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class AirhacksTest {

    public static void main(String[] args){

        Client client = ClientBuilder.newClient();
        WebTarget tut = client.target("https://jsonplaceholder.typicode.com/posts");

        String content = tut.request("text/html").get(String.class);

        System.out.println(content);
    }

}
