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
 * This class implements the entry budget report action
 *
 * @author
 * @version 2.0
 */
@Name("entryBudgetReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ENTRYBUDGETREPORT','VIEW')}")
public class EntryBudgetReportAction extends GenericReportAction {
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private Classifier classifier;
    private Gestion gestion;

    @Create
    public void init() {
        restrictions = new String[]{"entryBudget.company=#{currentCompany}",
                "entryBudget.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.budget.BudgetState','APPROVED')}",
                "businessUnit=#{entryBudgetReportAction.businessUnit}",
                "costCenter=#{entryBudgetReportAction.costCenter}",
                "classifier=#{entryBudgetReportAction.classifier}",
                "gestion = #{entryBudgetReportAction.gestion}"};
        sortProperty = "organization.id, " +
                "costCenter.id, " +
                "gestion.year," +
                "entryBudget.creationDate";
    }

    protected String getEjbql() {
        return "SELECT  businessUnit.executorUnitCode, " +
                "       organization.name, " +
                "       organization.id, " +
                "       costCenter.id, " +
                "       costCenter.code, " +
                "       costCenter.description, " +
                "       entryBudget.id, " +
                "       entryBudget.amount, " +
                "       classifier.id, " +
                "       classifier.name, " +
                "       classifier.code, " +
                "       entryBudget.creationDate, " +
                "       gestion.id, " +
                "       gestion.year " +
                " FROM  EntryBudget entryBudget" +
                "       LEFT JOIN entryBudget.gestion gestion" +
                "       LEFT JOIN entryBudget.costCenter costCenter" +
                "       LEFT JOIN entryBudget.classifier classifier" +
                "       LEFT JOIN entryBudget.businessUnit businessUnit" +
                "       LEFT JOIN businessUnit.organization organization";
    }

    public void generateReport() {
        log.debug("generating entryBudgetBudgetReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("reportGestion", getGestion().getYear());
        super.generateReport(
                "entryBudgetReport",
                "/finances/reports/entryBudgetReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("EntryBudget.report.entryBudgetReport"),
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
