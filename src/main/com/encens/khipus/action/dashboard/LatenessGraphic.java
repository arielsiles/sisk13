package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * @author
 * @version 3.3
 */
public class LatenessGraphic extends BarGraphic {

    private static final String LATENESS_CHART_ID = "latenesschartid";

    @Override
    public String getChartId() {
        return LATENESS_CHART_ID;
    }

    @Override
    public byte[] createChart() {
        createDataSet();
        return super.createChart();
    }

    public void createDataSet() {
        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
        // value category and serie
        for (Dto dto : data) {
            defaultcategorydataset.addValue(dto.getFieldAsNumber("id"), dto.getFieldAsString("organizationalUnit"), dto.getFieldAsString("costCenter"));
        }
        setDataSet(defaultcategorydataset);
    }

}
