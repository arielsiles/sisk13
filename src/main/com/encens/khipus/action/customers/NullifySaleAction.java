package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.2
 */

@Name("nullifySaleAction")
@Scope(ScopeType.PAGE)
public class NullifySaleAction {

    @In
    private SaleService saleService;

    private Date date;
    private SaleTypeEnum saleType;
    private String code;

    private CustomerOrder customerOrder;
    private String name;
    private String invoiceNumber;
    private BigDecimal totalAmount;

    @Factory(value = "saleTypes")
    public SaleTypeEnum[] getSaleTypes() {
        return SaleTypeEnum.values();
    }


    public void findSale(){

        customerOrder = saleService.findCustomerOrderByParams(saleType, date, code);

        if (customerOrder != null){
            setName(customerOrder.getClient().getFullName());
            setTotalAmount(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()));
            if (customerOrder.getMovement() != null)
                setInvoiceNumber(customerOrder.getMovement().getNumber().toString());
            else
                setInvoiceNumber(MessageUtils.getMessage("Sales.nullifySale.withoutInvoice"));
        }

    }

    public String annulSale(){

        return Outcome.SUCCESS;
    }

    public SaleTypeEnum getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleTypeEnum saleType) {
        this.saleType = saleType;
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
