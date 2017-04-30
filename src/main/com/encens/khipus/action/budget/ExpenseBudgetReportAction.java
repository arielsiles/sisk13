package com.encens.khipus.action.budget;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.finances.CostCenter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the expense budget report action
 *
 * @author
 * @version 2.0
 */
@Name("expenseBudgetReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EXPENSEBUDGETREPORT','VIEW')}")
public class ExpenseBudgetReportAction extends GenericReportAction {
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private Classifier classifier;
    private Gestion gestion;

    @Create
    public void init() {
        restrictions = new String[]{"expenseBudget.company=#{currentCompany}",
                "expenseBudget.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.budget.BudgetState','APPROVED')}",
                "businessUnit=#{expenseBudgetReportAction.businessUnit}",
                "costCenter=#{expenseBudgetReportAction.costCenter}",
                "classifier=#{expenseBudgetReportAction.classifier}",
                "gestion = #{expenseBudgetReportAction.gestion}"};

        sortProperty = "organization.id, " +
                "costCenter.id, " +
                "budgetProgram.id, " +
                "budgetActivity.id, " +
                "gestion.year," +
                "expenseBudget.creationDate";
    }

    protected String getEjbql() {
        return "SELECT  businessUnit.executorUnitCode, " +
                "       organization.name, " +
                "       organization.id, " +
                "       costCenter.id, " +
                "       costCenter.code, " +
                "       costCenter.description, " +
                "       budgetProgram.id, " +
                "       budgetProgram.code, " +
                "       budgetProgram.name, " +
                "       budgetActivity.id, " +
                "       budgetActivity.code, " +
                "       budgetActivity.name, " +
                "       expenseBudget.id, " +
                "       expenseBudget.amount, " +
                "       classifier.id, " +
                "       classifier.name, " +
                "       classifier.code, " +
                "       expenseBudget.creationDate, " +
                "       gestion.id, " +
                "       gestion.year " +
                " FROM  ExpenseBudget expenseBudget" +
                "       LEFT JOIN expenseBudget.gestion gestion" +
                "       LEFT JOIN expenseBudget.classifier classifier" +
                "       LEFT JOIN expenseBudget.costCenter costCenter " +
                "       LEFT JOIN expenseBudget.budgetActivity budgetActivity " +
                "       LEFT JOIN budgetActivity.budgetProgram budgetProgram" +
                "       LEFT JOIN expenseBudget.businessUnit businessUnit " +
                "       LEFT JOIN businessUnit.organization organization";
    }

    public void generateReport() {
        log.debug("generating expenseBudgetReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("reportGestion", getGestion().getYear());
        super.generateReport(
                "expenseBudgetReport",
                "/finances/reports/expenseBudgetReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("ExpenseBudget.report.expenseBudgetReport"),
                reportParameters);
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
}
