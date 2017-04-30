package com.encens.khipus.dashboard.component.dto.factory;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;

/**
 * @author
 * @version 2.17
 */
public class DefaultInstanceBuilder implements InstanceBuilder {
    public Dto buildInstance(Dto cachedDto, Object[] row, DtoConfiguration configuration) {
        IdField idField = configuration.getIdField();

        Dto dto = new Dto();
        dto.setField(idField.getName(), row[idField.getPosition()]);

        for (String name : configuration.getFieldNames()) {
            SingleField singleField = (SingleField) configuration.getField(name);
            dto.setField(singleField.getName(), row[singleField.getPosition()]);
        }

        return dto;
    }
}
