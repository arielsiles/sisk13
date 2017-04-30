package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.Name;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Name("graphicViewAction")
public class GraphicViewAction<T extends Graphic> extends ViewAction {
    protected T graphic;

    private Integer containerWidth = 500;

    public String getToolTipMap() {
        return graphic.getToolTipMap();
    }

    public Integer getContainerWidth() {
        return containerWidth;
    }

    public void setContainerWidth(Integer containerWidth) {
        this.containerWidth = containerWidth;
    }

    @Override
    public List<Dto> getResultList() {
        return super.getResultList();
    }

    protected Integer calculateGraphicWidth() {
        return BigDecimalUtil.divide(
                BigDecimalUtil.multiply(new BigDecimal(containerWidth), new BigDecimal("95")),
                new BigDecimal("100")).intValue();
    }

    protected T getGraphic() {
        search();
        graphic.setData(getResultList());
        graphic.setWidth(calculateGraphicWidth());
        setGraphicParameters(graphic);
        return graphic;
    }

    protected void setGraphic(T graphic) {
        this.graphic = graphic;
    }

    protected void setGraphicParameters(T graphic) {

    }

    protected T getGraphicInstance() {
        return graphic;
    }
}
