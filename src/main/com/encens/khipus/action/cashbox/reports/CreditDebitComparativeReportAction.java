package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the credit/Debit comparative report action
 *
 * @author
 * @version 2.17
 */
@Name("creditDebitComparativeReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTCREDDEBCOMP','VIEW')}")
public class CreditDebitComparativeReportAction extends GenericReportAction {
    private String executorUnitCode;
    private CostCenter costCenter;
    private Gestion gestion;
    @In
    private GenericService genericService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;

    protected String getNativeSql() {
        CreditDebitComparativeReportSql creditDebitComparativeReportSql = new CreditDebitComparativeReportSql();
        if (gestion != null) {
            creditDebitComparativeReportSql.setYear(gestion.getYear().toString());
        }
        if (executorUnitCode != null) {
            creditDebitComparativeReportSql.setBusinessUnitCode(executorUnitCode);
        }
        if (costCenter != null) {
            creditDebitComparativeReportSql.setCostCenterCode(costCenter.getCode());
        }


        return (creditDebitComparativeReportSql.getSql());
    }

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    public void generateReport() {
        log.debug("generating creditDebitComparativeReport......................................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        //Get last exchangeRate
        try {
            BigDecimal exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
            reportParameters.put("generalExchangeRate", exchangeRate.doubleValue());
        } catch (FinancesCurrencyNotFoundException e) {
            log.error("Currency not found exception... ", e);
        } catch (FinancesExchangeRateNotFoundException e) {
            log.error("ExchangeRate not found exception", e);
        }
        if (gestion != null) {
            reportParameters.put("year", gestion.getYear().toString());
        }

        super.generateSqlReport(
                "creditDebitComparativeReport",
                "/cashbox/reports/creditDebitComparativeReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.finances.creditDebitComparative"),
                reportParameters);
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public String getCostCenterFullName() {
        return costCenter != null ? costCenter.getFullName() : null;
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearCostCenter() {
        setCostCenter(null);
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public void assignCostCenter(CostCenter costCenter) {
        if (costCenter != null) {
            try {
                setCostCenter(genericService.findById(CostCenter.class, costCenter.getId()));
            } catch (EntryNotFoundException e) {
                // EntryNotFoundException
            }
        }
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }
}


