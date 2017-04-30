package com.encens.khipus.action.budget;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.CostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the expense budget report action
 *
 * @author
 * @version 2.5
 */
@Name("expenseBudgetExecutionReportAction")
@Scope(ScopeType.PAGE)
public class ExpenseBudgetExecutionReportAction extends GenericReportAction {
    @In
    private BudgetDistributionValidatorAction budgetDistributionValidatorAction;
    @In(create = true)
    private org.jboss.seam.framework.EntityQuery businessUnitListForExpenseBudgetExecutionReportQuery;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private Classifier classifier;
    private Gestion gestion;
    private Month movementMonth;

    @Create
    public void init() {
        restrictions = new String[]{"expenseBudget.company=#{currentCompany}",
                "expenseBudget.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.budget.BudgetState','APPROVED')}",
                "businessUnit=#{expenseBudgetExecutionReportAction.businessUnit}",
                "costCenter=#{expenseBudgetExecutionReportAction.costCenter}",
                "classifier=#{expenseBudgetExecutionReportAction.classifier}",
                "gestion = #{expenseBudgetExecutionReportAction.gestion}"};
        sortProperty = "businessUnit.executorUnitCode, costCenter.groupCode, costCenter.code, classifier.code";

    }

    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "       costCenter.code, " +
                "       costCenter.description, " +
                "       businessUnit.executorUnitCode, " +
                "       organization.name, " +
                "       classifier.name, " +
                "       classifier.code, " +
                "       expenseBudget.amount, " +
                "       expenseBudget.id, " +
                "       businessUnit.id, " +
                "       costCenter.groupCode " +
                " FROM  ExpenseBudget expenseBudget " +
                "       LEFT JOIN expenseBudget.gestion gestion" +
                "       LEFT JOIN expenseBudget.classifier classifier" +
                "       LEFT JOIN expenseBudget.costCenter costCenter " +
                "       LEFT JOIN expenseBudget.businessUnit businessUnit " +
                "       LEFT JOIN businessUnit.organization organization";
    }

    public void generateReport() {
        log.debug("generating expenseBudgetExecutionReport......................................");
        if (budgetDistributionValidatorAction.validateHasGlobalConfiguration(getGestion(), businessUnitListForExpenseBudgetExecutionReportQuery.getResultList())) {
            HashMap<String, Object> reportParameters = new HashMap<String, Object>();
            super.generateReport(
                    "expenseBudgetExecutionReport",
                    "/finances/reports/expenseBudgetExecutionReport.jrxml",
                    PageFormat.LEGAL,
                    PageOrientation.LANDSCAPE,
                    messages.get("ExpenseBudget.report.expenseBudgetExecutionReport.title"),
                    reportParameters);
        }
    }

    @Factory(value = "months", scope = ScopeType.STATELESS)
    public Month[] getMonths() {
        return Month.values();
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void assignCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void clearCostCenter() {
        costCenter = null;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMovementMonth() {
        return movementMonth;
    }

    public void setMovementMonth(Month movementMonth) {
        this.movementMonth = movementMonth;
    }
}
