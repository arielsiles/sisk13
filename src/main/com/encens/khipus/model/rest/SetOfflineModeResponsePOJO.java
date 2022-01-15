package com.encens.khipus.model.rest;

/**
 * Created by Admin on 3/12/2021.
 */
public class SetOfflineModeResponsePOJO {

    private Boolean isOnline;
    private Integer eventCode;
    private String description;
    private Long startDate;

    public SetOfflineModeResponsePOJO(Boolean isOnline, Integer eventCode, String description, Long startDate) {
        this.isOnline = isOnline;
        this.eventCode = eventCode;
        this.description = description;
        this.startDate = startDate;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public Integer getEventCode() {
        return eventCode;
    }

    public void setEventCode(Integer eventCode) {
        this.eventCode = eventCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }
}
