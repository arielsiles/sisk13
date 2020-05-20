package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("saleReportAction")
@Scope(ScopeType.PAGE)
public class SaleReportAction extends GenericReportAction {

    @In
    private User currentUser;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private SaleService saleService;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {

        Long customerOrderId = saleService.findLastSaleId(currentUser);
        System.out.println("-----------> ID: " + customerOrderId);
        System.out.println("-----------> user: " + currentUser.getUsername());

        return "SELECT " +
                " customerOrder.client, " +
                " customerOrder.orderDate, " +
                " articleOrder.codArt as productItemCode, " +
                " articleOrder.productItem, " +
                " articleOrder.price, " +
                " articleOrder.quantity, " +
                " articleOrder.amount, " +
                " articleOrder.promotion, " +
                " articleOrder.reposicion as replacement" +
                " FROM  CustomerOrder as customerOrder " +
                " JOIN customerOrder.articleOrderList articleOrder " +
                " WHERE customerOrder.id = " + customerOrderId;
    }

    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();
        }

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("currentUser.username", currentUser.getUsername());
        reportParameters.put("companyName", companyConfiguration.getCompanyName());
        reportParameters.put("locationName", companyConfiguration.getLocationName());
        reportParameters.put("systemName", companyConfiguration.getSystemName());
        reportParameters.put("documentTitle", messages.get("Sale.report.title"));

        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "saleReport",
                "/customers/reports/saleReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Sale.report.title"),
                reportParameters);
    }
}