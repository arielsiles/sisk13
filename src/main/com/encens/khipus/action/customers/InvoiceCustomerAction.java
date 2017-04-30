package com.encens.khipus.action.customers;

import com.encens.khipus.action.AuthenticatorAction;
import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.contacts.EntityType;
import com.encens.khipus.model.contacts.Organization;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Customer;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.finances.CashBox;
import com.encens.khipus.model.finances.TaxRule;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.CustomerService;
import com.encens.khipus.service.customers.InvoiceService;
import com.encens.khipus.service.finances.CashBoxTransactionService;
import com.encens.khipus.service.finances.UserCashBoxService;
import com.encens.khipus.util.Hash;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;

/**
 * Action which allows to select a customer for an invoice
 *
 * @author
 * @version $Id: InvoiceCustomer.java 2008-9-10 14:28:56 $
 */
@Name("invoiceCustomerAction")
@Scope(ScopeType.CONVERSATION)
public class InvoiceCustomerAction implements Serializable {

    @Out(required = false, scope = ScopeType.CONVERSATION, value = "customer")
    private Customer customer;

    @Out(required = false, scope = ScopeType.CONVERSATION, value = "invoice")
    private Invoice invoice;

    @In
    protected FacesMessages facesMessages;

    @In
    private CustomerService customerService;

    @In
    private InvoiceService invoiceService;

    @In("#{authenticator}")
    private AuthenticatorAction authenticatorAction;

    @In
    private UserService userService;

    @In
    private CashBoxTransactionService cashBoxTransactionService;

    @In
    private UserCashBoxService userCashBoxService;

    @In(required = false)
    private User currentUser;

    private EntityType entityType = EntityType.PERSON;

    private String idNumber;

    private String customerNumber;

    private boolean customerFound = false;

    private String printPrevious;

    private Long idPreviousInvoice;

    private String responsibleUsername;
    private String responsiblePassword;

    @In
    private Company currentCompany;

    @Factory(value = "selectedCustomer", scope = ScopeType.STATELESS)
    public Customer initCustomer() {
        if (customer == null) {
            if (idNumber != null) {
                customer = customerService.findByIdNumber(idNumber);
                updateEntityType();
                if (customer == null) {
                    initNewInstances();
                }
            } else if (customerNumber != null) {
                customer = customerService.findByCustomerNumber(customerNumber);
                updateEntityType();
                if (customer == null) {
                    initNewInstances();
                }
            } else {
                initNewInstances();
            }
        }
        return customer;
    }

    @Factory(value = "entityTypeList")
    public EntityType[] getEntityTypeList() {
        return EntityType.values();
    }

    private void initNewInstances() {
        customer = new Customer();
        if (EntityType.PERSON.equals(entityType)) {
            customer.setEntity(new Person());
        } else if (EntityType.ORGANIZATION.equals(entityType)) {
            customer.setEntity(new Organization());
        }
    }

    public void idNumberChange(ValueChangeEvent value) {
        customer = customerService.findByIdNumber((String) value.getNewValue());
        if (customer == null) {
            Entity entity = customerService.findEntityByIdNumber((String) value.getNewValue());
            if (entity != null) {
                customer = new Customer();
                customer.setEntity(entity);
            }
        }
        if (customer != null) {
            customerNumber = customer.getNumber();
        } else {
            customerNumber = null;
        }
        updateEntityType();
    }

    public void customerNumberChange(ValueChangeEvent value) {
        if (idNumber == null) {
            customer = customerService.findByCustomerNumber((String) value.getNewValue());
            if (customer != null) {
                idNumber = customer.getEntity().getIdNumber();
            }
            updateEntityType();
        }
    }

    public void entityTypeChange(ValueChangeEvent value) {
        customer = null;
    }

    public void changeEntityType(EntityType entityType) {
        setEntityType(entityType);
        initNewInstances();
    }

    /**
     * TODO: if you enter an number and a person is found, but the entitytype was changed to organization, a clean
     * instance of organization must be created. But this assumption creates conflict with the above method...
     */
    private void updateEntityType() {
        System.out.println("updateEntityType was called...");
        if (customer != null) {
            customerFound = true;
            if (customer.getEntity() instanceof Person) {
                entityType = EntityType.PERSON;
            } else {
                entityType = EntityType.ORGANIZATION;
            }

        } else {
            customerFound = false;
            initNewInstances();
        }
    }

    public String select() {
        //update the Entity.idNUmber with the action number if it's null (which means it's gonna be created)
        if (invoiceService.findTaxRuleByUserId(currentUser.getId()) != null) {
            if (customerNumber != null) {
                customer.setNumber(customerNumber);
            }
            customer.getEntity().setIdNumber(idNumber);
            customerService.createOrUpdate(customer);
            printPrevious = "notPress";
            System.out.println("pressSelectString: " + printPrevious);
            createInvoice();
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "TaxRule.error.unassignedToUser", currentUser.getEmployee().getFullName());
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    public void cancel() {
    }

    public boolean isPerson() {
        return EntityType.PERSON.equals(entityType);
    }


    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public boolean isCustomerFound() {
        return customerFound;
    }

    public void createInvoice() {
        this.invoice = new Invoice();
        try {
            invoiceService.create(this.invoice, customer);
        } catch (EntryDuplicatedException e) {
        }
    }

    public void validateCustomerNumber(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String number = value.toString();
        if (customerService.numberExists(number, idNumber)) {
            FacesMessage msg = new FacesMessage("");
            throw new ValidatorException(msg);
        }
    }

    public String getPrintPrevious() {
        return printPrevious;
    }

    public void setPrintPrevious(String printPrevious) {
        this.printPrevious = printPrevious;
    }

    public void pressPrintPrevious() {
        printPrevious = "press";
    }

    public Long getIdPreviousInvoice() {
        return idPreviousInvoice;
    }

    public void setIdPreviousInvoice(Long idPreviousInvoice) {
        if (idPreviousInvoice == -1) {
            idPreviousInvoice = null;
        }
        this.idPreviousInvoice = idPreviousInvoice;
    }

    public boolean isRequiredAuthorizationToOpen() {
        CashBox cashBox = userCashBoxService.findByUser(currentUser);
        return cashBox != null && cashBox.getRequiredAuthorization() && !cashBoxTransactionService.cashBoxOpen(cashBox);
    }

    public boolean isRequiredAuthorizationToClose() {
        CashBox cashBox = userCashBoxService.findByUser(currentUser);
        return cashBox != null && cashBox.getRequiredAuthorization() && cashBoxTransactionService.cashBoxOpen(cashBox);
    }

    public String getResponsibleUsername() {
        return responsibleUsername;
    }

    public void setResponsibleUsername(String responsibleUsername) {
        this.responsibleUsername = responsibleUsername;
    }

    public String getResponsiblePassword() {
        return responsiblePassword;
    }

    public void setResponsiblePassword(String responsiblePassword) {
        this.responsiblePassword = responsiblePassword;
    }

    @End(beforeRedirect = true)
    public String validateResponsibleUser() {
        try {
            CashBox cashBox = userCashBoxService.findByUser(currentUser);
            if (cashBox != null) {
                User responsibleUser = cashBox.getUser();
                User user = userService.findByUsernameAndPasswordAndCompany(responsibleUsername, Hash.instance().hash(responsiblePassword), currentCompany.getLogin());
                if (null != user && user.equals(responsibleUser)) {
                    if (!cashBoxTransactionService.cashBoxOpen(cashBox)) {
                        cashBoxTransactionService.openCashBox(cashBox, currentUser);
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "InvoiceCustomer.cashBox.openCashBox", cashBox.getDescription());
                    } else {
                        cashBoxTransactionService.closeCashBox(cashBox);
                        authenticatorAction.logOut();
                    }
                    return Outcome.SUCCESS;
                } else {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "org.jboss.seam.loginFailed");
                    return Outcome.REDISPLAY;
                }
            } else {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "UserCashBox.error.unassignedCashBox", currentUser.getEmployee().getFullName());
                return Outcome.REDISPLAY;
            }
        } catch (EntryNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "org.jboss.seam.loginFailed");
            return Outcome.REDISPLAY;
        } catch (RuntimeException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "org.jboss.seam.loginFailed");
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = true)
    public String openCashBox() {
        try {
            CashBox cashBox = userCashBoxService.findByUser(currentUser);
            if (cashBox != null) {
                if (!cashBoxTransactionService.cashBoxClosedToday(cashBox)) {
                    if (!cashBoxTransactionService.cashBoxOpen(cashBox)) {
                        cashBoxTransactionService.openCashBox(cashBox, currentUser);
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "InvoiceCustomer.cashBox.openCashBox", cashBox.getDescription());
                    }
                    return Outcome.SUCCESS;
                } else {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "InvoiceCustomer.cashBox.closedCashBox", cashBox.getDescription());
                    return Outcome.REDISPLAY;
                }
            } else {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "UserCashBox.error.unassignedCashBox", currentUser.getEmployee().getFullName());
                return Outcome.REDISPLAY;
            }
        } catch (EntryDuplicatedException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = false)
    public String closeCashBox() {
        try {
            CashBox cashBox = userCashBoxService.findByUser(currentUser);
            if (cashBox != null) {
                cashBoxTransactionService.closeCashBox(cashBox);
                authenticatorAction.logOut();
                return Outcome.SUCCESS;
            } else {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "UserCashBox.error.unassignedCashBox", currentUser.getEmployee().getFullName());
                return Outcome.REDISPLAY;
            }
        } catch (ConcurrencyException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        }
    }

    public boolean isZeroInvoice() {
        boolean res = false;
        TaxRule tr = invoiceService.findTaxRuleByUserId(currentUser.getId());
        if (tr != null) {
            System.out.println("**zeroInvoice " + (tr.getEndInvoiceNumber() - tr.getCurrentInvoiceNumber()));
            if ((tr.getEndInvoiceNumber() - tr.getCurrentInvoiceNumber()) == 0) {
                res = true;
            }
        }
        return res;
    }

}
