package com.encens.khipus.action.finances;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.dashboard.module.finances.sql.BankBalanceSql;
import com.encens.khipus.util.MessageUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.21.3
 */
public class BankBalanceGraph extends Graphic {
    private Map<BankBalanceSql.BankBalanceType, List<Dto>> resultMap = new HashMap<BankBalanceSql.BankBalanceType, List<Dto>>();

    public byte[] createChart() {

        JFreeChart jfreechart = createChart(MessageUtils.getMessage("BankBalance.company"),
                BankBalanceSql.BankBalanceType.COMPANY);

        ChartUtilities.applyCurrentTheme(jfreechart);
        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, jfreechart, getWidth(), 300, info);

            toolTipMap = ImageMapUtilities.getImageMap("companyBalanceChartId", info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            //log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }


    public byte[] createBankChart() {
        JFreeChart jfreechart = createChart(MessageUtils.getMessage("BankBalance.bank"),
                BankBalanceSql.BankBalanceType.BANK);

        ChartUtilities.applyCurrentTheme(jfreechart);

        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, jfreechart, getWidth(), 300, info);

            toolTipMap += ImageMapUtilities.getImageMap("bankBalanceChartId", info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            //log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }

    private JFreeChart createChart(String title, BankBalanceSql.BankBalanceType type) {
        JFreeChart jfreechart = ChartFactory.createBarChart(title,
                MessageUtils.getMessage("BankBalance.yLabel.month"),
                MessageUtils.getMessage("BankBalance.xLabel.quantity"),
                createDataset(type),
                PlotOrientation.VERTICAL, true, true, false);

        jfreechart.getLegend().setPosition(RectangleEdge.RIGHT);
        jfreechart.getTitle().setFont(new Font("SansSerif", 1, 10));

        jfreechart.getCategoryPlot().getRenderer().setBaseToolTipGenerator(
                new StandardCategoryToolTipGenerator("{0}: {2}",
                        new DecimalFormat(MessageUtils.getMessage("patterns.decimalNumber")))
        );
        return jfreechart;
    }

    public void setResultMap(Map<BankBalanceSql.BankBalanceType, List<Dto>> resultMap) {
        this.resultMap = resultMap;
    }

    private CategoryDataset createDataset(BankBalanceSql.BankBalanceType type) {
        List<Dto> data = resultMap.get(type);
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        for (Dto dto : data) {
            dataSet.addValue(dto.getFieldAsNumber("amount"), dto.getFieldAsString("currencyName"), dto.getFieldAsString("monthName"));
        }

        return dataSet;
    }
}
