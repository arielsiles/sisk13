package com.encens.khipus.dashboard.configuration.structure;

/**
 * @author
 * @version 2.26
 */
public class XmlInterval extends XmlFilter {

    private int minValue;
    private int maxValue;

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
