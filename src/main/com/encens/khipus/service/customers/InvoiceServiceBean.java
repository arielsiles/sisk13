package com.encens.khipus.service.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.contacts.Organization;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Customer;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.customers.InvoiceDetail;
import com.encens.khipus.model.finances.CashBoxTransaction;
import com.encens.khipus.model.finances.TaxRule;
import com.encens.khipus.service.finances.CashBoxTransactionService;
import com.encens.khipus.service.finances.UserCashBoxService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Invoice service implementation class
 *
 * @author
 * @version $Id: InvoiceServiceBean.java 2008-9-11 15:57:13 $
 */
@Stateless
@Name("invoiceService")
@AutoCreate
public class InvoiceServiceBean implements InvoiceService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In(required = false)
    private User currentUser;

    @In
    private UserCashBoxService userCashBoxService;

    @In
    private CashBoxTransactionService cashBoxTransactionService;

    @Logger
    protected Log log;

    public void create(Invoice invoice, Customer customer) throws EntryDuplicatedException {
        try {

            /*if (customer.getFirstPurchase() == null)
                customer.setFirstPurchase(new Date());
            if (customer.getTotalPurchasedAmount() != null)
                customer.setTotalPurchasedAmount(customer.
                        getTotalPurchasedAmount().add(invoice.getTotalAmount()));
            else
                customer.setTotalPurchasedAmount(invoice.getTotalAmount());

            if (customer.getTotalPurchasedProducts() != null)
                customer.setTotalPurchasedProducts(customer.
                        getTotalPurchasedProducts() + invoice.getTotalProductsQuantity());
            else
                customer.setTotalPurchasedProducts(invoice.getTotalProductsQuantity());

            customer.setLastPurchase(new Date());*/

            invoice.setCustomer(customer);
            //invoice.setNumber();

            //setting the customer information on the entity
            Entity customerEntity = customer.getEntity();
            if (customerEntity instanceof Person) {
                Person personCustomer = (Person) customerEntity;
                invoice.setFirstName(personCustomer.getFirstName());
                invoice.setLastName(personCustomer.getLastName());
                invoice.setMaidenName(personCustomer.getMaidenName());

            } else {
                Organization organizationCustomer = (Organization) customerEntity;
                invoice.setOrganizationName(organizationCustomer.getName());
            }

            //TODO: this must be got from the discount rules..
            /*if (invoice.getTotalDiscount() == null)
                invoice.setTotalDiscount(BigDecimal.valueOf(0));*/

            //TODO: this must be generated according tax configuration...
            invoice.setNumber(String.valueOf(System.currentTimeMillis()));

            /******************************************/
            invoice.setTotalAmount(new BigDecimal(0.0));
            invoice.setTotalDiscount(new BigDecimal(0.0));
            invoice.setCashBox(userCashBoxService.findByUser(currentUser));
            TaxRule taxRule = findTaxRuleByUserId(currentUser.getId());
            if (taxRule.getCurrentInvoiceNumber() == taxRule.getEndInvoiceNumber()) {
                invoice.setNumber("000000");
            } else {
                taxRule.setCurrentInvoiceNumber(taxRule.getCurrentInvoiceNumber() + 1);
                invoice.setNumber("" + taxRule.getCurrentInvoiceNumber());
            }
            //invoice.setNumber(""+taxRule.getCurrentInvoiceNumber());
            invoice.setTaxDocumentNumber(taxRule.getOrderNumber());
            invoice.setTaxDocumentType(taxRule.getSerialNumber());
            invoice.setTaxRule(taxRule);

            em.persist(invoice);
            em.merge(customer);
            em.flush();
            System.out.println("**** FACTURA CREADA ");

        } catch (EntityExistsException e) {
            throw new EntryDuplicatedException();
        }
    }

    public void update(Invoice invoice) {
        em.merge(invoice);
        em.flush();
    }

    public void save(Invoice invoice, Customer customer) {

        if (customer.getFirstPurchase() == null) {
            customer.setFirstPurchase(new Date());
        }
        if (customer.getTotalPurchasedAmount() != null) {
            customer.setTotalPurchasedAmount(customer.
                    getTotalPurchasedAmount().add(invoice.getTotalAmount()));
        } else {
            customer.setTotalPurchasedAmount(invoice.getTotalAmount());
        }

        if (customer.getTotalPurchasedProducts() != null) {
            customer.setTotalPurchasedProducts(customer.
                    getTotalPurchasedProducts() + invoice.getTotalProductsQuantity());
        } else {
            customer.setTotalPurchasedProducts(invoice.getTotalProductsQuantity());
        }

        customer.setLastPurchase(new Date());

        CashBoxTransaction cashBoxTransaction = cashBoxTransactionService.findByCashBoxUser(currentUser);
        cashBoxTransaction.setTotalAmount(cashBoxTransaction.getTotalAmount().add(invoice.getTotalAmount()));

        em.merge(cashBoxTransaction);
        em.merge(invoice);
        em.merge(customer);
        em.flush();
    }

    public void remove(Invoice invoice) {
        em.remove(invoice);
        em.flush();
    }

    public Invoice findById(Long id) {
        return em.find(Invoice.class, id);
    }

    public InvoiceDetail findDetailById(Long id) {
        try {
            return (InvoiceDetail) em.createQuery("select id from InvoiceDetail id where id.id =:id")
                    .setParameter("id", id).getSingleResult();
        } catch (NoResultException nre) {
            log.debug(nre);
            return null;
        }
    }

    public TaxRule findTaxRuleByUserId(Long id) {
        try {
            return (TaxRule) em.createQuery("select tr from TaxRule tr where tr.user.id=:id")
                    .setParameter("id", id).getSingleResult();
        } catch (NoResultException nre) {
            log.debug(nre);
            return null;
        }

    }
}
