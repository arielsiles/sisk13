package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate control report
 *
 * @author
 * @version $Id: ControlReportReportAction.java  07-abr-2010 16:50:57$
 */
@Name("controlReportReportAction")
@Scope(ScopeType.PAGE)
public class ControlReportReportAction extends GenericReportAction {

    @In
    GenericService genericService;
    private GeneratedPayroll generatedPayroll;

    public void generateReport(GeneratedPayroll generatedPayroll) {
        log.debug("Generate ControlReportReportAction......");
        //set filter properties
        setGeneratedPayroll(generatedPayroll);

        Map params = new HashMap();
        params.putAll(readReportHeaderParamsInfo(generatedPayroll));

        setReportFormat(ReportFormat.XLS);
        super.generateReport("controlReportReport", "/employees/reports/controlReportReport.jrxml", MessageUtils.getMessage("Reports.controlReport.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT " +
                "controlReport.horaryBandContract.jobContract.contract.employee.idNumber," +
                "controlReport.horaryBandContract.jobContract.contract.employee.lastName," +
                "controlReport.horaryBandContract.jobContract.contract.employee.maidenName," +
                "controlReport.horaryBandContract.jobContract.contract.employee.firstName," +
                "controlReport.date," +
                "controlReport.horaryBandContract.horaryBand.initHour," +
                "controlReport.horaryBandContract.horaryBand.endHour," +
                "controlReport.initMark," +
                "controlReport.endMark," +
                "controlReport.marks," +
                "controlReport.discountAmount," +
                "controlReport.performanceMinuteAmount," +
                "controlReport.bandAbsenceDiscount," +
                "controlReport.perBandSalary," +
                "controlReport.performanceMinutes," +
                "controlReport.horaryBandContract.horaryBand.duration," +
                "controlReport.minutesDiscount," +
                "controlReport.bandAbsence" +
                " FROM ControlReport controlReport";
    }

    @Create
    public void init() {
        restrictions = new String[]{"controlReport.company=#{currentCompany}",
                "controlReport.generatedPayroll=#{controlReportReportAction.generatedPayroll}"};

        sortProperty = "controlReport.horaryBandContract.jobContract.contract.employee.idNumber";
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    /**
     * Read report header fields an define as params
     *
     * @param generatedPayroll generated payroll
     * @return Map
     */
    private Map readReportHeaderParamsInfo(GeneratedPayroll generatedPayroll) {
        Map headerParamMap = new HashMap();
        try {
            generatedPayroll = genericService.findById(GeneratedPayroll.class, generatedPayroll.getId());
        } catch (EntryNotFoundException e) {
            log.debug("Not found generated payroll.. ", e);
        }
        GestionPayroll gestionPayroll = generatedPayroll.getGestionPayroll();

        headerParamMap.put("sedeParam", gestionPayroll.getBusinessUnit().getOrganization().getName());
        headerParamMap.put("jobCategoryParam", gestionPayroll.getJobCategory().getName());
        headerParamMap.put("periodParam", MessageUtils.getMessage(gestionPayroll.getMonth().getResourceKey()));

        return headerParamMap;
    }
}
