package com.encens.khipus.dashboard.component.dto.configuration.field;

/**
 * @author
 * @version 2.17
 */
public class IdField extends SingleField implements Identifier {
    public IdField(String name, Integer position) {
        super(name, position);
    }

    public static IdField getInstance(String name, Integer position) {
        return new IdField(name, position);
    }
}
