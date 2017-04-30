package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.OrganizationalLevel;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.service.employees.OrganizationalUnitService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate academic planning report
 *
 * @author
 * @version $Id: AcademicPlanningSummaryReportAction.java  07-jul-2010 17:09:19$
 */
@Name("academicPlanningSummaryReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTACADEMICPLANNINGSUMMARY','VIEW')}")
public class AcademicPlanningSummaryReportAction extends GenericReportAction {

    @In
    private OrganizationalUnitService organizationalUnitService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;

    private Cycle cycle;
    private BusinessUnit businessUnit;
    private OrganizationalLevel organizationalLevel;
    private OrganizationalUnit organizationalUnit;

    private String studyPlan;
    private Integer gestion;
    private Integer period;

    public void generateReport() {
        log.debug("Generating academicPlanningSummaryReportAction............................");

        //execute report only if this organization unit is career and is maped study plan of academic system
        if (organizationalUnit.getCareer() != null) {
            //set SQL filters
            setStudyPlan(organizationalUnit.getCareer());
            if (cycle != null) {
                Integer period = cycle.getCycleType().getPeriod();
                Integer gestion = cycle.getGestion().getYear();
                setPeriod(period);
                setGestion(gestion);
            }


            Map params = new HashMap();
            params.putAll(readReportParamsInfo());

            super.generateReport("academicPlanningReport", "/employees/reports/academicPlanningSummaryReport.jrxml", PageFormat.LEGAL, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.academicPlanning.title"), params);
        }

    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "academicPlanning.employeeCode," +
                "academicPlanning.acronym," +
                "academicPlanning.asignatureName," +
                "academicPlanning.theoreticalCharge," +
                "academicPlanning.practicalCharge," +
                "academicPlanning.scheduleCharge," +
                "academicPlanning.lastName," +
                "academicPlanning.maidenName," +
                "academicPlanning.firstName," +
                "academicPlanning.asignatureGroup," +
                "academicPlanning.groupType," +
                "academicPlanning.numberOfStudents," +
                "academicPlanning.semester" +
                " FROM AcademicPlanningSummary academicPlanning";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "academicPlanning.studyPlan=#{academicPlanningSummaryReportAction.studyPlan}",
                "academicPlanning.gestion=#{academicPlanningSummaryReportAction.gestion}",
                "academicPlanning.period=#{academicPlanningSummaryReportAction.period}"
        };

        sortProperty = " academicPlanning.lastName," +
                " academicPlanning.maidenName," +
                " academicPlanning.firstName," +
                " academicPlanning.asignatureName";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map readReportParamsInfo() {
        Map paramMap = new HashMap();

        String filtersInfo = "";
        if (businessUnit != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Reports.academicPlanning.sede") + ": " + businessUnit.getPublicity() + "\n";
        }
        if (cycle != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Reports.academicPlanning.gestion") + ": " + cycle.getName() + "\n";
        }
        if (organizationalUnit != null) {
            filtersInfo = filtersInfo + MessageUtils.getMessage("Reports.academicPlanning.career") + ": " + organizationalUnit.getName();
        }

        paramMap.put("filterInfoParam", filtersInfo);
        paramMap.put("susToBsExchangeParam", getSusToBsExchangeRate());
        paramMap.put("cycleParam", cycle);
        paramMap.put("organizationalUnitParam", organizationalUnit);

        return paramMap;
    }

    /**
     * get the exchange rate of $us to Bs
     *
     * @return Bs equivalent
     */
    private BigDecimal getSusToBsExchangeRate() {
        BigDecimal susToBsExchange = BigDecimal.ONE;
        try {
            susToBsExchange = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
        } catch (FinancesCurrencyNotFoundException e) {
            log.debug("$us currency not found... " + e.getMessage());
        } catch (FinancesExchangeRateNotFoundException e) {
            log.debug("$us exchange not found... " + e.getMessage());
        }
        return susToBsExchange;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public OrganizationalLevel getOrganizationalLevel() {
        return organizationalLevel;
    }

    public void setOrganizationalLevel(OrganizationalLevel organizationalLevel) {
        this.organizationalLevel = organizationalLevel;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public String getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    @SuppressWarnings({"NullableProblems"})
    public void refreshBusinessUnit() {
        setOrganizationalLevel(null);
        setOrganizationalUnit(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void refreshOrganizationalLevel() {
        setOrganizationalUnit(null);
    }

    public List<OrganizationalUnit> getOrganizationalUnitList() {
        return organizationalUnitService.getOrganizationalUnitByBusinessUnitLevelName(getBusinessUnit(), getOrganizationalLevel());
    }

}
