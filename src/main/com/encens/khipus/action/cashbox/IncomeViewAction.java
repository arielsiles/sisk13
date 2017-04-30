package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.GraphicViewAction;
import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.cashbox.sql.IncomeSql;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 2.18
 */
@Name("incomeViewAction")
@Scope(ScopeType.EVENT)
public class IncomeViewAction extends GraphicViewAction<IncomeGraph> {
    private Integer executorUnitId;

    private Map<IncomeSql.SqlToExecute, List<Dto>> resultMap = new HashMap<IncomeSql.SqlToExecute, List<Dto>>();

    @Create
    public void initialize() {
        setGraphic(new IncomeGraph());
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    @Override
    protected void executeService(SqlQuery sqlQuery) {
        for (IncomeSql.SqlToExecute sqlToExecute : IncomeSql.getSqlToExecuteConstants()) {
            ((IncomeSql) sqlQuery).setSqlToExecute(sqlToExecute);

            List<Dto> result = dashboardQueryService.getData(getDtoConfiguration(sqlToExecute), getInstanceBuilder(), sqlQuery);
            resultMap.put(sqlToExecute, result);
        }
    }

    private DtoConfiguration getDtoConfiguration(IncomeSql.SqlToExecute sqlToExecute) {
        if (IncomeSql.SqlToExecute.INVOICE.equals(sqlToExecute)) {
            return DtoConfiguration.getInstance(IdField.getInstance("monthName", 2))
                    .addField(SingleField.getInstance("bsAmount", 3))
                    .addField(SingleField.getInstance("usdAmount", 4))
                    .addField(SingleField.getInstance("graphicValue", 5))
                    .addField(SingleField.getInstance("exchangeRate", 6));
        }
        if (IncomeSql.SqlToExecute.BUDGET.equals(sqlToExecute)) {
            return DtoConfiguration.getInstance(IdField.getInstance("monthName", 4))
                    .addField(SingleField.getInstance("yearBudget", 0))
                    .addField(SingleField.getInstance("accumulatedExecution", 1))
                    .addField(SingleField.getInstance("monthlyExecution", 2))
                    .addField(SingleField.getInstance("graphicValue", 3));
        }

        if (IncomeSql.SqlToExecute.CASHBOX.equals(sqlToExecute)) {
            return DtoConfiguration.getInstance(IdField.getInstance("monthNumber", 0))
                    .addField(SingleField.getInstance("year", 1))
                    .addField(SingleField.getInstance("monthName", 2))
                    .addField(SingleField.getInstance("bsAmount", 3))
                    .addField(SingleField.getInstance("usdAmount", 4))
                    .addField(SingleField.getInstance("graphicValue", 5))
                    .addField(SingleField.getInstance("exchangeRate", 6));
        }
        return null;
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new IncomeSql();
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((IncomeSql) sqlQuery).setExecutorUnitCode(executorUnitId);
    }

    @Override
    protected void setGraphicParameters(IncomeGraph graphic) {
        graphic.setResultMap(resultMap);
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }

    public void disableExecutorUnit() {
        this.executorUnitId = null;
    }

    public String getDateRange() {
        String startDate = DateUtils.format(((IncomeSql) getSqlQueryInstance()).getStartDate(), MessageUtils.getMessage("patterns.date"));
        String endDate = DateUtils.format(((IncomeSql) getSqlQueryInstance()).getEndDate(), MessageUtils.getMessage("patterns.date"));

        return MessageUtils.getMessage("Common.range", startDate, endDate).trim();
    }
}
