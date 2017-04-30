package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.DashboardObjectAction;
import com.encens.khipus.dashboard.component.factory.ComponentFactory;
import com.encens.khipus.dashboard.component.totalizer.SumTotalizer;
import com.encens.khipus.dashboard.module.cashbox.Income;
import com.encens.khipus.dashboard.module.cashbox.IncomeByCashboxSqlQuery;
import com.encens.khipus.dashboard.module.cashbox.IncomeInstanceFactory;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.imagemap.ImageMapUtilities;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.6
 */
@Name("incomeByCashboxComponentAction")
@Scope(ScopeType.EVENT)
public class IncomeByCashboxAction extends DashboardObjectAction<Income> {
    private String graphicInformation = "";

    private Integer executorUnitCode = null;

    @In(create = true)
    private EntityQuery executorUnitQuery;

    @In
    protected Map<String, String> messages;

    public void disableExecutorUnit() {
        executorUnitCode = null;
    }

    public void enableExecutorUnit(Integer code) {
        executorUnitCode = code;
    }

    public Map<String, Number> getTotals() {
        return ((SumTotalizer<Income>) factory.getTotalizer()).getTotals();
    }

    public String getDateRange() {
        String startDate = DateUtils.format(getSqlQuery().getStartDate(), MessageUtils.getMessage("patterns.date"));
        String endDate = DateUtils.format(getSqlQuery().getEndDate(), MessageUtils.getMessage("patterns.date"));

        return MessageUtils.getMessage("Common.range", startDate, endDate).trim();
    }

    @Override
    public List<Income> getResultList() {
        search();
        return super.getResultList();
    }

    @Factory(value = "incomeByCashboxChart")
    public byte[] createChart() {
        JFreeChart chart = ChartFactory.createBarChart(null,
                messages.get("Common.months"),
                messages.get("Common.incomes"),
                createDataSet(),
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        chart.setBackgroundPaint(Color.white);
        CategoryItemRenderer renderer = ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{0}: {2}",
                new DecimalFormat(MessageUtils.getMessage("patterns.decimalNumber"))));

        OutputStream out = new ByteArrayOutputStream();

        try {
            ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

            ChartUtilities.writeChartAsPNG(out, chart, getGraphicWidth(), GRAPHIC_HEIGHT, info);

            graphicInformation = ImageMapUtilities.getImageMap("incomeByCashboxChartId", info);

            return ((ByteArrayOutputStream) out).toByteArray();
        } catch (IOException e) {
            log.debug("Cannot render the graphic because ", e);
        }

        return new byte[]{};
    }

    @Override
    protected void initializeFactory() {
        factory = new ComponentFactory<Income, SumTotalizer<Income>>(
                new IncomeByCashboxSqlQuery(),
                new IncomeInstanceFactory(),
                new SumTotalizer<Income>()
        );
    }

    @Override
    protected void setFilters() {
        getSqlQuery().setExecutorUnitCode(executorUnitCode);
    }

    private IncomeByCashboxSqlQuery getSqlQuery() {
        return (IncomeByCashboxSqlQuery) factory.getSqlQuery();
    }

    @SuppressWarnings(value = "unchecked")
    private CategoryDataset createDataSet() {
        List<ExecutorUnit> units = executorUnitQuery.getResultList();

        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        disableExecutorUnit();
        for (Income income : getResultList()) {
            dataSet.addValue(income.getTotalAmount(), messages.get("IncomeByCashbox.tab.all"), income.getMonthName());
        }

        for (ExecutorUnit unit : units) {
            enableExecutorUnit(unit.getId());
            for (Income income : getResultList()) {
                dataSet.addValue(income.getTotalAmount(), unit.getDescription(), income.getMonthName());
            }
        }

        return dataSet;
    }

    public String getGraphicInformation() {
        return graphicInformation;
    }

    public void setGraphicInformation(String graphicInformation) {
        this.graphicInformation = graphicInformation;
    }
}
