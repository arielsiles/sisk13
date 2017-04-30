package com.encens.khipus.dashboard.configuration;

/**
 * @author
 * @version 2.26
 */
public class XmlConstants {

    public static enum DashboardTag {
        TAG_NAME("dashboard");

        private String constant;

        DashboardTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum WidgetTag {
        TAG_NAME("widget"),
        ATTR_ID("id"),
        ATTR_TITLE("title"),
        ATTR_COMPONENT_NAME("componentName"),
        ATTR_AREA("area"),
        ATTR_MODULE("module"),
        ATTR_FUNCTION("function"),
        ATTR_VERIFICATION("verification"),
        ATTR_UNIT("unit");

        private String constant;

        WidgetTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }

    public static enum IntervalTag {
        TAG_NAME("interval"),
        ATTR_NAME("name"),
        ATTR_DESCRIPTION("description"),
        ATTR_INDEX("index"),
        ATTR_COLOR("color"),
        ATTR_MIN_VALUE("minValue"),
        ATTR_MAX_VALUE("maxValue");

        private String constant;

        IntervalTag(String constant) {
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }
}
