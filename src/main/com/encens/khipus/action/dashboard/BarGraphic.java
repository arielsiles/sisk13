package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.graphics.Graphic;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author
 * @version 3.3
 */
public class BarGraphic extends Graphic {
    private String categoryAxisLabel;
    private String valueAxisLabel;
    private CategoryDataset dataSet;
    private PlotOrientation orientation;

    public byte[] createChart() {
        JFreeChart chart = ChartFactory.createBarChart(null,
                categoryAxisLabel,
                valueAxisLabel,
                dataSet,
                orientation != null ? orientation : PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        setColors(plot);
        OutputStream out = new ByteArrayOutputStream();
        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            ChartUtilities.writeChartAsPNG(out, chart, getWidth(), 300, info);
            toolTipMap = ImageMapUtilities.getImageMap(getChartId(), info);
            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            //log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }

    protected void setColors(CategoryPlot categoryplot) {
        categoryplot.setDomainGridlinesVisible(true);
        categoryplot.setRangeCrosshairVisible(true);
        categoryplot.setRangeCrosshairPaint(Color.blue);
        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
        barrenderer.setDrawBarOutline(false);
        GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.blue, 0.0F, 0.0F, new Color(0, 0, 64));
        GradientPaint gradientpaint1 = new GradientPaint(0.0F, 0.0F, Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
        GradientPaint gradientpaint2 = new GradientPaint(0.0F, 0.0F, Color.red, 0.0F, 0.0F, new Color(64, 0, 0));
        barrenderer.setSeriesPaint(0, gradientpaint);
        barrenderer.setSeriesPaint(1, gradientpaint1);
        barrenderer.setSeriesPaint(2, gradientpaint2);
        barrenderer.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tooltip: {0}"));
        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.52359877559829882D));
    }

    // must be overiden by inheritors
    public String getChartId() {
        throw new UnsupportedOperationException("getChartId method have to be implemented");
    }

    public void setCategoryAxisLabel(String categoryAxisLabel) {
        this.categoryAxisLabel = categoryAxisLabel;
    }

    public void setValueAxisLabel(String valueAxisLabel) {
        this.valueAxisLabel = valueAxisLabel;
    }

    public CategoryDataset getDataSet() {
        return dataSet;
    }

    public void setDataSet(CategoryDataset dataSet) {
        this.dataSet = dataSet;
    }

    public void setOrientation(PlotOrientation orientation) {
        this.orientation = orientation;
    }
}