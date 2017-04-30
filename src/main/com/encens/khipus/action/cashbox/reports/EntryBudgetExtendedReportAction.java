package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.finances.EntryBudgetExtendedAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate entry budget report
 *
 * @author
 * @version $Id: EntryBudgetReportAction.java  29-jul-2010 18:47:19$
 */
@Name("entryBudgetExtendedReportAction")
@Restrict("#{s:hasPermission('REPORTENTRYBUDGET','VIEW')}")
public class EntryBudgetExtendedReportAction extends GenericReportAction {

    @In
    private EntryBudgetExtendedAction entryBudgetExtendedAction;

    public void generateReport() {
        log.debug("Generating EntryBudgetExtendedReportAction............................");

        Map params = new HashMap();
        super.generateSqlReport("entryBudgetReport", "/cashbox/reports/entryBudgetExtendedReport.jrxml", PageFormat.LEGAL, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.entryBudget.title"), params);
    }

    @Override
    protected String getNativeSql() {
        return entryBudgetExtendedAction.getSql();
    }
}
