package com.encens.khipus.action.employees;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.DischargeDocumentState;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;

/**
 * @author
 */
@Name("dischargeDocumentsReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DISCHARGEDOCUMENTSREPORT','VIEW')}")
public class DischargeDocumentsReportAction extends GenericReportAction {
    private Employee employee;
    private DischargeDocumentState approvedState = DischargeDocumentState.APPROVED;
    private GestionPayroll gestionPayroll;
    private Date startDate;
    private Date endDate;


    @Create
    public void init() {
        restrictions = new String[]{
                "employee=#{dischargeDocumentsReportAction.employee}",
                "dischargeDocument.state=#{dischargeDocumentsReportAction.approvedState}",
                "gestionPayroll=#{dischargeDocumentsReportAction.gestionPayroll}",
                "dischargeDocument.date>=#{dischargeDocumentsReportAction.startDate}",
                "dischargeDocument.date<=#{dischargeDocumentsReportAction.endDate}"
        };
        sortProperty = "dischargeDocument.id";
    }

    @Override
    protected String getEjbql() {
        return "SELECT dischargeDocument.id, " +
                "      gestionPayroll, " +
                "      dischargeDocument.date, " +
                "      dischargeDocument.nit, " +
                "      dischargeDocument.name, " +
                "      dischargeDocument.number, " +
                "      dischargeDocument.authorizationNumber, " +
                "      dischargeDocument.controlCode, " +
                "      dischargeDocument.amount, " +
                "      dischargeDocument.exempt, " +
                "      dischargeDocument.ice, " +
                "      dischargeDocument.netAmount, " +
                "      dischargeDocument.iva " +
                "FROM  DischargeDocument dischargeDocument " +
                "      LEFT JOIN dischargeDocument.gestionPayroll gestionPayroll " +
                "      LEFT JOIN dischargeDocument.jobContract jobContract " +
                "      LEFT JOIN jobContract.contract contract " +
                "      LEFT JOIN contract.employee employee ";

    }

    public void generateReport() {
        log.debug("Generating dischargeDocuments report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        super.generateReport(
                "dischargeDocumentsReport",
                "/employees/reports/dischargeDocumentsReport.jrxml",
                PageFormat.LEGAL,
                PageOrientation.LANDSCAPE,
                messages.get("DischargeDocuments.report.title"),
                reportParameters);

    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        this.employee = null;
    }

    public DischargeDocumentState getApprovedState() {
        return approvedState;
    }

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    public void clearGestionPayRoll() {
        this.gestionPayroll = null;
    }

    public void assignGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}