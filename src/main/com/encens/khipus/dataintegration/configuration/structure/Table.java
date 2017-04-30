package com.encens.khipus.dataintegration.configuration.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
public class Table {
    private String dbTableName;
    private String alias;
    private String schema;

    private List<Column> columns;

    public String getDbTableName() {
        return dbTableName;
    }

    public void setDbTableName(String dbTableName) {
        this.dbTableName = dbTableName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Column findColumn(String columnAlias) {
        if (null == columns) {
            return null;
        }

        for (Column column : columns) {
            if (column.getAlias().equals(columnAlias)) {
                return column;
            }
        }

        return null;
    }

    public List<Column> getPrimaryKeyColumns() {
        List<Column> result = new ArrayList<Column>();
        if (null == this.columns) {
            return result;
        }

        for (Column column : columns) {
            if (column.isPrimaryKey()) {
                result.add(column);
            }
        }

        return result;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
