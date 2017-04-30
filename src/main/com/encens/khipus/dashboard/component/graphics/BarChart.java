package com.encens.khipus.dashboard.component.graphics;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.io.IOException;

/**
 * This class allows to define and build a JFreeChart bar chart in order to be included in
 * the dashboard components.
 *
 * @author
 * @version 1.0
 */
public class BarChart {

    protected static final LogProvider log = Logging.getLogProvider(BarChart.class);

    private String title;
    private String domainLabel;
    private String rangeLabel;
    private CategoryDataset dataSet;


    public BarChart(String title, String domainLabel, String rangeLabel, CategoryDataset dataSet) {
        this.title = title;
        this.domainLabel = domainLabel;
        this.rangeLabel = rangeLabel;
        this.dataSet = dataSet;
    }

    public JFreeChart getChart() {
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                domainLabel,
                rangeLabel,
                dataSet,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // set the range axis to display integers only...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        return chart;

    }

    public byte[] getAsPNG(int width, int height) {
        try {
            return ChartUtilities.encodeAsPNG(getChart().createBufferedImage(width, height));
        } catch (IOException e) {
            log.error("Unexpected error printing the chart", e);
            return null;
        }

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDomainLabel() {
        return domainLabel;
    }

    public void setDomainLabel(String domainLabel) {
        this.domainLabel = domainLabel;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
    }

    public CategoryDataset getDataSet() {
        return dataSet;
    }

    public void setDataSet(CategoryDataset dataSet) {
        this.dataSet = dataSet;
    }
}
