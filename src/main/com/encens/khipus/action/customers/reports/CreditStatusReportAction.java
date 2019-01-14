package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.warehouse.reports.KardexProductMovementAction;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditType;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.SingleProduct;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.warehouse.InitialInventoryService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.ProductInventoryService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("creditStatusReportAction")
@Scope(ScopeType.PAGE)
public class CreditStatusReportAction extends GenericReportAction {

    private Date startDate = new Date();
    private Date endDate = new Date();

    private CreditState creditState;
    private CreditType creditType;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {

        String ejbql = "";

        ejbql = " SELECT " +
                " credit.state || ' - ' || creditType.name AS status," +
                " credit.state," +
                " creditType.name as creditTypeName," +
                " productiveZone.name AS gabName," +
                " partner.firstName," +
                " partner.lastName," +
                " partner.maidenName," +
                " credit.grantDate," +
                " credit.amount," +
                " credit.capitalBalance," +
                " credit.lastPayment" +
                " FROM Credit credit" +
                " LEFT JOIN credit.creditType creditType" +
                " LEFT JOIN credit.partner partner" +
                " LEFT JOIN partner.productiveZone productiveZone" +
                " ORDER BY credit.state, creditType.name";

        if (creditState != null){

            ejbql = " SELECT " +
                    " credit.state || ' - ' || creditType.name AS status," +
                    " credit.state," +
                    " creditType.name as creditTypeName," +
                    " productiveZone.name AS gabName," +
                    " partner.firstName," +
                    " partner.lastName," +
                    " partner.maidenName," +
                    " credit.grantDate," +
                    " credit.amount," +
                    " credit.capitalBalance," +
                    " credit.lastPayment" +
                    " FROM Credit credit" +
                    " LEFT JOIN credit.creditType creditType" +
                    " LEFT JOIN credit.partner partner" +
                    " LEFT JOIN partner.productiveZone productiveZone" +
                    " WHERE credit.state = '" + creditState.toString() +"'" +
                    " ORDER BY credit.state, creditType.name";

        }

        if (creditType != null){

            ejbql = " SELECT " +
                    " credit.state || ' - ' || creditType.name AS status," +
                    " credit.state," +
                    " creditType.name as creditTypeName," +
                    " productiveZone.name AS gabName," +
                    " partner.firstName," +
                    " partner.lastName," +
                    " partner.maidenName," +
                    " credit.grantDate," +
                    " credit.amount," +
                    " credit.capitalBalance," +
                    " credit.lastPayment" +
                    " FROM Credit credit" +
                    " LEFT JOIN credit.creditType creditType" +
                    " LEFT JOIN credit.partner partner" +
                    " LEFT JOIN partner.productiveZone productiveZone" +
                    " WHERE credit.creditType = " + creditType.getId() +
                    " ORDER BY credit.state, creditType.name";

        }

        if (creditState != null && creditType != null){

            ejbql = " SELECT " +
                    " credit.state || ' - ' || creditType.name AS status," +
                    " credit.state," +
                    " creditType.name as creditTypeName," +
                    " productiveZone.name AS gabName," +
                    " partner.firstName," +
                    " partner.lastName," +
                    " partner.maidenName," +
                    " credit.grantDate," +
                    " credit.amount," +
                    " credit.capitalBalance," +
                    " credit.lastPayment" +
                    " FROM Credit credit" +
                    " LEFT JOIN credit.creditType creditType" +
                    " LEFT JOIN credit.partner partner" +
                    " LEFT JOIN partner.productiveZone productiveZone" +
                    " WHERE credit.state = '" + creditState.toString() +"'" +
                    " AND credit.creditType = " + creditType.getId() +
                    " ORDER BY credit.state, creditType.name";

        }


        return ejbql;
    }


    public void generateReport() {

        String documentTitle = "ESTADO DE CARTERA";
        log.debug("Generating credit status report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("documentTitle", documentTitle);
        //reportParameters.put("startDate", startDate);
        //reportParameters.put("endDate", endDate);

        super.generateReport(
                "summaryProviderKardexReport",
                "/customers/reports/creditStatusReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.credit.creditStatus.title"),
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


    public CreditState getCreditState() {
        return creditState;
    }

    public void setCreditState(CreditState creditState) {
        this.creditState = creditState;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }
}