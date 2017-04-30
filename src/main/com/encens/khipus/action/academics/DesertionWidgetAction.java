package com.encens.khipus.action.academics;

import com.encens.khipus.action.cashbox.DebtWidgetGraph;
import com.encens.khipus.action.dashboard.WidgetViewAction;
import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.academics.sql.DesertionWidgetSql;
import com.encens.khipus.model.academics.Carrer;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.Gestion;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Name("desertionWidgetAction")
@Scope(ScopeType.PAGE)
public class DesertionWidgetAction extends WidgetViewAction<DebtWidgetGraph> {

    public static final String XML_WIDGET_ID = "12";

    private List<Dto> result = new ArrayList<Dto>();
    private Integer executorUnitId;
    private Carrer career;
    private Gestion gestion;
    private Cycle cycle;

    @Create
    public void initialize() {
        setGraphic(new DebtWidgetGraph());
        initializeWidget();
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    @Override
    protected void executeService(SqlQuery sqlQuery) {
        result = dashboardQueryService.getData(getDtoConfiguration(), getInstanceBuilder(), sqlQuery);
    }

    @Override
    protected DtoConfiguration getDtoConfiguration() {
        return DtoConfiguration.getInstance(IdField.getInstance("codeCounter", 0))
                .addField(SingleField.getInstance("totalCounter", 1))
                .addField(SingleField.getInstance("desertionCounter", 2))
                .addField(SingleField.getInstance("percentage", 3));
    }

    @Override
    public List<Dto> getResultList() {
        return result;
    }

    @Override
    protected String getXmlWidgetId() {
        return XML_WIDGET_ID;
    }

    @Override
    protected void setFilters(SqlQuery sqlQuery) {
        ((DesertionWidgetSql) sqlQuery).setExecutorUnitId(executorUnitId);

        if (null != career) {
            ((DesertionWidgetSql) sqlQuery).setCareer(career.getStudyPlan());
        } else {
            ((DesertionWidgetSql) sqlQuery).setCareer(null);
        }

        if (null != gestion) {
            ((DesertionWidgetSql) sqlQuery).setGestion(gestion.getYear());
        } else {
            ((DesertionWidgetSql) sqlQuery).setGestion(null);
        }

        if (null != cycle) {
            ((DesertionWidgetSql) sqlQuery).setPeriod(cycle.getCycleType().getPeriod());
        } else {
            ((DesertionWidgetSql) sqlQuery).setPeriod(null);
        }
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new DesertionWidgetSql();
    }

    @Override
    protected void setGraphicParameters(DebtWidgetGraph graphic) {
        graphic.setWidget(this.widget);
    }

    public void disableExecutorUnit() {
        this.executorUnitId = null;
        this.career = null;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
        this.career = null;
    }

    public Integer getExecutorUnitId() {
        return executorUnitId;
    }

    public Carrer getCareer() {
        return career;
    }

    public void setCareer(Carrer carrer) {
        this.career = carrer;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }
}
