package com.encens.khipus.action.dashboard;

import com.encens.khipus.model.dashboard.Interval;
import com.encens.khipus.model.dashboard.Widget;
import com.encens.khipus.service.dashboard.WidgetService;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import java.lang.reflect.ParameterizedType;

/**
 * @author
 * @version 3.2
 */
@Name("meterWidgetViewAction")
public class MeterWidgetViewAction<T extends MeterGraphic> extends GraphicViewAction<T> implements SemaphoreBehaviorWidgetAction {

    protected long meterValue;
    protected Widget widget;
    public String xmlWidgetId;
    @In
    private WidgetService widgetService;

    @Override
    protected T getGraphic() {
        return super.getGraphic();
    }

    @Override
    protected void setGraphic(T graphic) {
        super.setGraphic(graphic);
    }

    @Override
    protected void setGraphicParameters(T graphic) {
        super.setGraphicParameters(graphic);
    }

    @Override
    protected T getGraphicInstance() {
        return super.getGraphicInstance();
    }

    @Override
    public JFreePlotType getJFreePlotType() {
        return JFreePlotType.METER;
    }

    protected void initialize() {
        T instance = null;
        try {
            instance = (T) ((Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        setGraphic(instance);
        widget = widgetService.findByXmlId(getXmlWidgetId());
        if (widget == null) {
            widget = widgetService.loadWidget(getXmlWidgetId());
        }

        resetFilters();
    }

    protected void resetFilters() {
        refresh();
    }

    protected void refresh() {

    }

    public String getXmlWidgetId() {
        return xmlWidgetId;
    }

    public void setXmlWidgetId(String xmlWidgetId) {
        this.xmlWidgetId = xmlWidgetId;
    }

    public Widget getWidget() {
        return widget;
    }

    public Double getStateMetricValue() {
        return getMeterValue().doubleValue();
    }

    public Double getStateMetricPercentage() {
        return null;
    }

    public boolean isInFirstInterval() {
        Interval interval = (Interval) widget.getFilters().get(firstPosition);
        return null != interval && null != interval.getMaxValue()
                && meterValue <= (interval.getMaxValue());
    }

    public boolean isInSecondInterval() {
        Interval interval = (Interval) widget.getFilters().get(secondPosition);
        return null != interval && null != interval.getMaxValue()
                && meterValue >= interval.getMinValue() && meterValue <= (interval.getMaxValue());
    }

    public boolean isInThirdInterval() {
        Interval interval = (Interval) widget.getFilters().get(thirdPosition);
        return null != interval && null != interval.getMaxValue()
                && meterValue >= interval.getMinValue();
    }

    //Must be implemented in ChildrenMetaDataRepositoryVisitor classes
    public String getWidgetName() {
        throw new UnsupportedOperationException();
    }

    public Long getMeterValue() {
        return meterValue;
    }

}
