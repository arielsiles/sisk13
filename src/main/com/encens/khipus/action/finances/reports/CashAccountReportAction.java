package com.encens.khipus.action.finances.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;

@Name("cashAccountReportAction")
@Scope(ScopeType.PAGE)
public class CashAccountReportAction extends GenericReportAction {

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    @Override
    protected String getEjbql() {

        return " SELECT cashAccount.accountCode as code, " +
               "        cashAccount.description as name, " +
               "        cashAccount.rootAccountCode as root, " +
               "        cashAccount.accountType as type, " +
               "        cashAccount.currency as currency, " +
               "        cashAccount.active as active, " +
               "        cashAccount.hasWarehousePermission as inv, " +
               "        cashAccount.expenseType as expenseType " +
               "  FROM  CashAccount cashAccount ";
    }

    public void generateReport() {

        log.debug("Generating chash account report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        String fileReport = "/finances/reports/cashAccountReport.jrxml";

        setReportFormat(ReportFormat.XLSX);
        super.generateReport(
                "cashAccountReport",
                fileReport,
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                "Plan de Cuentas",
                reportParameters);
    }


}
