package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.cashbox.Branch;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.Map;

/**
 * PendingSoldProductDeliveryReportAction
 *
 * @author
 * @version 2.27
 */
@Name("pendingSoldProductDeliveryReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PENDINGSOLDPRODUCTDELIVERYREPORT','VIEW')}")
public class PendingSoldProductDeliveryReportAction extends GenericReportAction {

    private Branch branch;
    private ProductItem productItem;
    private String invoiceNumber;
    private String personalIdentification;
    private String names;
    private String firstName;
    private String secondName;

    public void generateReport() {
        log.debug("Generate PendingSoldProductDeliveryReportAction......");
        Map params = new HashMap();
        super.generateReport("pendingSoldProductDeliveryReport", "/warehouse/reports/pendingSoldProductDeliveryReport.jrxml", MessageUtils.getMessage("Reports.pendingSoldProductDelivery.title"), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT " +
                "branch.id," +
                "branch.description," +
                "soldProduct.invoiceNumber," +
                "soldProduct.personalIdentification," +
                "soldProduct.names," +
                "soldProduct.firstName," +
                "soldProduct.secondName," +
                "productItem.productItemCode," +
                "productItem.name," +
                "usageMeasureUnit.name," +
                "soldProduct.quantity" +
                " FROM SoldProduct soldProduct" +
                " LEFT JOIN soldProduct.productItem productItem" +
                " LEFT JOIN productItem.usageMeasureUnit usageMeasureUnit" +
                " LEFT JOIN soldProduct.branch branch";

    }

    @Create
    public void init() {
        restrictions = new String[]{
                "branch=#{pendingSoldProductDeliveryReportAction.branch}",
                "productItem=#{pendingSoldProductDeliveryReportAction.productItem}",
                "lower(soldProduct.invoiceNumber) like concat(lower(#{pendingSoldProductDeliveryReportAction.invoiceNumber}), '%')",
                "lower(soldProduct.personalIdentification) like concat(lower(#{pendingSoldProductDeliveryReportAction.personalIdentification}), '%')",
                "lower(soldProduct.firstName) like concat('%', concat(lower(#{pendingSoldProductDeliveryReportAction.firstName}), '%'))",
                "lower(soldProduct.secondName) like concat('%', concat(lower(#{pendingSoldProductDeliveryReportAction.secondName}), '%'))",
                "lower(soldProduct.names) like concat('%', concat(lower(#{pendingSoldProductDeliveryReportAction.names}), '%'))",
                "soldProduct.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.SoldProductState','PENDING')}"
        };

        sortProperty = "branch.id,soldProduct.invoiceNumber,productItem.productItemCode";
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPersonalIdentification() {
        return personalIdentification;
    }

    public void setPersonalIdentification(String personalIdentification) {
        this.personalIdentification = personalIdentification;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
}
