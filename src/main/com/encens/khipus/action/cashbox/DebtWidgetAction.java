package com.encens.khipus.action.cashbox;

import com.encens.khipus.action.dashboard.WidgetViewAction;
import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.cashbox.sql.DebtWidgetSql;
import com.encens.khipus.model.academics.Carrer;
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
@Name("debtWidgetAction")
@Scope(ScopeType.PAGE)
public class DebtWidgetAction extends WidgetViewAction<DebtWidgetGraph> {
    private List<Dto> result = new ArrayList<Dto>();

    public static final String XML_WIDGET_ID = "10";

    private Integer executorUnitId;

    private Carrer carrer = null;

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
                .addField(SingleField.getInstance("registeredStudentsCounter", 1))
                .addField(SingleField.getInstance("debtStudentsCounter", 2))
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
        ((DebtWidgetSql) sqlQuery).setExecutorUnitId(executorUnitId);
        if (null != carrer) {
            ((DebtWidgetSql) sqlQuery).setCarreer(carrer.getStudyPlan());
        } else {
            ((DebtWidgetSql) sqlQuery).setCarreer(null);
        }
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new DebtWidgetSql();
    }

    @Override
    protected void setGraphicParameters(DebtWidgetGraph graphic) {
        graphic.setWidget(this.widget);
    }

    public void disableExecutorUnit() {
        this.executorUnitId = null;
        this.carrer = null;
    }

    public void enableExecutorUnit(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
        this.carrer = null;
    }

    public Carrer getCarrer() {
        return carrer;
    }

    public void setCarrer(Carrer carrer) {
        this.carrer = carrer;
    }

    public Integer getExecutorUnitId() {
        return executorUnitId;
    }
}
