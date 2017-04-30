package com.encens.khipus.action.cashbox;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.graphics.Graphic;
import com.encens.khipus.util.MessageUtils;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author
 * @version 2.15
 */
public class DebtGraph extends Graphic {
    private String xLabel;

    public DebtGraph() {
    }

    public byte[] createChart() {
        CategoryAxis categoryAxis = new CategoryAxis(xLabel);

        CombinedDomainCategoryPlot combinedDomainCategoryPlot = new CombinedDomainCategoryPlot(categoryAxis);
        combinedDomainCategoryPlot.add(getQuantitiesPlot());
        combinedDomainCategoryPlot.add(getAmountsPlot());

        JFreeChart jfreechart = new JFreeChart(combinedDomainCategoryPlot);
        ChartUtilities.applyCurrentTheme(jfreechart);

        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, jfreechart, getWidth(), 300, info);

            toolTipMap = ImageMapUtilities.getImageMap("debtsChartId", info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            //log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }

    private CategoryPlot getQuantitiesPlot() {
        GroupedStackedBarRenderer renderer = buildRenderer(buildGroupMap("G1", "G2", "G3"),
                "{0}: {2}",
                new DecimalFormat(MessageUtils.getMessage("patterns.integerNumber")));

        NumberAxis numberAxis = new NumberAxis(MessageUtils.getMessage("Debt.yLabel.quantity"));

        CategoryPlot plot = new CategoryPlot(createMainQuantityDataset(), null, numberAxis, renderer);
        plot.setDomainAxis(buildSubCategoryAxis());
        plot.setFixedLegendItems(new LegendItemCollection());

        return plot;
    }

    private CategoryPlot getAmountsPlot() {
        String usdSymbol = MessageUtils.getMessage("Dashboard.symbol.usd");

        GroupedStackedBarRenderer renderer = buildRenderer(buildGroupMap("G1", "G2", "G3"),
                "{0} [" + usdSymbol + "]: {2}",
                new DecimalFormat(MessageUtils.getMessage("patterns.decimalNumber")));

        NumberAxis numberAxis = new NumberAxis(MessageUtils.getMessage("Debt.yLabel.amount"));

        CategoryPlot plot = new CategoryPlot(createAmountsDataSet(), null, numberAxis, renderer);
        plot.setDomainAxis(buildSubCategoryAxis());
        plot.setFixedLegendItems(new LegendItemCollection());

        return plot;
    }

    private GroupedStackedBarRenderer buildRenderer(KeyToGroupMap keyToGroupMap,
                                                    String toolTipPattern,
                                                    NumberFormat numberFormat) {
        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();

        renderer.setSeriesToGroupMap(keyToGroupMap);
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(toolTipPattern, numberFormat));

        return renderer;
    }

    private KeyToGroupMap buildGroupMap(String firstGroup, String secondGroup, String thirdGroup) {
        KeyToGroupMap map = new KeyToGroupMap(firstGroup);

        map.mapKeyToGroup(MessageUtils.getMessage("Debt.createdDebt.registeredStudent"), firstGroup);
        map.mapKeyToGroup(MessageUtils.getMessage("Debt.createdDebt.studentship"), firstGroup);
        map.mapKeyToGroup(MessageUtils.getMessage("Debt.createdDebt.defectorStudent"), firstGroup);

        map.mapKeyToGroup(MessageUtils.getMessage("Debt.payOver.registeredStudent"), secondGroup);
        map.mapKeyToGroup(MessageUtils.getMessage("Debt.payOver.studentship"), secondGroup);
        map.mapKeyToGroup(MessageUtils.getMessage("Debt.payOver.defectorStudent"), secondGroup);

        map.mapKeyToGroup(MessageUtils.getMessage("Debt.debt.registeredStudent"), thirdGroup);
        map.mapKeyToGroup(MessageUtils.getMessage("Debt.debt.studentship"), thirdGroup);
        map.mapKeyToGroup(MessageUtils.getMessage("Debt.debt.defectorStudent"), thirdGroup);

        return map;
    }


    private SubCategoryAxis buildSubCategoryAxis() {
        SubCategoryAxis domainAxis = new SubCategoryAxis(xLabel);
        domainAxis.setCategoryMargin(0.05);
        domainAxis.addSubCategory(MessageUtils.getMessage("Debt.createdDebt"));
        domainAxis.addSubCategory(MessageUtils.getMessage("Debt.payOver"));
        domainAxis.addSubCategory(MessageUtils.getMessage("Debt.debt"));

        return domainAxis;
    }

    private CategoryDataset createMainQuantityDataset() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        for (Dto debt : data) {
            dataSet.addValue(debt.getFieldAsNumber("registeredStudentCreatedDebt"), MessageUtils.getMessage("Debt.createdDebt.registeredStudent"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("studentshipCreatedDebt"), MessageUtils.getMessage("Debt.createdDebt.studentship"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("defectorStudentCreateDebt"), MessageUtils.getMessage("Debt.createdDebt.defectorStudent"), debt.getFieldAsString("domainName"));

            dataSet.addValue(debt.getFieldAsNumber("registeredStudentPayOver"), MessageUtils.getMessage("Debt.payOver.registeredStudent"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("studentshipPayOver"), MessageUtils.getMessage("Debt.payOver.studentship"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("defectorStudentPayOver"), MessageUtils.getMessage("Debt.payOver.defectorStudent"), debt.getFieldAsString("domainName"));

            dataSet.addValue(debt.getFieldAsNumber("registeredStudentDebt"), MessageUtils.getMessage("Debt.debt.registeredStudent"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("studentshipDebt"), MessageUtils.getMessage("Debt.debt.studentship"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("defectorStudentDebt"), MessageUtils.getMessage("Debt.debt.defectorStudent"), debt.getFieldAsString("domainName"));
        }

        return dataSet;
    }

    private CategoryDataset createAmountsDataSet() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (Dto debt : data) {
            dataSet.addValue(debt.getFieldAsNumber("registeredStudentCreatedDebtAmount"), MessageUtils.getMessage("Debt.createdDebt.registeredStudent"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("studentshipCreatedDebtAmount"), MessageUtils.getMessage("Debt.createdDebt.studentship"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("defectorStudentCreateDebtAmount"), MessageUtils.getMessage("Debt.createdDebt.defectorStudent"), debt.getFieldAsString("domainName"));

            dataSet.addValue(debt.getFieldAsNumber("registeredStudentPayOverAmount"), MessageUtils.getMessage("Debt.payOver.registeredStudent"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("studentshipPayOverAmount"), MessageUtils.getMessage("Debt.payOver.studentship"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("defectorStudentPayOverAmount"), MessageUtils.getMessage("Debt.payOver.defectorStudent"), debt.getFieldAsString("domainName"));

            dataSet.addValue(debt.getFieldAsNumber("registeredStudentDebtAmount"), MessageUtils.getMessage("Debt.debt.registeredStudent"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("studentshipDebtAmount"), MessageUtils.getMessage("Debt.debt.studentship"), debt.getFieldAsString("domainName"));
            dataSet.addValue(debt.getFieldAsNumber("defectorStudentDebtAmount"), MessageUtils.getMessage("Debt.debt.defectorStudent"), debt.getFieldAsString("domainName"));
        }

        return dataSet;
    }

    public void setXLabel(String xLabel) {
        this.xLabel = xLabel;
    }
}
