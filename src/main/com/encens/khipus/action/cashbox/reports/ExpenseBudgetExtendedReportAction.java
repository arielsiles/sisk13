package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.finances.ExpenseBudgetExtendedAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate expense budget extended report
 *
 * @author
 * @version $Id: ExpenseBudgetExtendedReportAction.java  20-ago-2010 17:13:37$
 */
@Name("expenseBudgetExtendedReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTEXPENSEBUDGETEXTENDED','VIEW')}")
public class ExpenseBudgetExtendedReportAction extends GenericReportAction {

    @In
    private ExpenseBudgetExtendedAction expenseBudgetExtendedAction;

    public void generateReport() {
        Map parameters = new HashMap();

        super.generateSqlReport("expenseBudgetExtReport", "/cashbox/reports/expenseBudgetExtendedReport.jrxml", PageFormat.LEGAL, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.expenseBudgetExtended.title"), parameters);
    }

    @Override
    protected String getNativeSql() {
        return expenseBudgetExtendedAction.getSql();
    }
}
