package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Interval;
import com.encens.khipus.model.dashboard.Widget;
import com.encens.khipus.service.dashboard.WidgetService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * @author
 * @version 2.26
 */
@Name("widgetViewAction")
public class WidgetViewAction<T extends Graphic> extends GraphicViewAction<T> {

    @In
    private WidgetService widgetService;

    protected Widget widget;

    @Override
    protected void executeService(SqlQuery sqlQuery) {
        executeService(this.widget, sqlQuery);
    }

    protected void executeService(Widget widget, SqlQuery sqlQuery) {
        throw new UnsupportedOperationException("This method should be overwrite in the children classes");
    }

    protected String getXmlWidgetId() {
        throw new UnsupportedOperationException("This method should be overwrite in the children classes");
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    protected Widget findWidget(String xmlWidgetId) {
        Widget resultWidget = widgetService.findByXmlId(xmlWidgetId);
        if (null == resultWidget) {
            resultWidget = widgetService.loadWidget(xmlWidgetId);
        }

        return resultWidget;
    }

    protected void updateWidget(Widget widget) {
        String newTitle = MessageUtils.getMessage(widget.getTitle());
        widget.setTitle(newTitle);
    }

    protected void updateFilters(Widget widget) {
        for (Filter filter : widget.getFilters()) {
            String newDescription = MessageUtils.getMessage(filter.getDescription());
            if (filter instanceof Interval) {
                newDescription = newDescription + "(" + ((Interval) filter).getMinValue() + "," + ((Interval) filter).getMaxValue() + ")";
            }

            filter.setDescription(newDescription);
        }
    }

    protected void initializeWidget() {
        this.widget = findWidget(getXmlWidgetId());
    }
}
