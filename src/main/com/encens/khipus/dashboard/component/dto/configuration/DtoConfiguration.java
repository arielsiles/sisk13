package com.encens.khipus.dashboard.component.dto.configuration;

import com.encens.khipus.dashboard.component.dto.configuration.field.DtoField;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.17
 */
public class DtoConfiguration {
    private IdField idField;

    private Map<String, DtoField> config = new HashMap<String, DtoField>();

    private List<String> fieldNames = new ArrayList<String>();

    public static DtoConfiguration getInstance(IdField idField) {
        return new DtoConfiguration(idField);
    }

    private DtoConfiguration(IdField idField) {
        this.idField = idField;
    }

    public DtoConfiguration addField(DtoField field) {
        if (field instanceof Identifier) {
            this.idField = (IdField) field;
        } else {
            fieldNames.add(field.getName());
            config.put(field.getName(), field);
        }

        return this;
    }

    public IdField getIdField() {
        return idField;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public DtoField getField(String fieldName) {
        return config.get(fieldName);
    }
}
