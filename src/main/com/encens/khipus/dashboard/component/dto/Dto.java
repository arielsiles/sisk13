package com.encens.khipus.dashboard.component.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 2.17
 */
public class Dto {
    private Map<String, Object> fields = new HashMap<String, Object>();

    public Object getField(String key) {
        return fields.get(key);
    }

    public void setField(String key, Object value) {
        fields.put(key, value);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public Number getFieldAsNumber(String key) {
        return (Number) fields.get(key);
    }

    public String getFieldAsString(String key) {
        if (fields.get(key) == null) {
            return null;
        }

        if (!(fields.get(key) instanceof String)) {
            return String.valueOf(fields.get(key));
        }

        return (String) fields.get(key);
    }
}
