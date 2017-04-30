package com.encens.khipus.util.barcode;

import java.io.Serializable;

/**
 * @author
 * @version 3.1
 */
public class BarcodeData implements Serializable {
    private String text;

    private Integer width;

    private Integer height;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
