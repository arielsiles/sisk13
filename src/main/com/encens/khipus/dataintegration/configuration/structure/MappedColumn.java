package com.encens.khipus.dataintegration.configuration.structure;

/**
 * @author
 */
public class MappedColumn extends Column {
    private Column sourceColumn;

    public Column getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(Column sourceColumn) {
        this.sourceColumn = sourceColumn;
    }
}
