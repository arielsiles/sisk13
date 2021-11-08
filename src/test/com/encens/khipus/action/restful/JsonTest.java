package com.encens.khipus.action.restful;

import com.encens.khipus.action.restful.pojo.AuthorPOJO;
import com.encens.khipus.action.restful.pojo.BookPOJO;
import com.encens.khipus.action.restful.pojo.DayPOJO;
import com.encens.khipus.action.restful.pojo.SimpleTestCaseJsonPOJO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    private String simpleTest = "{\n" +
            "  \"title\": \"Coder from khipus\",\n" +
            "  \"author\": \"Rui\"\n" +
            "}";

    private String dayScenario1 = "{\n" +
            "  \"date\": \"2019-12-25\",\n" +
            "  \"name\": \"Christmas Day\"\n" +
            "}";

    private String authorBookScenario = "{\n" +
            "  \"authorName\": \"Rui\",\n" +
            "  \"books\": [\n" +
            "    {\n" +
            "      \"title\": \"title 1\",\n" +
            "      \"inPrint\": true,\n" +
            "      \"publishDate\": \"2019-12-25\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"title\": \"title 2\",\n" +
            "      \"inPrint\": false,\n" +
            "      \"publishDate\": \"2019-01-01\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    void parse() throws IOException {
        JsonNode node = Json.parse(simpleTest);
        System.out.printf("--> " + node.get("title").asText());
        assertEquals(node.get("title").asText(), "Coder from khipus");
    }

    @Test
    void fromJson() throws IOException {
        JsonNode node = Json.parse(simpleTest);
        SimpleTestCaseJsonPOJO pojo = Json.fromJson(node, SimpleTestCaseJsonPOJO.class);
        System.out.println("--> " + pojo.getTitle() + " - " + pojo.getAuthor());
        assertEquals(pojo.getTitle(), "Coder from khipus");
    }

    @Test
    void toJson(){
        SimpleTestCaseJsonPOJO pojo = new SimpleTestCaseJsonPOJO();
        pojo.setTitle("Testing 123");

        JsonNode node = Json.toJson(pojo);
        assertEquals(node.get("title").asText(), "Testing 123");
    }

    @Test
    void stringify() throws JsonProcessingException {
        SimpleTestCaseJsonPOJO pojo = new SimpleTestCaseJsonPOJO();
        pojo.setTitle("Testing 123");
        pojo.setAuthor("Lucas");

        JsonNode node = Json.toJson(pojo);
        System.out.println(Json.stringify(node));
        System.out.println(Json.prettyPrint(node));

    }

    @Test
    void dayTestScenario1() throws IOException {
        JsonNode node = Json.parse(dayScenario1);
        DayPOJO pojo = Json.fromJson(node, DayPOJO.class);

        System.out.println("DATE: " + pojo.getDate());
        assertEquals("2019-12-25", pojo.getDate().toString());
    }

    @Test
    void authorBookScenario1() throws IOException {
        JsonNode node = Json.parse(authorBookScenario);
        AuthorPOJO authorPOJO = Json.fromJson(node, AuthorPOJO.class);

        System.out.println("Author: " + authorPOJO.getAuthorName());
        for (BookPOJO book : authorPOJO.getBooks()){
            System.out.println("Book: " + book.getTitle());
            System.out.println("Is in Print?: " + book.isInPrint());
            System.out.println("Date: " + book.getPublishDate());
        }

    }

}