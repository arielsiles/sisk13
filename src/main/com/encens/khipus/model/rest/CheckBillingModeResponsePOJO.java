package com.encens.khipus.model.rest;


/**
 * Created by Admin on 8/12/2021.
 */
public class CheckBillingModeResponsePOJO {

    private Boolean isOnline;
    private Integer eventCode;
    private Long startDate;
    private String description;
    private Integer offlineBills;

    public Boolean getIsOnline() {
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

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOfflineBills() {
        return offlineBills;
    }

    public void setOfflineBills(Integer offlineBills) {
        this.offlineBills = offlineBills;
    }
}
