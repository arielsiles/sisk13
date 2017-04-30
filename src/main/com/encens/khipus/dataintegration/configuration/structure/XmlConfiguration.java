package com.encens.khipus.dataintegration.configuration.structure;

import java.util.List;

/**
 * @author
 */
public class XmlConfiguration {
    private Long timerInterval;
    private String localDataSource;
    private List<IntegrationElement> integrationElements;

    public Long getTimerInterval() {
        return timerInterval;
    }

    public void setTimerInterval(Long timerInterval) {
        this.timerInterval = timerInterval;
    }

    public String getLocalDataSource() {
        return localDataSource;
    }

    public void setLocalDataSource(String localDataSource) {
        this.localDataSource = localDataSource;
    }

    public List<IntegrationElement> getIntegrationElements() {
        return integrationElements;
    }

    public void setIntegrationElements(List<IntegrationElement> integrationElements) {
        this.integrationElements = integrationElements;
    }
}
