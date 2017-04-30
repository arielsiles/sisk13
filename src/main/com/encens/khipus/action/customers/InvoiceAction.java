package com.encens.khipus.action.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.customers.InvoiceDetail;
import com.encens.khipus.model.products.Product;
import com.encens.khipus.model.products.ProductDiscountRule;
import com.encens.khipus.service.customers.*;
import com.encens.khipus.service.products.ProductService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.international.StatusMessage;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice creation operations
 *
 * @author
 * @version $Id: InvoiceAction.java 2008-9-10 14:03:36 $
 */

@Name("invoiceAction")
@Scope(ScopeType.CONVERSATION)
public class InvoiceAction extends GenericAction<Invoice> {

    @In(required = true)
    @Out
    private Entity customer;

    @In(required = true)
    private Invoice invoice;

    @DataModelSelection
    private InvoiceDetail selectedDetail;

    private String productCode;
    private Integer productQuantity = 1;

    public static final String TYPE_CASH = "CASH";
    public static final String TYPE_CREDIT_CARD = "CREDIT_CARD";
    public static final String TYPE_ACCOUNT = "ACCOUNT";
    private String paymentType = TYPE_CASH;
    private BigDecimal cashAmount;
    private BigDecimal change = BigDecimal.ZERO;
    private String code;
    private BigDecimal productPrice;
    private String productName;

    private BigDecimal customerDiscountAmount;
    private BigDecimal customerDiscountPercentage;

    private BigDecimal actualTotalAmount = BigDecimal.ZERO;
    private List<InvoiceDetail> deletedDetails = new ArrayList<InvoiceDetail>(0);

    @In
    private ProductService productService;

    @In
    private InvoiceService invoiceService;

    @In
    private CreditService creditService;

    @In
    private CreditTransactionService creditTransactionService;

    @In
    private CustomerDiscountService customerDiscountService;

    @In
    private CustomerService customerService;

    private Long invoiceDetailId;
    private boolean printHeader = true;

    @DataModel
    public List<InvoiceDetail> getDetails() {
        return getInstance().getDetails();
    }

    @Override
    public Invoice getInstance() {
        return invoice;
    }

    public void addProduct() {
        Product product = productService.findByCode(productCode);
        printHeader = false;

        if (product != null) {
            //getInstance().getDetails().add(0, new InvoiceDetail(product, productQuantity));
            getInstance().getDetails().add(0, createInvoiceDetail(product, productQuantity));
            productQuantity = 1;
            setTotalDiscount();
            invoiceService.update(getInstance());
            invoiceDetailId = getInstance().getDetails().get(0).getId();

        } else {
            invoiceDetailId = null;
            facesMessages.addToControlFromResourceBundle("productCode", StatusMessage.Severity.ERROR, "Invoice.error.productNotFound");
        }
    }

    public void deleteProduct() {
        //getInstance().getDetails().remove(selectedDetail);       
        InvoiceDetail detail = createInvoiceDetail(selectedDetail.getProduct(), -selectedDetail.getQuantity());
        getInstance().getDetails().add(0, detail);
        setTotalDiscount();
        invoiceService.update(getInstance());
        invoiceDetailId = getInstance().getDetails().get(0).getId();
    }

    public void selectDetail() {
    }

    public boolean isDetailSelected() {
        return selectedDetail != null;
    }

    @Override
    @End(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, beforeRedirect = true)
    public String create() {

        if (isTypeCash() && getCashAmount() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.required", SeamResourceBundle.getBundle().getString("Invoice.cash"));
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }

        try {
            if (getInstance().getDetails().size() == 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Invoice.error.emptyProducts");
                return com.encens.khipus.framework.action.Outcome.REDISPLAY;
            }

            invoiceService.save(getInstance(), customerService.findByEntity(customer));

            if (isTypeAccount()) {
                creditTransactionService.create(getCredit(), getInstance());
            }

            return com.encens.khipus.framework.action.Outcome.SUCCESS;

        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
    }

    @Override
    @End(beforeRedirect = true)
    public String cancel() {
        invoiceService.remove(getInstance());
        return com.encens.khipus.framework.action.Outcome.CANCEL;
    }

    public boolean isPerson() {
        return customer instanceof Person;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }


    public void cash() {
        paymentType = TYPE_CASH;
    }

    public void creditCard() {
        paymentType = TYPE_CREDIT_CARD;
    }

    public void account() {
        if (getCredit() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Invoice.error.noAccountAssigned");
        }
        paymentType = TYPE_ACCOUNT;
    }

    public boolean isTypeCash() {
        return TYPE_CASH.equals(paymentType);
    }

    public boolean isTypeCreditCard() {
        return TYPE_CREDIT_CARD.equals(paymentType);
    }

    public boolean isTypeAccount() {
        return TYPE_ACCOUNT.equals(paymentType) && isCreditAssigned();
    }

    public BigDecimal getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(BigDecimal cashAmount) {
        this.cashAmount = cashAmount;
    }

    public void validateCashAmount(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        double amount = ((BigDecimal) value).doubleValue();
        actualTotalAmount = getInstance().getTotalAmount().subtract(getInstance().getTotalDiscount());

        if (amount < actualTotalAmount.doubleValue()) {
            throw new ValidatorException(null);
        }
    }

    public BigDecimal getChange() {
        try {
            change = cashAmount.subtract(getInstance().getTotalAmount().subtract(getInstance().getTotalDiscount()));
            if (change.doubleValue() < 0) {
                change = BigDecimal.ZERO;
            }

        } catch (NullPointerException e) {
        }
        return change;
    }

    public void cashAmountChange(ValueChangeEvent value) {
        this.change = ((BigDecimal) value.getNewValue()).subtract(getInstance().getTotalAmount());
    }

    public Credit getCredit() {
        return creditService.findByEntity(customer);
    }

    public BigDecimal getCreditAmount() {
        return creditService.getActualCreditBalance(getCredit());
    }

    public BigDecimal getCreditBalance() {
        Credit credit = getCredit();
        BigDecimal creditBalance = creditService.getActualCreditBalance(credit);
        if (getInstance().getTotalDiscount().doubleValue() > 0) {
            return creditBalance.subtract(getInstance().getTotalAmount().subtract(getInstance().getTotalDiscount()));
        } else {
            return creditBalance.subtract(getInstance().getTotalAmount());
        }
    }

    public boolean isCreditAssigned() {
        return getCredit() != null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void searchProduct() {
        Product product = productService.findByCode(code);
        if (product != null) {
            productPrice = product.getSellPrice();
            productName = product.getName();
        } else {
            productPrice = null;
            productName = null;
            facesMessages.addToControlFromResourceBundle("code", StatusMessage.Severity.ERROR, "Invoice.error.productNotFound");
        }
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public String getProductName() {
        return productName;
    }

    public boolean isProductFound() {
        return productPrice != null;
    }

    public BigDecimal getCustomerDiscountAmount() {
        customerDiscountAmount = customerDiscountService.getTotalDiscountAmount(customerService.findByEntity(customer));
        return customerDiscountAmount;
    }

    public BigDecimal getCustomerDiscountPercentage() {
        customerDiscountPercentage = customerDiscountService.getTotalDiscountPercentage(customerService.findByEntity(customer));
        return customerDiscountPercentage;
    }

    public BigDecimal getActualTotalAmount() {
        actualTotalAmount = getInstance().getTotalAmount().subtract(getInstance().getTotalDiscount());
        return actualTotalAmount;
    }

    public boolean isDiscountAssigned() {
        return customerService.findByEntity(customer).getDiscounts().size() > 0;
    }

    public void setTotalDiscount() {
        getInstance().setDiscountRules(customerDiscountService.findDiscountRulesByCustomer(customerService.findByEntity(customer)));
        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (customerDiscountAmount != null) {
            totalDiscount = totalDiscount.add(customerDiscountAmount);
        }
        if (customerDiscountPercentage != null) {
            totalDiscount = totalDiscount.add(getInstance().getTotalAmount().multiply(customerDiscountPercentage).
                    divide(new BigDecimal(100.0)));
        }
        if (getInstance().getTotalAmount().doubleValue() >= totalDiscount.doubleValue()) {
            getInstance().setTotalDiscount(totalDiscount);
        } else {
            getInstance().setTotalDiscount(BigDecimal.ZERO);
        }
    }

    public InvoiceDetail createInvoiceDetail(Product product, Integer quantity) {
        List<ProductDiscountRule> rules = productService.findDiscountRules(product);
        BigDecimal discountPercentage = productService.getDiscountPercentage(product);
        BigDecimal discount = productService.getDiscount(product);
        return new InvoiceDetail(product, quantity, rules, discountPercentage, discount);
    }

    public Long getInvoiceDetailId() {
        return invoiceDetailId;
    }

    public void setInvoiceDetailId(Long invoiceDetailId) {
        this.invoiceDetailId = invoiceDetailId;
    }

    public boolean isPrintHeader() {
        return printHeader;
    }

    public boolean isFewInvoiceNumber() {
        boolean res = false;
        //System.out.println("** fewInvoice " + (getInstance().getTaxRule().getEndInvoiceNumber() - getInstance().getTaxRule().getCurrentInvoiceNumber()));
        if ((getInstance().getTaxRule().getEndInvoiceNumber() - getInstance().getTaxRule().getCurrentInvoiceNumber()) <= 10) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Invoice.fewInvoiceNumber");
            res = true;
        }
        return res;
    }

    @Destroy
    public void safeRemove() {
        invoiceService.remove(getInstance());
    }
}

