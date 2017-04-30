package com.encens.khipus.initialize;

/**
 * @author
 * @version 3.0
 */
public class CustomQuartzProcessor {
    String serviceSeamName;

    Integer interval;
    // linux cron syntax
    String intervalCron;
    CustomQuartzProcessorType customQuartzProcessorType;

    public String getServiceSeamName() {
        return serviceSeamName;
    }

    public void setServiceSeamName(String serviceSeamName) {
        this.serviceSeamName = serviceSeamName;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getIntervalCron() {
        return intervalCron;
    }

    public void setIntervalCron(String intervalCron) {
        this.intervalCron = intervalCron;
    }

    public CustomQuartzProcessorType getCustomQuartzProcessorType() {
        return customQuartzProcessorType;
    }

    public void setCustomQuartzProcessorType(CustomQuartzProcessorType customQuartzProcessorType) {
        this.customQuartzProcessorType = customQuartzProcessorType;
    }
}
