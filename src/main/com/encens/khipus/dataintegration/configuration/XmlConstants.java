package com.encens.khipus.dataintegration.configuration;

/**
 * @author
 */
public class XmlConstants {
    public static enum IntegrationsTag {
        TAG_NAME("integrations"),
        ATTR_TIMER_INTERVAL("timer-interval"),
        ATTR_LOCAL_DATA_SOURCE("local-data-source");

        private String constant;

        IntegrationsTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum IntegrationTag {
        TAG_NAME("integration"),
        ATTR_APPLICATION_ID("application-id"),
        ATTR_SERVICE_SEAM_NAME("service-seam-name"),
        ATTR_DATA_SOURCE("data-source");

        private String constant;

        IntegrationTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum TargetTableTag {
        TAG_NAME("targetTable"),
        ATTR_DB_SCHEMA_NAME("db-schema-name"),
        ATTR_DB_TABLE_NAME("db-table-name"),
        ATTR_ALIAS("alias");

        private String constant;

        TargetTableTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum SourceTableTag {
        TAG_NAME("sourceTable"),
        ATTR_DB_SCHEMA_NAME("db-schema-name"),
        ATTR_DB_TABLE_NAME("db-table-name"),
        ATTR_ALIAS("alias");

        private String constant;

        SourceTableTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum ColumnTag {
        TAG_NAME("column"),
        ATTR_DB_COLUMN_NAME("db-column-name"),
        ATTR_ALIAS("alias"),
        ATTR_IS_PRIMARY_KEY("is-primary-key"),
        ATTR_DATA_TYPE("data-type");

        private String constant;

        ColumnTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum MappedColumnTag {
        TAG_NAME("mappedColumn"),
        ATTR_DB_COLUMN_NAME("db-column-name"),
        ATTR_ALIAS("alias"),
        ATTR_SOURCE_COLUMN_ALIAS("source-column-alias"),
        ATTR_IS_PRIMARY_KEY("is-primary-key"),
        ATTR_DATA_TYPE("data-type");

        private String constant;

        MappedColumnTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum DataType {
        STRING, DECIMAL, TIMESTAMP;
    }
}
