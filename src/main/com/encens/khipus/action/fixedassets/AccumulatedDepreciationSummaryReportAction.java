package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the accumulated depreciations summary report action
 *
 * @author
 * @version 2.3
 */

@Name("accumulatedDepreciationSummaryReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ACCUMULATEDDEPRECIATIONSUMMARYREPORT','VIEW')}")
public class AccumulatedDepreciationSummaryReportAction extends GenericReportAction {
    private FixedAssetGroup fixedAssetGroup;
    private Date initDateRange;
    private Date endDateRange;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @Create
    public void init() {
        restrictions = new String[]{
                "fixedAssetGroup=#{accumulatedDepreciationSummaryReportAction.fixedAssetGroup}",
                "fixedAssetDepreciationRecord.depreciationDate>=#{accumulatedDepreciationSummaryReportAction.initDateRange}",
                "fixedAssetDepreciationRecord.depreciationDate<=#{accumulatedDepreciationSummaryReportAction.endDateRange}"
        };
        sortProperty = "fixedAssetGroup.description";
        groupByProperty = "fixedAssetGroup.id, fixedAssetGroup.description";
    }

    @Override
    protected String getEjbql() {
        return "SELECT fixedAssetGroup.id, " +
                "      fixedAssetGroup.description, " +
                "      SUM(fixedAssetDepreciationRecord.acumulatedDepreciation), " +
                "      SUM(fixedAssetDepreciationRecord.totalValue), " +
                "      SUM(fixedAssetDepreciationRecord.totalValue - fixedAssetDepreciationRecord.acumulatedDepreciation)" +
                "FROM  FixedAssetDepreciationRecord fixedAssetDepreciationRecord " +
                "      LEFT JOIN fixedAssetDepreciationRecord.fixedAsset fixedAsset " +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup " +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                "WHERE fixedAssetDepreciationRecord.id = (select MAX(dr.id) from FixedAssetDepreciationRecord dr where dr.fixedAsset = fixedAsset ) ";

    }

    public void generateReport() {
        log.debug("generating accumulatedDepreciationSummaryReport......................................");
        BigDecimal bsExchange = BigDecimal.ZERO;
        try {
            bsExchange = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.name());
        } catch (FinancesCurrencyNotFoundException e) {
            log.debug("Bolivianos currency not found... " + e.getMessage());
        } catch (FinancesExchangeRateNotFoundException e) {
            log.debug("Bolivianos exchange not found... " + e.getMessage());
        }

        String dateRange = "";
        dateRange += (initDateRange != null) ? MessageUtils.getMessage("DepreciationsSummaryReport.report.from",
                DateUtils.format(initDateRange, MessageUtils.getMessage("patterns.date"))) + " " : "";
        dateRange += (endDateRange != null) ? MessageUtils.getMessage("DepreciationsSummaryReport.report.to",
                DateUtils.format(endDateRange, MessageUtils.getMessage("patterns.date"))) : "";

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("BS_EXCHANGE", bsExchange);
        reportParameters.put("dateRangeParam", dateRange);
        super.generateReport(
                "accumulatedDepreciationSummaryReport",
                "/fixedassets/reports/accumulatedDepreciationSummaryReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.LANDSCAPE,
                messages.get("AccumulatedDepreciationSummary.report.title"),
                reportParameters);
    }

    public Date getInitDateRange() {
        return initDateRange;
    }

    public void setInitDateRange(Date initDateRange) {
        this.initDateRange = initDateRange;
    }

    public Date getEndDateRange() {
        return endDateRange;
    }

    public void setEndDateRange(Date endDateRange) {
        this.endDateRange = endDateRange;
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        if (null != fixedAssetGroup) {
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, fixedAssetGroup.getId());
        } else {
            this.fixedAssetGroup = null;
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
    }

}
