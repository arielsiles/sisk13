package com.encens.khipus.action.sales.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.sales.SalesOrder;
import com.encens.khipus.model.sales.SalesOrderNote;
import com.encens.khipus.model.sales.SalesOrderState;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.security.Restrict;

import javax.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Genera el PDF de una Orden de Venta con el formato del documento (una fila por
 * linea; la cabecera se pasa como parametros del reporte).
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALESORDER','VIEW')}")
public class SalesOrderReportAction extends GenericReportAction {

    @In(value = "#{entityManager}")
    private EntityManager entityManager;

    private Long salesOrderId;

    @Create
    public void init() {
        restrictions = new String[]{
                "salesOrderDetail.salesOrder.id = #{salesOrderReportAction.salesOrderId}"};
        sortProperty = "salesOrderDetail.detailNumber";
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "salesOrderDetail.detailNumber," +
                "salesOrderDetail.productItemCode," +
                "costCenter.code," +
                "salesOrderDetail.description," +
                "salesOrderDetail.quantity," +
                "salesOrderDetail.measureUnit," +
                "salesOrderDetail.unitPrice," +
                "salesOrderDetail.totalAmount" +
                " FROM SalesOrderDetail salesOrderDetail" +
                " LEFT JOIN salesOrderDetail.costCenter costCenter";
    }

    public void generateReport(SalesOrder order) {
        setReportFormat(ReportFormat.PDF);
        this.salesOrderId = order.getId();
        SalesOrder o = entityManager.find(SalesOrder.class, salesOrderId);

        Map params = new HashMap();
        params.put("P_number", o.getOrderNumber());
        params.put("P_date", formatDate(o.getDate()));
        params.put("P_deliveryDate", formatDate(o.getDeliveryDate()));
        params.put("P_state", o.getState() != null ? MessageUtils.getMessage(o.getState().getResourceKey()) : "");
        boolean approved = o.getState() != null && SalesOrderState.APR.equals(o.getState());
        params.put("P_approved", Boolean.valueOf(approved));
        params.put("P_securityCode", approved ? buildSecurityCode(o) : "");
        params.put("P_buyerName", nvl(o.getBuyerName()));
        params.put("P_buyerReg", nvl(o.getBuyerRegNumber()));
        params.put("P_buyerAddress", nvl(o.getBuyerAddress()));
        params.put("P_buyerPhone", nvl(o.getBuyerPhone()));
        params.put("P_buyerFax", nvl(o.getBuyerFax()));
        params.put("P_buyerEmail", nvl(o.getBuyerEmail()));
        params.put("P_incoterm", o.getIncoterm() != null ? o.getIncoterm().getCode() : "");
        params.put("P_incotermPlace", nvl(o.getIncotermPlace()));
        params.put("P_currency", currencySymbol(o.getCurrency()));
        params.put("P_quotation", nvl(o.getQuotation()));
        params.put("P_scCode", nvl(o.getScCode()));
        params.put("P_ccCode", nvl(o.getCcCode()));
        params.put("P_paymentConditions", nvl(o.getPaymentConditions()));
        params.put("P_headerTerm", nvl(o.getHeaderTerm()));
        params.put("P_comments", nvl(o.getComments()));
        params.put("P_subtotal", o.getSubTotalAmount());
        params.put("P_discount", o.getDiscountAmount());
        params.put("P_recharge", o.getRechargeAmount());
        params.put("P_total", o.getTotalAmount());
        params.put("P_notes", buildNotes(o));

        super.generateReport("salesOrderReport", "/sales/reports/salesOrderReport.jrxml",
                MessageUtils.getMessage("Reports.salesOrder.title"), params);
    }

    private String buildNotes(SalesOrder o) {
        StringBuilder sb = new StringBuilder();
        if (o.getSelectedNotes() != null) {
            for (SalesOrderNote note : o.getSelectedNotes()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("- ").append(note.getText());
            }
        }
        return sb.toString();
    }

    /**
     * Codigo de seguridad (firma) para ordenes aprobadas: hash SHA-256 de los
     * datos clave de la orden -> hex en mayusculas. Deterministico y verificable.
     */
    private String buildSecurityCode(SalesOrder o) {
        try {
            String raw = o.getId() + "|" + o.getOrderNumber()
                    + "|" + (o.getApprovedBy() != null ? o.getApprovedBy() : "")
                    + "|" + (o.getApprovedAt() != null ? o.getApprovedAt().getTime() : 0L)
                    + "|" + (o.getTotalAmount() != null ? o.getTotalAmount().toPlainString() : "0");
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return nvl(o.getOrderNumber());
        }
    }

    private String currencySymbol(FinancesCurrencyType currency) {
        return currency != null ? MessageUtils.getMessage(currency.getSymbolResourceKey()) : "";
    }

    private String formatDate(java.util.Date date) {
        return date != null ? new SimpleDateFormat("dd/MM/yyyy").format(date) : "";
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }
}
