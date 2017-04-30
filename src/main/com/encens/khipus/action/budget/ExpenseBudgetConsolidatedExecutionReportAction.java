package com.encens.khipus.action.budget;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;

/**
 * ExpenseBudgetConsolidatedExecutionReportAction
 *
 * @author
 * @version 2.7
 */
@Name("expenseBudgetConsolidatedExecutionReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EXPENSEBUDGETCONSOLIDATEDEXECREPORT','VIEW')}")
public class ExpenseBudgetConsolidatedExecutionReportAction extends GenericReportAction {
    @In
    private BudgetDistributionValidatorAction budgetDistributionValidatorAction;
    @In(create = true)
    private org.jboss.seam.framework.EntityQuery businessUnitListForExpenseBudgetConsolidatedExecutionReportQuery;
    private BusinessUnit businessUnit;
    private Classifier classifier;
    private Gestion gestion;
    private Month movementMonth;

    @Create
    public void init() {
        restrictions = new String[]{"expenseBudget.company=#{currentCompany}",
                "expenseBudget.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.budget.BudgetState','APPROVED')}",
                "businessUnit=#{expenseBudgetConsolidatedExecutionReportAction.businessUnit}",
                "classifier=#{expenseBudgetConsolidatedExecutionReportAction.classifier}",
                "gestion=#{expenseBudgetConsolidatedExecutionReportAction.gestion}"};
        sortProperty = "businessUnit.executorUnitCode, classifier.code";

    }

    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "       businessUnit.executorUnitCode, " +
                "       organization.name, " +
                "       businessUnit.id, " +
                "       classifier.name, " +
                "       classifier.code, " +
                "       classifier.id, " +
                "       expenseBudget.id " +
                " FROM  ExpenseBudget expenseBudget " +
                "       LEFT JOIN expenseBudget.gestion gestion" +
                "       LEFT JOIN expenseBudget.classifier classifier" +
                "       LEFT JOIN expenseBudget.businessUnit businessUnit" +
                "       LEFT JOIN businessUnit.organization organization";
    }

    @SuppressWarnings({"unchecked"})
    public void generateReport() {
        log.debug("generating expenseBudgetConsolidatedExecutionReport......................................");
        if (budgetDistributionValidatorAction.validateHasGlobalConfiguration(getGestion(), businessUnitListForExpenseBudgetConsolidatedExecutionReportQuery.getResultList())) {
            HashMap<String, Object> reportParameters = new HashMap<String, Object>();
            super.generateReport(
                    "expenseBudgetConsolidatedExecutionReport",
                    "/finances/reports/expenseBudgetConsolidatedExecutionReport.jrxml",
                    PageFormat.LEGAL,
                    PageOrientation.LANDSCAPE,
                    messages.get("ExpenseBudget.report.expenseBudgetConsolidatedExecutionReport.title"),
                    reportParameters);
        }
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
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
