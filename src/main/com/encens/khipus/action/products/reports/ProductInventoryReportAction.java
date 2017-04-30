package com.encens.khipus.action.products.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("productInventoryReportAction")
@Scope(ScopeType.PAGE)
public class ProductInventoryReportAction extends GenericReportAction {



    @Create
    public void init() {
        restrictions = new String[]{
                //"voucher.id=#{voucherReportAction.voucherId}"
        };
        //sortProperty = "name";
    }

    @Override
    protected String getEjbql() {

        return " SELECT productInventory.productItemCode as code, " +
               "        productInventory.productItem.name, " +
               "        productInventory.quantity " +
               "  FROM  ProductInventory productInventory ";

    }

    public void generateReport() {

        log.debug("Generating products produced report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("tmp", "tmp");
        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "stockReport",
                "/products/reports/productStockReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.PORTRAIT,
                messages.get("Voucher.accountingEntry.titleReport"),
                reportParameters);
    }

}
