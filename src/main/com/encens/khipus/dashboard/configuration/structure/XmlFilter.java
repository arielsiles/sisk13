package com.encens.khipus.dashboard.configuration.structure;

/**
 * @author
 * @version 2.26
 */
public abstract class XmlFilter {

    private String name;
    private String description;
    private int index;
    private int color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
