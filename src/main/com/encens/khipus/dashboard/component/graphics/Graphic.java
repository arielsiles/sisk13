package com.encens.khipus.dashboard.component.graphics;

import com.encens.khipus.dashboard.component.dto.Dto;

import java.util.List;

/**
 * @author
 * @version 2.17
 */
public abstract class Graphic {
    protected List<Dto> data;

    protected String toolTipMap = "";

    private Integer width = 480;

    public String getToolTipMap() {
        return toolTipMap;
    }

    protected Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        if (null != width) {
            this.width = width;
        }
    }

    public void setData(List<Dto> data) {
        this.data = data;
    }
}
