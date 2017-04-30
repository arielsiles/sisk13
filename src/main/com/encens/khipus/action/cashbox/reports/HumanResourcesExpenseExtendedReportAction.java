package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.employees.HumanResourcesExpenseExtendedAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate human resources expense report
 *
 * @author
 * @version $Id: HumanResourcesExpenseExtendedReportAction.java  17-sep-2010 16:07:48$
 */
@Name("humanResourcesExpenseExtendedReportAction")
@Restrict("#{s:hasPermission('REPORTHUMANRESOURCEEXPENSEEXT','VIEW')}")
public class HumanResourcesExpenseExtendedReportAction extends GenericReportAction {

    @In
    private HumanResourcesExpenseExtendedAction humanResourcesExpenseExtendedAction;

    public void generateReport() {
        log.debug("Generating HumanResourcesExpenseExtendedReportAction............................");

        Map params = new HashMap();
        super.generateSqlReport("humanExpenseExtendedReport", "/cashbox/reports/humanResourcesExpenseExtendedReport.jrxml", MessageUtils.getMessage("Reports.humanResourcesExpenseExtended.title"), params);

    }

    @Override
    protected String getNativeSql() {
        return humanResourcesExpenseExtendedAction.getSql();
    }
}
