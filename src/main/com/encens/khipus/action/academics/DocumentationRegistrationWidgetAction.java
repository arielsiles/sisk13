package com.encens.khipus.action.academics;

import com.encens.khipus.action.cashbox.DebtWidgetGraph;
import com.encens.khipus.action.dashboard.WidgetViewAction;
import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.academics.sql.DocumentationRegistrationWidgetSql;
import com.encens.khipus.model.academics.Carrer;
import com.encens.khipus.model.academics.IngressDocumentType;
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
@Name("documentationRegistrationWidgetAction")
@Scope(ScopeType.PAGE)
public class DocumentationRegistrationWidgetAction extends WidgetViewAction<DebtWidgetGraph> {

    public static final String XML_WIDGET_ID = "14";

    private List<Dto> result = new ArrayList<Dto>();
    private Integer executorUnitId=1;
    private Carrer career;
    private Gestion gestion;
    private Cycle cycle;
    private IngressDocumentType ingressDocumentType;

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
        if(null!=ingressDocumentType){
            result = dashboardQueryService.getData(getDtoConfiguration(), getInstanceBuilder(), sqlQuery);
        }
        else{
            result=new ArrayList<Dto>();
        }
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
        if(null!=executorUnitId){
            ((DocumentationRegistrationWidgetSql) sqlQuery).setExecutorUnitId(executorUnitId);
        }

        if (null != career) {
            ((DocumentationRegistrationWidgetSql) sqlQuery).setCareer(career.getStudyPlan());
        } else {
            ((DocumentationRegistrationWidgetSql) sqlQuery).setCareer(null);
        }

        if (null != gestion) {
            ((DocumentationRegistrationWidgetSql) sqlQuery).setGestion(gestion.getYear());
        } else {
            ((DocumentationRegistrationWidgetSql) sqlQuery).setGestion(null);
        }

        if (null != cycle) {
            ((DocumentationRegistrationWidgetSql) sqlQuery).setPeriod(cycle.getCycleType().getPeriod());
        } else {
            ((DocumentationRegistrationWidgetSql) sqlQuery).setPeriod(null);
        }

        if(null!=ingressDocumentType){
            ((DocumentationRegistrationWidgetSql) sqlQuery).setDocumentType(ingressDocumentType.getIngressDocumentTypeId());
        }

    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return new DocumentationRegistrationWidgetSql();
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

    public IngressDocumentType getIngressDocumentType() {
        return ingressDocumentType;
    }

    public void setIngressDocumentType(IngressDocumentType ingressDocumentType) {
        this.ingressDocumentType = ingressDocumentType;
    }
}
