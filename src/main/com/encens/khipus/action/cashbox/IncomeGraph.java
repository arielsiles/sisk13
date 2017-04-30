package com.encens.khipus.action.cashbox;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.dashboard.module.cashbox.sql.IncomeSql;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.SortOrder;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.18
 */
public class IncomeGraph extends Graphic {
    private Map<IncomeSql.SqlToExecute, List<Dto>> resultMap = new HashMap<IncomeSql.SqlToExecute, List<Dto>>();

    public void setResultMap(Map<IncomeSql.SqlToExecute, List<Dto>> resultMap) {
        this.resultMap = resultMap;
    }

    public byte[] createChart() {
        String usdSymbol = MessageUtils.getMessage("Dashboard.symbol.usd");

        JFreeChart jfreechart = ChartFactory.createBarChart(null,
                MessageUtils.getMessage("Income.xLabel.month"),
                MessageUtils.getMessage("Income.yLabel.amount"),
                createDataset(),
                PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
        categoryplot.setDomainGridlinesVisible(true);

        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        LayeredBarRenderer layeredbarrenderer = new LayeredBarRenderer();
        layeredbarrenderer.setDrawBarOutline(false);
        layeredbarrenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{0} [" + usdSymbol + "]: {2}",
                new DecimalFormat(MessageUtils.getMessage("patterns.decimalNumber"))));

        categoryplot.setRenderer(layeredbarrenderer);
        categoryplot.setRowRenderingOrder(SortOrder.DESCENDING);
        GradientPaint gradientPaint = new GradientPaint(0.0F, 0.0F, Color.blue, 0.0F, 0.0F, new Color(0, 0, 64));
        GradientPaint gradientPaint1 = new GradientPaint(0.0F, 0.0F, Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
        GradientPaint gradientPaint2 = new GradientPaint(0.0F, 0.0F, Color.red, 0.0F, 0.0F, new Color(64, 0, 0));
        layeredbarrenderer.setSeriesPaint(0, gradientPaint);
        layeredbarrenderer.setSeriesPaint(1, gradientPaint1);
        layeredbarrenderer.setSeriesPaint(2, gradientPaint2);
        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, jfreechart, getWidth(), 300, info);

            toolTipMap = ImageMapUtilities.getImageMap("invoiceChartId", info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            //log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        Map<String, Number> exchangeRateMap = new HashMap<String, Number>();

        BigDecimal lastExchangeRate = BigDecimal.ONE;
        List<Dto> invoiceDtoList = resultMap.get(IncomeSql.SqlToExecute.INVOICE);
        for (int i = 0; i < invoiceDtoList.size(); i++) {
            Dto dto = invoiceDtoList.get(i);
            exchangeRateMap.put(dto.getFieldAsString("monthName"), dto.getFieldAsNumber("exchangeRate"));
            if (i < invoiceDtoList.size() - 1) {
                lastExchangeRate = (BigDecimal) dto.getFieldAsNumber("exchangeRate");
            }
        }

        for (IncomeSql.SqlToExecute sqlToExecute : IncomeSql.getSqlToExecuteConstants()) {
            List<Dto> data = resultMap.get(sqlToExecute);

            for (Dto dto : data) {
                BigDecimal graphicValue = (BigDecimal) dto.getFieldAsNumber("graphicValue");
                String monthName = dto.getFieldAsString("monthName");

                if (null != graphicValue && IncomeSql.SqlToExecute.BUDGET.equals(sqlToExecute)) {
                    BigDecimal exchangeRate = (BigDecimal) exchangeRateMap.get(monthName);
                    if (null == exchangeRate) {
                        exchangeRate = lastExchangeRate;
                    }

                    graphicValue = BigDecimalUtil.divide(graphicValue, exchangeRate);
                }

                dataSet.addValue(graphicValue,
                        MessageUtils.getMessage(sqlToExecute.getResourceKey()),
                        monthName);
            }
        }

        return dataSet;
    }
}
