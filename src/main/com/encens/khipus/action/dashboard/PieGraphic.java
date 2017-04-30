package com.encens.khipus.action.dashboard;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.model.dashboard.Filter;
import com.encens.khipus.model.dashboard.Widget;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.26
 */
public class PieGraphic extends Graphic {
    private Map<String, List<Dto>> data;

    private Widget widget;

    public void setData(Map<String, List<Dto>> data) {
        this.data = data;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public byte[] createChart() {
        DefaultPieDataset dataSet = getDataSet();

        JFreeChart chart = ChartFactory.createPieChart(null,
                dataSet,
                true,
                true,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();

        setColors(plot);

        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{1} ({2})"));
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setCircular(false);
        plot.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);

        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, chart, getWidth(), 300, info);

            toolTipMap = ImageMapUtilities.getImageMap("toolTipMap_" + widget.getXmlId(), info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            //log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }

    protected DefaultPieDataset getDataSet() {
        DefaultPieDataset dataSet = new DefaultPieDataset();
        if (null != data) {
            for (Filter filter : widget.getFilters()) {
                List<Dto> value = data.get(filter.getName());
                if (null != value && !value.isEmpty()) {
                    Number number = value.get(0).getFieldAsNumber("value");

                    dataSet.setValue(filter.getDescription(), number);
                }
            }
        }

        return dataSet;
    }

    protected void setColors(PiePlot plot) {
        for (Filter filter : widget.getFilters()) {
            plot.setSectionPaint(filter.getDescription(), new Color(filter.getColor()));
        }
    }
}
