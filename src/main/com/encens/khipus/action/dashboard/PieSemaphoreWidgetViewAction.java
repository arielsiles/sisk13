package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.2
 */
public class PieSemaphoreWidgetViewAction<T extends PieGraphic> extends PieWidgetViewAction<T> implements SemaphoreBehaviorWidgetAction {
    private static final String VALUE = "value";
    protected Double stateMetricValue;
    protected Long firstSectionValue;
    protected Long secondSectionValue;
    protected Long thirdSectionValue;

    @Override
    public Double getStateMetricValue() {
        stateMetricValue = isInFirstInterval() ? firstSectionValue.doubleValue() : isInSecondInterval() ? secondSectionValue.doubleValue() : thirdSectionValue.doubleValue();

        return stateMetricValue;
    }

    public Double getStateMetricPercentage() {
        if (stateMetricValue == 0) {
            return 0.0;
        }
        return stateMetricValue / (firstSectionValue + secondSectionValue + thirdSectionValue) * 100;
    }

    @Override
    public boolean isInFirstInterval() {
        loadResultMapValues();
        return firstSectionValue.equals(secondSectionValue) && secondSectionValue.equals(thirdSectionValue) && firstSectionValue == 0
                || firstSectionValue > secondSectionValue && firstSectionValue > thirdSectionValue;
    }

    @Override
    public boolean isInSecondInterval() {
        return secondSectionValue >= firstSectionValue && secondSectionValue > thirdSectionValue;
    }

    @Override
    public boolean isInThirdInterval() {
        return thirdSectionValue >= firstSectionValue && thirdSectionValue >= secondSectionValue;
    }

    private void loadResultMapValues() {
        List<Number> sectionsList = new ArrayList<Number>();

        for (Map.Entry<String, List<Dto>> entry : getResultMap().entrySet()) {
            Number number = entry.getValue().get(0).getFieldAsNumber(VALUE);
            sectionsList.add(number);
        }
        firstSectionValue = sectionsList.get(0).longValue();
        secondSectionValue = sectionsList.get(1).longValue();
        thirdSectionValue = sectionsList.get(2).longValue();

    }

    @Override
    //Must be implemented in ChildrenMetaDataRepositoryVisitor classes
    public String getWidgetName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JFreePlotType getJFreePlotType() {
        return JFreePlotType.PIE;
    }

}
