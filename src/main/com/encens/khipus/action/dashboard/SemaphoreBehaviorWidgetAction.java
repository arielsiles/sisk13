package com.encens.khipus.action.dashboard;

import com.encens.khipus.model.dashboard.Widget;

/**
 * @author
 * @version 3.2
 */
public interface SemaphoreBehaviorWidgetAction {

    Integer firstPosition = 0;
    Integer secondPosition = 1;
    Integer thirdPosition = 2;

    Double getStateMetricValue();

    Double getStateMetricPercentage();

    boolean isInFirstInterval();

    boolean isInSecondInterval();

    boolean isInThirdInterval();

    String getWidgetName();

    Widget getWidget();

    JFreePlotType getJFreePlotType();
}
