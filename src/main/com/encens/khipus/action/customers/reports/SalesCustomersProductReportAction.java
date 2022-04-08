package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("salesCustomersProductReportAction")
@Scope(ScopeType.PAGE)
public class SalesCustomersProductReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private SaleTypeEnum saleType;

    private SaleStatus annuledState = SaleStatus.ANULADO;

    @Create
    public void init() {
        restrictions = new String[]{
                "customerOrder.state<>#{salesCustomersProductReportAction.annuledState}",
                "customerOrder.orderDate>=#{salesCustomersProductReportAction.startDate}",
                "customerOrder.orderDate<=#{salesCustomersProductReportAction.endDate}",
                "customerOrder.saleType=#{salesCustomersProductReportAction.saleType}"
        };
    }


    protected String getEjbql() {

        String ejbql = "";

        //String dateParam = DateUtils.format(this.endPeriodDate, "yyyy-MM-dd");

        ejbql = " SELECT " +

                    " client.id as clientId," +
                    " client.nitNumber as doc," +
                    " client.name || ' ' || client.lastName || ' ' || client.maidenName as name," +
                    " territory.nombre as zone," +
                    " SUM(articleOrder.amount) as amount," +
                    " COUNT(customerOrder.id) as quantity" +
                    " FROM ArticleOrder articleOrder" +
                    " LEFT JOIN articleOrder.customerOrder customerOrder" +
                    " LEFT JOIN customerOrder.client client" +
                    " LEFT JOIN articleOrder.productItem productItem" +
                    " LEFT JOIN client.territoriotrabajo territory" +
                    " GROUP BY client.id, client.nitNumber, territory.nombre";

        return ejbql;
    }


    public void generateReport() {

        String documentTitle = "VENTAS POR CLIENTE Y PRODUCTO";
        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("period", period);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/salesCustomerProductReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Customers.report.salesCustomerAndProduct.title"),
                reportParameters);

    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SaleStatus getAnnuledState() {
        return annuledState;
    }

    public void setAnnuledState(SaleStatus annuledState) {
        this.annuledState = annuledState;
    }

    public SaleTypeEnum getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleTypeEnum saleType) {
        this.saleType = saleType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}