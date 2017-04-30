package com.encens.khipus.action.cashbox;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.model.dashboard.Filter;
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
public class DebtWidgetGraph extends Graphic {

    private Widget widget;

    public byte[] createChart() {
        MeterPlot plot = getPlot();
        if (null != plot) {
            JFreeChart jfreechart = new JFreeChart(plot);

            ChartUtilities.applyCurrentTheme(jfreechart);

            OutputStream out = new ByteArrayOutputStream();

            try {
                ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

                ChartUtilities.writeChartAsPNG(out, jfreechart, getWidth(), 300, info);

                toolTipMap = ImageMapUtilities.getImageMap("toolTipMap_" + widget.getXmlId(), info);

                return ((ByteArrayOutputStream) out).toByteArray();
            } catch (IOException e) {
                //some exception was happen when graphic its build
            }
        }

        return new byte[]{};
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    private MeterPlot getPlot() {
        DefaultValueDataset dataSet = getDefaultValueDataset();
        if (null != dataSet) {
            MeterPlot plot = new MeterPlot(dataSet);
            plot.setUnits(MessageUtils.getMessage(widget.getUnit().getResourceKey()));
            plot.setBackgroundPaint(Color.WHITE);

            plot.setDrawBorder(true);

            Integer maxValue = 100;
            for (Filter filter : widget.getFilters()) {
                if (filter instanceof Interval) {
                    if (null != ((Interval) filter).getMaxValue() && maxValue < ((Interval) filter).getMaxValue()) {
                        maxValue = ((Interval) filter).getMaxValue();
                    }

                    plot.addInterval(
                            getMeterInterval(filter.getDescription(),
                                    ((Interval) filter).getMinValue(),
                                    ((Interval) filter).getMaxValue(),
                                    filter.getColor())
                    );
                }
            }

            plot.setNeedlePaint(Color.darkGray);
            plot.setDialBackgroundPaint(Color.white);
            plot.setDialOutlinePaint(Color.gray);
            plot.setDialShape(DialShape.CHORD);
            plot.setRange(new Range(0, maxValue));
            plot.setMeterAngle(270);
            plot.setTickLabelsVisible(true);
            plot.setTickLabelFont(new Font("Dialog", Font.BOLD, 10));
            plot.setTickLabelPaint(Color.darkGray);
            plot.setTickSize(10.0);
            plot.setTickPaint(Color.lightGray);

            plot.setValuePaint(Color.black);

            return plot;
        }

        return null;
    }

    private DefaultValueDataset getDefaultValueDataset() {
        if (null != data && !data.isEmpty()) {
            Dto dto = data.get(0);
            return new DefaultValueDataset(dto.getFieldAsNumber("percentage"));
        }

        return null;
    }

    private MeterInterval getMeterInterval(String label,
                                           Number lowerBound,
                                           Number upperBound,
                                           Integer backgroundColor) {
        return new MeterInterval(label,
                new Range(lowerBound.doubleValue(), upperBound.doubleValue()),
                Color.lightGray,
                new BasicStroke(2.0F),
                new Color(backgroundColor));
    }
}
