package com.encens.khipus.dataintegration.configuration.structure;

/**
 * @author
 */
public class IntegrationElement {
    private String applicationId;
    private String serviceSeamName;
    private String dataSource;

    private Table targetTable;

    private Table sourceTable;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getServiceSeamName() {
        return serviceSeamName;
    }

    public void setServiceSeamName(String serviceSeamName) {
        this.serviceSeamName = serviceSeamName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Table getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(Table targetTable) {
        this.targetTable = targetTable;
    }

    public Table getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(Table sourceTable) {
        this.sourceTable = sourceTable;
    }
}
