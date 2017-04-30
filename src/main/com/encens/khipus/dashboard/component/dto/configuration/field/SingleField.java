package com.encens.khipus.dashboard.component.dto.configuration.field;

/**
 * @author
 * @version 2.17
 */
public class SingleField extends DtoField {
    private Integer position;

    public SingleField(String name, Integer position) {
        super.setName(name);
        this.position = position;
    }

    public Integer getPosition() {
        return position;
    }

    public static SingleField getInstance(String name, Integer position) {
        return new SingleField(name, position);
    }
}
