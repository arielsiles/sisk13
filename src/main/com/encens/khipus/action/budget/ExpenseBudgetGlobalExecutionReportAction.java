package com.encens.khipus.action.budget;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;

/**
 * ExpenseBudgetGlobalExecutionReportAction
 *
 * @author
 * @version 2.7
 */
@Name("expenseBudgetGlobalExecutionReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EXPENSEBUDGETGLOBALEXECUTIONREPORT','VIEW')}")
public class ExpenseBudgetGlobalExecutionReportAction extends GenericReportAction {
    private Classifier classifier;
    private Gestion gestion;
    private Month movementMonth;

    @Create
    public void init() {
        restrictions = new String[]{"expenseBudget.company=#{currentCompany}",
                "expenseBudget.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.budget.BudgetState','APPROVED')}",
                "classifier=#{expenseBudgetGlobalExecutionReportAction.classifier}",
                "gestion=#{expenseBudgetGlobalExecutionReportAction.gestion}"};
        sortProperty = "classifier.code";

    }

    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "       classifier.name, " +
                "       classifier.code, " +
                "       classifier.id, " +
                "       expenseBudget.id " +
                " FROM  ExpenseBudget expenseBudget " +
                "       LEFT JOIN expenseBudget.gestion gestion" +
                "       LEFT JOIN expenseBudget.classifier classifier";
    }

    public void generateReport() {
        log.debug("generating expenseBudgetGlobalExecutionReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        super.generateReport(
                "expenseBudgetGlobalExecutionReport",
                "/finances/reports/expenseBudgetGlobalExecutionReport.jrxml",
                PageFormat.LEGAL,
                PageOrientation.LANDSCAPE,
                messages.get("ExpenseBudget.report.expenseBudgetGlobalExecutionReport.title"),
                reportParameters);
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
