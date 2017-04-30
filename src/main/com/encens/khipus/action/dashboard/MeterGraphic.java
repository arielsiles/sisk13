package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.model.dashboard.Interval;
import com.encens.khipus.model.dashboard.Widget;
import com.encens.khipus.util.MessageUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author
 * @version 2.26
 */
public class MeterGraphic extends Graphic {

    private long meterValue = 0;
    private Widget widget;

    public byte[] createChart() {
        int rangeValue1 = ((Interval) widget.getFilters().get(0)).getMinValue();
        int rangeValue2 = ((Interval) widget.getFilters().get(0)).getMaxValue();
        int rangeValue3 = ((Interval) widget.getFilters().get(1)).getMinValue();
        int rangeValue4 = ((Interval) widget.getFilters().get(1)).getMaxValue();
        int rangeValue5 = ((Interval) widget.getFilters().get(2)).getMinValue();
        int rangeValue6 = ((Interval) widget.getFilters().get(2)).getMaxValue();

        int minRange = Math.min(rangeValue1, Math.min(rangeValue2, Math.min(rangeValue3, Math.min(rangeValue4, Math.min(rangeValue5, rangeValue6)))));
        int maxRange = Math.max(rangeValue1, Math.max(rangeValue2, Math.max(rangeValue3, Math.max(rangeValue4, Math.max(rangeValue5, rangeValue6)))));

        String intervalLabel1 = widget.getFilters().get(0).getDescription();
        String intervalLabel2 = widget.getFilters().get(1).getDescription();
        String intervalLabel3 = widget.getFilters().get(2).getDescription();

        Color color1 = new Color(widget.getFilters().get(0).getColor());
        Color color2 = new Color(widget.getFilters().get(1).getColor());
        Color color3 = new Color(widget.getFilters().get(2).getColor());

        if (meterValue < minRange) {
            meterValue = minRange;
        }
        if (meterValue > maxRange) {
            meterValue = maxRange;
        }

        DefaultValueDataset dataSet = new DefaultValueDataset(meterValue);
        MeterPlot meterplot = new MeterPlot(dataSet);
        meterplot.setUnits(MessageUtils.getMessage(widget.getUnit().getResourceKey()));
        meterplot.setRange(new Range(rangeValue1, rangeValue4));
        meterplot.addInterval(new MeterInterval(intervalLabel1, new Range(rangeValue1, rangeValue2), Color.lightGray, new BasicStroke(2.0F), color1));
        meterplot.addInterval(new MeterInterval(intervalLabel2, new Range(rangeValue3, rangeValue4), Color.lightGray, new BasicStroke(2.0F), color2));
        meterplot.addInterval(new MeterInterval(intervalLabel3, new Range(rangeValue5, rangeValue6), Color.lightGray, new BasicStroke(2.0F), color3));
        meterplot.setNeedlePaint(Color.DARK_GRAY);
        meterplot.setDialBackgroundPaint(Color.BLACK);
        meterplot.setDialOutlinePaint(Color.GRAY);
        meterplot.setTickLabelPaint(Color.DARK_GRAY);
        meterplot.setTickPaint(Color.LIGHT_GRAY);
        meterplot.setValuePaint(Color.BLACK);
        meterplot.setDialShape(DialShape.CHORD);
        meterplot.setTickSize(1);
        meterplot.setMeterAngle(270);
        meterplot.setRange(new Range(minRange, maxRange));
        meterplot.setTickLabelsVisible(true);
        meterplot.setTickLabelFont(new Font("Dialog", 1, 10));
        meterplot.setValueFont(new Font("Dialog", 1, 14));
        meterplot.setDrawBorder(true);
        JFreeChart jfreechart = new JFreeChart(meterplot);

        ChartUtilities.applyCurrentTheme(jfreechart);

        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, jfreechart, getWidth(), 300, info);

            toolTipMap = ImageMapUtilities.getImageMap("warehouseMonthlyCloseChartId", info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
        }

        return new byte[]{};
    }

    public void setMeterValue(long meterValue) {
        this.meterValue = meterValue;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }
}
