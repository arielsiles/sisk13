package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.cashbox.DebtByStudentFiltersAction;
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
 * Action to execute debt by student report
 *
 * @author
 * @version 2.16
 */
@Name("debtByStudentReportAction")
@Restrict("#{s:hasPermission('REPORTDEBTBYSTUDENT','VIEW')}")
public class DebtByStudentReportAction extends GenericReportAction {

    @In
    private DebtByStudentFiltersAction debtByStudentFiltersAction;

    public void generateReport() {
        Map parameters = new HashMap();
        super.generateSqlReport("debtByStudentReport", "/cashbox/reports/debtByStudentReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.debtByStudent.title"), parameters);
    }

    @Override
    protected String getNativeSql() {
        return debtByStudentFiltersAction.getSql();
    }
}
