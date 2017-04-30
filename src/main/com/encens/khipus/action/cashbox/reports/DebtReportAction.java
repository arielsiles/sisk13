package com.encens.khipus.action.cashbox.reports;

import com.encens.khipus.action.cashbox.DebtFiltersAction;
import com.encens.khipus.action.reports.GenericReportAction;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * @author
 * @version 2.17
 */
@Name("debtReportAction")
public class DebtReportAction extends GenericReportAction {
    @In
    private DebtFiltersAction debtFiltersAction;

    public void generateReport() {
        log.debug("-------------------------------------------------");
        log.debug(getNativeSql());
        log.debug("-------------------------------------------------");
    }

    @Override
    protected String getNativeSql() {
        return debtFiltersAction.getSql();
    }
}
