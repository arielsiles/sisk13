package com.encens.khipus.action.employees;

import com.encens.khipus.action.dashboard.GraphicViewAction;
import com.encens.khipus.action.dashboard.LatenessGraphic;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;
import com.encens.khipus.dashboard.component.dto.configuration.field.IdField;
import com.encens.khipus.dashboard.component.dto.configuration.field.SingleField;
import com.encens.khipus.dashboard.component.sql.SqlQuery;
import com.encens.khipus.dashboard.module.employees.LatenessSql;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jfree.chart.plot.PlotOrientation;

/**
 * @author
 * @version 3.2
 */
@Name("latenessViewAction")
@Scope(ScopeType.EVENT)
public class LatenessViewAction extends GraphicViewAction<LatenessGraphic> {

    private Integer businessUnitId;
    private CostCenter costCenter;
    private OrganizationalUnit organizationalUnit;
    private LatenessSql sql;

    @Create
    public void initialize() {
        setGraphic(new LatenessGraphic());
        graphic.setCategoryAxisLabel(MessageUtils.getMessage("Reports.lateness.categoryAxisLabel"));
        graphic.setValueAxisLabel(MessageUtils.getMessage("Reports.lateness.valueAxisLabel"));
        graphic.setOrientation(PlotOrientation.HORIZONTAL);
        sql = new LatenessSql();
        sql.setBusinessUnitId(businessUnitId);
        sql.setCostCenter(costCenter);
        sql.setOrganizationalUnit(organizationalUnit);
    }

    public byte[] createChart() {
        return getGraphic().createChart();
    }

    @Override
    protected DtoConfiguration getDtoConfiguration() {
        return DtoConfiguration.getInstance(IdField.getInstance("id", 0))
                .addField(SingleField.getInstance("organizationalUnit", 1))
                .addField(SingleField.getInstance("costCenter", 2));
    }

    public void refresh(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
        sql.setBusinessUnitId(businessUnitId);
        sql.setCostCenter(costCenter);
        sql.setOrganizationalUnit(organizationalUnit);
        getGraphic();
    }

    @Override
    protected SqlQuery getSqlQueryInstance() {
        return sql;
    }

    public void disableBusinessUnit() {
        businessUnitId = null;
    }

    public void enableBusinessUnit(Integer businessUnitId) {
        this.businessUnitId = businessUnitId;
        sql.setBusinessUnitId(businessUnitId);
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        costCenter = null;
        //noinspection NullableProblems
        sql.setCostCenter(null);
    }

    public void assignCostCenter(CostCenter costCenter) {
        setCostCenter(costCenter);
        sql.setCostCenter(costCenter);
    }

    public void clearOrganizationalUnit() {
        organizationalUnit = null;
        //noinspection NullableProblems
        sql.setOrganizationalUnit(null);
    }

    public void assignOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        setOrganizationalUnit(organizationalUnit);
        sql.setOrganizationalUnit(organizationalUnit);
    }
}
