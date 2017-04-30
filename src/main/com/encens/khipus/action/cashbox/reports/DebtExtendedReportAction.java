package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.cashbox.DebtExtendedAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.service.academics.PeriodService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate debt extended report
 *
 * @author
 * @version $Id: DebtExtendedReportAction.java  10-ago-2010 16:33:46$
 */
@Name("debtExtendedReportAction")
@Restrict("#{s:hasPermission('REPORTDEBTEXTENDED','VIEW')}")
public class DebtExtendedReportAction extends GenericReportAction {

    @In
    private DebtExtendedAction debtExtendedAction;

    @In
    private PeriodService periodService;
    

    public void generateReport() {
        log.debug("Generating DebtExtendedReportAction............................");

        Map params = new HashMap();

        setReportFormat(ReportFormat.XLSX);
        super.generateSqlReport("debtExtendedReport", "/cashbox/reports/debtExtendedReport.jrxml", MessageUtils.getMessage("Reports.debtExtended.title"), params);
    }

    @Override
    protected String getNativeSql() {
        return debtExtendedAction.getSql();
    }

    /**
     * List of available periods
     * @return List
     */
    public List<Integer> getPeriods() {
        return periodService.findAllPeriods();
    }

}
