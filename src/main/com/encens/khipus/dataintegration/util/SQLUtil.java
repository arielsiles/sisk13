package com.encens.khipus.dataintegration.util;

import com.encens.khipus.dataintegration.configuration.XmlConstants;
import com.encens.khipus.dataintegration.configuration.structure.Column;
import com.encens.khipus.dataintegration.configuration.structure.MappedColumn;
import com.encens.khipus.dataintegration.configuration.structure.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that provides methods to generate sql <code>(SELECT, INSERT, DELETE)</code> sentences.
 *
 * @author
 */
public class SQLUtil {

    public static SQLUtil i = new SQLUtil();

    private SQLUtil() {

    }

    public String generateSelectSQL(Table table) {
        String sql = "SELECT ";

        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            sql += column.getDbColumnName() + " AS " + column.getAlias();

            if (i < columns.size() - 1) {
                sql += ", ";
            }
        }

        sql += " FROM " + table.getSchema() + "." + table.getDbTableName() + "";
        return sql;
    }

    public List<String> generateInsertSQL(Table targetTable, List<Map<String, String>> sourceData, String identifierQuote) {
        List<String> result = new ArrayList<String>();

        for (Map<String, String> row : sourceData) {
            result.add(generateSingleInsertSQL(targetTable, row, identifierQuote));
        }

        return result;
    }

    public String generateDeleteSQL(Table sourceTable, List<Map<String, String>> sourceData) {
        String sql = "DELETE FROM " + sourceTable.getSchema() + "." + sourceTable.getDbTableName() + " WHERE ";

        List<Column> primaryKeys = sourceTable.getPrimaryKeyColumns();

        int j = 0;
        for (Map<String, String> row : sourceData) {
            sql += "(";
            for (int i = 0; i < primaryKeys.size(); i++) {
                Column keyColumn = primaryKeys.get(i);

                Object value = row.get(keyColumn.getAlias());

                sql += sourceTable.getDbTableName() + "." + keyColumn.getDbColumnName() + "=" + value.toString();

                if (i < primaryKeys.size() - 1) {
                    sql += " AND ";
                }
            }
            sql += ") ";

            if (j < sourceData.size() - 1) {
                sql += " OR ";
            }

            j++;
        }

        sql += "";

        return sql;
    }

    private String generateSingleInsertSQL(Table targetTable, Map<String, String> row, String identifierQuote) {
        String sql = "INSERT INTO " + targetTable.getSchema() + "." + targetTable.getDbTableName() + "(";
        for (int i = 0; i < targetTable.getColumns().size(); i++) {
            MappedColumn mappedColumn = (MappedColumn) targetTable.getColumns().get(i);

            sql += identifierQuote + mappedColumn.getDbColumnName() + identifierQuote;
            if (i < targetTable.getColumns().size() - 1) {
                sql += ", ";
            }
        }

        sql += ") VALUES (";

        for (int i = 0; i < targetTable.getColumns().size(); i++) {
            MappedColumn mappedColumn = (MappedColumn) targetTable.getColumns().get(i);
            Object value = row.get(mappedColumn.getSourceColumn().getAlias());

            String valueAsString = "NULL";
            if (null != value) {
                if (XmlConstants.DataType.DECIMAL.name().equals(mappedColumn.getDataType())) {
                    valueAsString = " " + value.toString() + "";
                } else if (XmlConstants.DataType.STRING.name().equals(mappedColumn.getDataType())) {
                    valueAsString = " '" + value.toString() + "'";
                } else {
                    valueAsString = mappedColumn.getDataType() + " '" + value.toString() + "'";
                }
            }
            sql += valueAsString;
            if (i < targetTable.getColumns().size() - 1) {
                sql += ", ";
            }

        }

        sql += ")\n";
        return sql;
    }
}
