package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.cashbox.IncomeByCashboxExtendedAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate income by cashbox report
 *
 * @author
 * @version $Id: IncomeByCashboxReportAction.java  16-jul-2010 18:12:39$
 */
@Name("incomeByCashboxReportAction")
@Restrict("#{s:hasPermission('REPORTINCOMEBYCASHBOX','VIEW')}")
public class IncomeByCashboxReportAction extends GenericReportAction {

    @In
    private IncomeByCashboxExtendedAction incomeByCashboxExtendedAction;

    public void generateReport() {
        log.debug("Generating IncomeByCashboxReportAction............................");

        Map params = new HashMap();
        super.generateSqlReport("incomeByCashboxReport", "/cashbox/reports/incomeByCashboxReport.jrxml", PageFormat.LEGAL, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.incomeByCashbox.title"), params);
    }

    @Override
    protected String getNativeSql() {
        return incomeByCashboxExtendedAction.getSql();
    }

    /**
     * calculate SUS total value
     *
     * @param usdValue
     * @param bsValue
     * @param exchangeRate
     * @return BigDecimal
     */
    public static BigDecimal calculateUsdTotalValue(BigDecimal usdValue, BigDecimal bsValue, BigDecimal exchangeRate) {
        BigDecimal totalValue = null;

        if (usdValue != null && bsValue != null && exchangeRate != null) {
            totalValue = BigDecimalUtil.sum(usdValue, BigDecimalUtil.divide(bsValue, exchangeRate));
        }
        return totalValue;
    }
}
