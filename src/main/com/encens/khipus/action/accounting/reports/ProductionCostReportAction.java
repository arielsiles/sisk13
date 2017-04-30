package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("productionCostReportAction")
@Scope(ScopeType.PAGE)
public class ProductionCostReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    @In(create = true)
    VoucherUpdateAction voucherUpdateAction;
    @In
    private VoucherService voucherService;
    @In
    private VoucherAccoutingService voucherAccoutingService;


    @Create
    public void init() {
        restrictions = new String[]{
                //"voucherDetail.account=#{majorAccountingReportAction.cashAccount.accountCode}"
        };
        //sortProperty = "date";
    }

    @Override
    protected String getEjbql() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        String ejbql = "";

            ejbql = " SELECT " +
                    "        productionCost.account, " +
                    "        productionCost.description, " +
                    "        SUM(productionCost.debit) as debit, " +
                    "        SUM(productionCost.credit) as credit " +
                    "  FROM  ProductionCost productionCost " +
                    "  WHERE productionCost.date between '" + start + "' and '" + end + "' " +
                    "  group by productionCost.account, productionCost.description ";

        return ejbql;
    }

    public void generateReport() {

            String documentTitle = "COSTO DE PRODUCCION MENSUAL";

            log.debug("Generating summary client state report...................");

            HashMap<String, Object> reportParameters = new HashMap<String, Object>();
            reportParameters.put("documentTitle", documentTitle);
            reportParameters.put("startDate", startDate);
            reportParameters.put("endDate", endDate);

            /*setReportFormat(ReportFormat.PDF);*/
            super.generateReport(
                    "productionCostReport",
                    "/accounting/reports/productionCostReport.jrxml",
                    PageFormat.LETTER,
                    PageOrientation.PORTRAIT,
                    messages.get("ProductionCost.report"),
                    reportParameters);

    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
