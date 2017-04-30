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
 * Action to generate salary managers national summary report
 *
 * @author
 * @version 3.4
 */
@Name("managersPayrollSummaryReportAction")
@Restrict("#{s:hasPermission('MANAGERSNATIONALSUMMARYPAYROLLREPORT','VIEW')}")
public class ManagersPayrollSummaryReportAction extends GenericReportAction {

    private List<GestionPayroll> gestionPayrollList;
    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;
    private Gestion gestion;
    private Month month;
    private JobCategory jobCategory;
    private String comments;

    public void generateReport() {
        //add seam properties as filter, summary only to OFFICIAL payroll
        setGeneratedPayrollType(GeneratedPayrollType.OFFICIAL);

        Map params = new HashMap();
        params.putAll(getReportParamsInfo());

        super.generateReport("managersSummaryReport", "/employees/reports/managersPayrollSummaryReport.jrxml", PageFormat.LETTER, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.managersPayrollSummary.title"), params);
    }

    @Override
    protected String getEjbql() {
        String gestionPayrollConditions = composeWhereGestionPayrollConditions("gestionPayroll.id");

        String ejbql = "SELECT DISTINCT " +
                "generatedPayroll.id," +
                "businessUnit.id," +
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
                " LEFT JOIN generatedPayroll.managersPayrollList managersPayroll" +
                " LEFT JOIN managersPayroll.costCenter costCenter" +
                (gestionPayrollConditions != null ? " WHERE " + gestionPayrollConditions : "");

        return ejbql;
    }

    @Create
    public void init() {
        restrictions = new String[]{"generatedPayroll.generatedPayrollType=#{managersPayrollSummaryReportAction.generatedPayrollType}"};

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

    /**
     * Read report params
     *
     * @return Map
     */
    private Map getReportParamsInfo() {
        Map paramMap = new HashMap();

        paramMap.put("groupTitleParam", MessageUtils.getMessage("Reports.managersPayrollSummary.groupTitle", MessageUtils.getMessage(getMonth().getResourceKey()), getGestion().getYear()));
        paramMap.put("groupFooterTitleParam", MessageUtils.getMessage("Reports.managersPayrollSummary.groupFooterTitle", MessageUtils.getMessage(getMonth().getResourceKey()), getGestion().getYear()));
        paramMap.put("commentsParam", (comments != null) ? comments : "");

        paramMap.put("jobCategoryParam", jobCategory);
        paramMap.put("gestionParam", gestion);
        paramMap.put("monthParam", month);


        return paramMap;
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

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void cleanGestionList() {
        setGestionPayrollList(null);
    }
}
