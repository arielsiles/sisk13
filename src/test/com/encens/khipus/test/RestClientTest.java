package com.encens.khipus.test;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestClientTest {

    private Client client;
    private WebTarget tut;

    @Before
    public void initClient(){
        this.client = ClientBuilder.newClient();
        this.tut = this.client.target("http://airhacks.com");
    }

    @Test
    public void isJavaEEWorkshop(){
        String content = this.tut.request("text/html").get(String.class);
        assertTrue(content.contains("java"));
        assertFalse(content.contains("scala"));
    }


}
