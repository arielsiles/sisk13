package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to generate christmas bonus national summary report
 *
 * @author
 * @version 3.2
 */
@Name("christmasNationalSummaryReportAction")
@Restrict("#{s:hasPermission('CHRISTMASNATIONALSUMMARYREPORT','VIEW')}")
public class ChristmasNationalSummaryReportAction extends GenericReportAction {

    private List<GestionPayroll> gestionPayrollList;
    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;
    private Gestion gestion;
    private JobCategory jobCategory;
    private Boolean showCostCenterDetail = false;
    private GestionPayrollType gestionPayrollType = GestionPayrollType.CHRISTMAS_BONUS;


    public void generateReport() {
        //add seam properties as filter, summary only to OFFICIAL payroll
        setGeneratedPayrollType(GeneratedPayrollType.OFFICIAL);
        setGestionPayrollType(GestionPayrollType.CHRISTMAS_BONUS);

        Map params = new HashMap();
        params.put("groupTitleParam", MessageUtils.getMessage("Reports.christmasNationalSummary.groupTitle", getGestion().getYear()));
        params.put("groupFooterTitleParam", MessageUtils.getMessage("Reports.christmasNationalSummary.groupFooterTitle", getGestion().getYear()));

        //add SHOWCOSTCENTERDETAIL parameter for show cost center detail
        params.put("SHOWCOSTCENTERDETAIL", getShowCostCenterDetail());

        super.generateReport("christmasSummaryNationalReport", "/employees/reports/christmasNationalSummaryReport.jrxml", PageFormat.LETTER, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.christmasNationalSummary.title"), params);
    }

    @Override
    protected String getEjbql() {
        String gestionPayrollConditions = composeWhereGestionPayrollConditions("generatedPayroll.gestionPayroll.id");

        String ejbql = "SELECT DISTINCT " +
                "generatedPayroll.id," +
                "businessUnit.publicity," +
                "jobCategory.name," +
                "exchangeRate.rate," +
                "costCenter.code," +
                "costCenter.description" +
                " FROM GeneratedPayroll generatedPayroll" +
                " LEFT JOIN generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN gestionPayroll.businessUnit businessUnit" +
                " LEFT JOIN gestionPayroll.jobCategory jobCategory" +
                " LEFT JOIN gestionPayroll.exchangeRate exchangeRate" +
                " LEFT JOIN generatedPayroll.christmasPayrollList christmasPayroll" +
                " LEFT JOIN christmasPayroll.costCenter costCenter" +
                (gestionPayrollConditions != null ? " WHERE " + gestionPayrollConditions : "");

        return ejbql;
    }

    @Create
    public void init() {
        restrictions = new String[]{"generatedPayroll.generatedPayrollType=#{christmasNationalSummaryReportAction.generatedPayrollType}"};

        sortProperty = "generatedPayroll.id, businessUnit.publicity, jobCategory.name, costCenter.code";
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

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Boolean getShowCostCenterDetail() {
        return showCostCenterDetail;
    }

    public void setShowCostCenterDetail(Boolean showCostCenterDetail) {
        this.showCostCenterDetail = showCostCenterDetail;
    }

    public GestionPayrollType getGestionPayrollType() {
        return gestionPayrollType;
    }

    public void setGestionPayrollType(GestionPayrollType gestionPayrollType) {
        this.gestionPayrollType = gestionPayrollType;
    }

    public void cleanGestionList() {
        setGestionPayrollList(null);
    }
}
