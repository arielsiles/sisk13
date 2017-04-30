package com.encens.khipus.action.customers;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.customers.InvoiceDetail;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.customers.InvoiceService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Invoice print action
 *
 * @author
 * @version $Id: InvoicePrintAction.java 2008-9-10 14:03:36 $
 */

@Name("invoicePrintAction")
@Scope(ScopeType.EVENT)
public class InvoicePrintAction {

    @In
    private InvoiceService invoiceService;

    @In
    private BusinessUnitService businessUnitService;

    @In(required = false)
    private User currentUser;

    private Invoice invoice;

    private Long invoiceId;

    private InvoiceDetail invoiceDetail;

    private Long invoiceDetailId;

    public Invoice getInvoice() {
        if (invoice == null) {
            invoice = invoiceService.findById(invoiceId);
        }
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public InvoiceDetail getInvoiceDetail() {
        if (invoiceDetailId != null) {
            invoiceDetail = invoiceService.findDetailById(invoiceDetailId);
        }
        return invoiceDetail;
    }

    public void setInvoiceDetail(InvoiceDetail invoiceDetail) {
        this.invoiceDetail = invoiceDetail;
    }

    public Long getInvoiceDetailId() {
        return invoiceDetailId;
    }

    public void setInvoiceDetailId(Long invoiceDetailId) {
        this.invoiceDetailId = invoiceDetailId;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnitService.findByUser(currentUser);
    }

}