package com.encens.khipus.action.restful;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class JsonTestMain {

    public static void main(String[] args){

        String jsonSource = "{ \"title\": \"Coder from asdf\" }";

        try {
            JsonNode node = Json.parse(jsonSource);
            System.out.println(node.get("title").asText());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
