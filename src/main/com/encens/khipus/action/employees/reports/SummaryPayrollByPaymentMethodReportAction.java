package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.GestionPayrollService;
import com.encens.khipus.service.employees.PayrollSummaryReportService;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.*;

/**
 * Encens S.R.L.
 * Action to generate payroll summary report by payment method and currency
 *
 * @author
 * @version $Id: SummaryPayrollByPaymentMethodReportAction.java  22-ene-2010 11:38:12$
 */
@Name("summaryPayrollByPaymentMethodReportAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('SUMMARYPAYROLLBYPAYMENTMETHODREPORT','VIEW')}")
public class SummaryPayrollByPaymentMethodReportAction extends GenericReportAction {
    @In
    GestionPayrollService gestionPayrollService;

    private String summaryReportTitle;
    private String gestionTitle;
    private Sector sector;
    private Sector selectedSector = new Sector();
    private List<GestionPayroll> gestionPayrollList;
    private Gestion gestion;
    private Month month;

    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;

    public void generateReport() {
//        add seam properties as filter, summary only to OFFICIAL payroll
        setGeneratedPayrollType(GeneratedPayrollType.OFFICIAL);

        Map params = new HashMap();
        params.put("TITLE_PARAM", summaryReportTitle);
        params.put("GESTION_PARAM", gestionTitle);
        params.put("SECTORTITLE_PARAM", (sector != null) ? sector.getName() : "");

        //add summary by sede sub report
        addSummaryBySedeSubReport(params);
        //add summary by currency sub report
        addSummaryByCurrencySubReport(params);

        super.generateReport("summaryPayrollReport", "/employees/reports/summaryPayrollByPaymentMethodReport.jrxml",
                PageFormat.LETTER, PageOrientation.PORTRAIT, summaryReportTitle, params);
    }

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    /**
     * Add the sub report in main report
     * summary payment method by sede
     *
     * @param mainReportParams main report params
     */
    private void addSummaryBySedeSubReport(Map mainReportParams) {
        log.debug("Generating addSummaryBySedeSubReport.............................");
        String subReportKey = "SUMMARYBYSEDESUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SUMMARYSERVICE_PARAM", (PayrollSummaryReportService) Component.getInstance("payrollSummaryReportService"));

        String gestionPayrollConditions = composeWhereGestionPayrollConditions("generatedPayroll.gestionPayroll.id");

        String ejbql = "SELECT " +
                "generatedPayroll.id," +
                "generatedPayroll.gestionPayroll.businessUnit.publicity," +
                "generatedPayroll.name," +
                "generatedPayroll.gestionPayroll.jobCategory.sector.id," +
                "generatedPayroll.gestionPayroll.jobCategory.id," +
                "generatedPayroll.gestionPayroll.exchangeRate.rate" +
                " FROM GeneratedPayroll generatedPayroll" +
                (gestionPayrollConditions != null ? " WHERE " + gestionPayrollConditions : "");

        String[] restrictions = new String[]{"generatedPayroll.generatedPayrollType=#{summaryPayrollByPaymentMethodReportAction.generatedPayrollType}"};
        String pollByCareerOrder = "generatedPayroll.id";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/employees/reports/payrollSummaryBySedeSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), pollByCareerOrder),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Add the sub report in main report
     * summary by currency
     *
     * @param mainReportParams main report params
     */
    private void addSummaryByCurrencySubReport(Map mainReportParams) {
        log.debug("Generating addSummaryByCurrencySubReport.............................");
        String subReportKey = "SUMMARYBYCURRENCYSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SUMMARYSERVICE_PARAM", (PayrollSummaryReportService) Component.getInstance("payrollSummaryReportService"));

        String gestionPayrollConditions = composeWhereGestionPayrollConditions("generatedPayroll.gestionPayroll.id");
        String ejbql = "SELECT " +
                "generatedPayroll.id," +
                "generatedPayroll.gestionPayroll.businessUnit.publicity," +
                "generatedPayroll.gestionPayroll.jobCategory.sector.id," +
                "generatedPayroll.gestionPayroll.jobCategory.id," +
                "generatedPayroll.gestionPayroll.exchangeRate.rate" +
                " FROM GeneratedPayroll generatedPayroll" +
                (gestionPayrollConditions != null ? " WHERE " + gestionPayrollConditions : "");

        String[] restrictions = new String[]{"generatedPayroll.generatedPayrollType=#{summaryPayrollByPaymentMethodReportAction.generatedPayrollType}"};
        String pollByCareerOrder = "generatedPayroll.id";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/employees/reports/payrollSummaryByCurrencySubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), pollByCareerOrder),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Compose where conditions from gestion payroll list
     *
     * @param gestionPayrollIdProperty property to condition
     * @return String
     */
    private String composeWhereGestionPayrollConditions(String gestionPayrollIdProperty) {
        String conditions = null;
        if (gestionPayrollList != null) {
            for (GestionPayroll gestionPayroll : gestionPayrollList) {
                if (conditions == null) {
                    conditions = gestionPayrollIdProperty + "=" + gestionPayroll.getId();
                } else {
                    conditions = conditions + " OR " + gestionPayrollIdProperty + "=" + gestionPayroll.getId();
                }
            }
            if (conditions != null) {
                conditions = "(" + conditions + ")";
            }
        }
        return conditions;
    }

    public GeneratedPayrollType getGeneratedPayrollType() {
        return generatedPayrollType;
    }

    public void setGeneratedPayrollType(GeneratedPayrollType generatedPayrollType) {
        this.generatedPayrollType = generatedPayrollType;
    }

    public String getSummaryReportTitle() {
        return summaryReportTitle;
    }

    public void setSummaryReportTitle(String summaryReportTitle) {
        this.summaryReportTitle = summaryReportTitle;
    }

    public String getGestionTitle() {
        return gestionTitle;
    }

    public void setGestionTitle(String gestionTitle) {
        this.gestionTitle = gestionTitle;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public List<GestionPayroll> getGestionPayrollList() {
        return gestionPayrollList;
    }

    public void setGestionPayrollList(List<GestionPayroll> gestionPayrollList) {
        this.gestionPayrollList = gestionPayrollList;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    @Factory(value = "monthEnum")
    public Month[] getMonthEnum() {
        return Month.values();
    }


    private void setTestValues() {
        setSummaryReportTitle("RESUMEN DE PAGO PLANILLAS ADMINISTRATIVOS");
        setGestionTitle("DICIEMBRE DEL 2009");
        setGestionPayrollList(new ArrayList());
    }

    public Sector getSelectedSector() {
        return selectedSector;
    }

    public void setSelectedSector(Sector selectedSector) {
        this.selectedSector = selectedSector;
    }

//    public List<GestionPayroll> getFilterGestionList() {
//        System.out.println("entra getFilterGestionList");
//        if (getSector() != null) {
//            System.out.println("sector" + getSector().getName());
//        } else {
//            System.out.println("sector nulo");
//        }
//        return gestionPayrollService.filterGestionPayroll(initRange, endRange, sector);
//
//    }

    public void cleanGestionList() {
        setGestionPayrollList(null);
    }

    public void showGestionList() {
        System.out.println("gestionPayrollList" + gestionPayrollList);
        if (gestionPayrollList != null) {
            System.out.println("tama;o lista de gestiones" + gestionPayrollList.size());
            for (int i = 0; i < gestionPayrollList.size(); i++) {
                GestionPayroll gestionPayroll = gestionPayrollList.get(i);
                System.out.println(gestionPayroll.getGestionName());
            }
        }
    }

}