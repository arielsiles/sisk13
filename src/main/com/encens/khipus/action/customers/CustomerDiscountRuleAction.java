package com.encens.khipus.action.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.CustomerDiscountRule;
import com.encens.khipus.model.customers.DiscountRuleState;
import com.encens.khipus.service.customers.CustomerDiscountService;
import com.encens.khipus.service.customers.CustomerService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.security.Restrict;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actions for Customer Discount Rules
 *
 * @author:
 */

@Name("customerDiscountRuleAction")
@Scope(ScopeType.CONVERSATION)
public class CustomerDiscountRuleAction extends GenericAction<CustomerDiscountRule> {

    private boolean selectAllOption;
    private int searchOption = 1;
    private String idNumber;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String organizationName;

    @In(value = "#{entityManager}")
    public EntityManager em;

    @In(required = false)
    private User currentUser;

    @In
    private CustomerDiscountService customerDiscountService;
    @In
    private CustomerService customerService;

    private Map<Entity, Boolean> selectedCustomers = new HashMap<Entity, Boolean>();

    @Factory(value = "discountRuleState")
    public DiscountRuleState[] getDiscountRuleState() {
        return DiscountRuleState.values();
    }

    @DataModel
    public List<Entity> getCustomerList() {
        return getQueryResult();
    }

    @Factory(value = "customerDiscountRule", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CUSTOMERDISCOUNTRULE','VIEW')}")
    public CustomerDiscountRule initCustomerDiscountRule() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CUSTOMERDISCOUNTRULE','CREATE')}")
    public String create() {
        try {
            getInstance().setUser(currentUser);
            genericService.create(getInstance());
            addCreatedMessage();
            return select(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    public String updateRule() {
        getInstance().setUser(currentUser);
        return update();
    }

    @SuppressWarnings({"unchecked"})
    public List<Entity> getQueryResult() {
        //TODO This code part must be fixed by the properly implementation, should be used the EntityQuery provided by Seam
        return new ArrayList<Entity>();
    }

    private String getSearchPattern(String criteria) {
        return criteria == null ? "%" : criteria.toLowerCase().replace('*', '%') + '%';
    }

    public void selectCustomer(Entity customer) {
        System.out.println("***********" + selectedCustomers.get(customer));
        if (selectedCustomers.get(customer)) {
            customerDiscountService.newDiscount(getInstance(), customerService.findByEntity(customer));
        } else {
            selectAllOption = false;
            customerDiscountService.deleteDiscount(getInstance(), customerService.findByEntity(customer));
        }
        update();
    }

    public Map<Entity, Boolean> getSelectedCustomers() {
        if (selectedCustomers.isEmpty()) {
            for (Entity customer : getQueryResult()) {
                if (customerDiscountService.findDiscountByRule(customerService.findByEntity(customer), getInstance()) != null) {
                    selectedCustomers.put(customer, Boolean.TRUE);
                } else {
                    selectedCustomers.put(customer, Boolean.FALSE);
                }
            }
        }
        return selectedCustomers;
    }

    public void setSelectedCustomers(Map<Entity, Boolean> selectedCustomers) {
        this.selectedCustomers = selectedCustomers;
    }

    public int getSearchOption() {
        return searchOption;
    }

    public void setSearchOption(int searchOption) {
        selectAllOption = false;
        selectedCustomers = new HashMap<Entity, Boolean>();
        this.searchOption = searchOption;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isSearchPerson() {
        return searchOption == 1;
    }

    public boolean isSelectAllOption() {
        return selectAllOption;
    }

    public void setSelectAllOption(boolean selectAllOption) {
        this.selectAllOption = selectAllOption;
    }

    public void selectAllAction() {
        if (selectAllOption) {
            for (Entity customer : getQueryResult()) {
                selectedCustomers.put(customer, Boolean.TRUE);
            }
        }
    }

    public void assignCustomers() {
        for (Entity customer : getQueryResult()) {
            if (selectedCustomers.get(customer)) {
                customerDiscountService.newDiscount(getInstance(), customerService.findByEntity(customer));
            }
        }
        update();
    }

    public void cancelAction() {
        selectAllOption = false;
        selectedCustomers = new HashMap<Entity, Boolean>();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CUSTOMERDISCOUNTRULE','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            genericService.update(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(genericService.findById(getEntityClass(), getId(getInstance())));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }
}
