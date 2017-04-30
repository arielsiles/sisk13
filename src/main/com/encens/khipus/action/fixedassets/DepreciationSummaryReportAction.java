package com.encens.khipus.action.fixedassets;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
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
 * This class implements the fixed asset depreciations summary report action
 *
 * @author
 * @version 2.3
 */

@Name("depreciationSummaryReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DEPRECIATIONSUMMARYREPORT','VIEW')}")
public class DepreciationSummaryReportAction extends GenericReportAction {
    private Date initDateRange;
    private Date endDateRange;
    private String groupDescription;
    private FixedAssetGroup fixedAssetGroup;
    private FixedAssetSubGroup fixedAssetSubGroup;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @Create
    public void init() {
        restrictions = new String[]{
                "fixedAssetDepreciationRecord.depreciationDate>=#{depreciationSummaryReportAction.initDateRange}",
                "fixedAssetDepreciationRecord.depreciationDate<=#{depreciationSummaryReportAction.endDateRange}",
                "fixedAssetGroup=#{depreciationSummaryReportAction.fixedAssetGroup}",
                "fixedAssetSubGroup=#{depreciationSummaryReportAction.fixedAssetSubGroup}",
        };
        sortProperty = "fixedAssetGroup.description";
        groupByProperty = "fixedAssetGroup.id, fixedAssetGroup.description";
    }

    @Override
    protected String getEjbql() {
        return "SELECT fixedAssetGroup.id, " +
                "      fixedAssetGroup.description, " +
                "      SUM(fixedAssetDepreciationRecord.depreciation) " +
                "FROM  FixedAssetDepreciationRecord fixedAssetDepreciationRecord " +
                "      LEFT JOIN fixedAssetDepreciationRecord.fixedAsset fixedAsset " +
                "      LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup " +
                "      LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup ";

    }

    public void generateReport() {
        log.debug("generating depreciationSummaryReport......................................");
        BigDecimal bsExchange = BigDecimal.ZERO;
        try {
            bsExchange = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.U.toString());
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
                "depreciationSummaryReport",
                "/fixedassets/reports/depreciationSummaryReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("DepreciationsSummaryReport.report.title"),
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

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        if (null != fixedAssetGroup) {
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, fixedAssetGroup.getId());
            fixedAssetSubGroup = null;
        } else {
            this.fixedAssetGroup = null;
        }
    }

    public FixedAssetSubGroup getFixedAssetSubGroup() {
        return fixedAssetSubGroup;
    }

    public void setFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        if (fixedAssetSubGroup != null) {
            this.fixedAssetSubGroup = getEntityManager().find(FixedAssetSubGroup.class, fixedAssetSubGroup.getId());
            this.fixedAssetGroup = getEntityManager().find(FixedAssetGroup.class, this.fixedAssetSubGroup.getFixedAssetGroup().getId());
        } else {
            this.fixedAssetSubGroup = null;
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearFixedAssetGroup() {
        setFixedAssetGroup(null);
        fixedAssetSubGroup = null;
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearFixedAssetSubGroup() {
        setFixedAssetSubGroup(null);
    }

}
