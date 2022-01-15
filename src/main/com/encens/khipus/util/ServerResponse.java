package com.encens.khipus.util;

public class ServerResponse {

    private Boolean connection;
    private Integer responseCode;
    private String responseJson;

    public ServerResponse(Boolean connection, Integer responseCode, String responseJson) {
        this.connection = connection;
        this.responseCode = responseCode;
        this.responseJson = responseJson;
    }

    public Boolean getConnection() {
        return connection;
    }

    public void setConnection(Boolean connection) {
        this.connection = connection;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }
}
