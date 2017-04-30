package com.encens.khipus.action.finances;

import com.encens.khipus.action.AppIdentity;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.MalformedEntityQueryCompoundConditionException;
import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.RotatoryFund;
import com.encens.khipus.model.finances.RotatoryFundState;
import com.encens.khipus.model.finances.RotatoryFundType;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryCompoundCondition;
import com.encens.khipus.util.query.EntityQuerySingleCondition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for RotatoryFund
 *
 * @author
 * @version 2.22
 */

@Name("rotatoryFundDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ROTATORYFUND','VIEW') or s:hasPermission('OTHERRECEIVABLES','VIEW')}")
public class RotatoryFundDataModel extends QueryDataModel<Long, RotatoryFund> {
    private Employee employee;

    private CashAccount cashAccount;

    private String idNumber;

    private String firstName;

    private String maidenName;

    private String lastName;

    private RotatoryFundType rotatoryFundType;

    @In
    private GenericService genericService;
    @In(value = "org.jboss.seam.security.identity")
    private AppIdentity appIdentity;
    @Logger
    protected Log log;

    private static final String[] RESTRICTIONS =
            {
                    "employee= #{rotatoryFundDataModel.employee}",
                    "employee.idNumber like concat(#{rotatoryFundDataModel.idNumber}, '%')",
                    "lower(employee.lastName) like concat('%', concat(lower(#{rotatoryFundDataModel.lastName}), '%'))",
                    "lower(employee.maidenName) like concat('%', concat(lower(#{rotatoryFundDataModel.maidenName}), '%'))",
                    "lower(employee.firstName) like concat('%', concat(lower(#{rotatoryFundDataModel.firstName}), '%'))",
                    "rotatoryFund.code=#{rotatoryFundDataModel.criteria.code}",
                    "documentType=#{rotatoryFundDataModel.criteria.documentType}",
                    "documentType.rotatoryFundType=#{rotatoryFundDataModel.rotatoryFundType}",
                    "rotatoryFund.state=#{rotatoryFundDataModel.criteria.state}",
                    "businessUnit=#{rotatoryFundDataModel.criteria.businessUnit}",
                    "lower(rotatoryFund.description) like concat('%', concat(lower(#{rotatoryFundDataModel.criteria.description}),'%'))",
                    "rotatoryFund.amount=#{rotatoryFundDataModel.criteria.amount}",
                    "cashAccount=#{rotatoryFundDataModel.cashAccount}"
            };
    private static final String VIEW_PERMISSION = "VIEW";

    @Create
    public void init() {
        sortProperty = "rotatoryFund.code";
        setSortAsc(false);
    }

    @Override
    public String getEjbql() {
        return "select rotatoryFund from RotatoryFund rotatoryFund" +
                " left join fetch rotatoryFund.employee employee" +
                " left join fetch rotatoryFund.businessUnit businessUnit" +
                " left join fetch rotatoryFund.cashAccount cashAccount" +
                " left join fetch rotatoryFund.documentType documentType";
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        entityQuery.setEjbql(addConditions(getEjbql()));
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String addConditions(String ejbql) {

        EntityQueryCompoundCondition entityQueryCompoundCondition = new EntityQueryCompoundCondition();
        String restrictionResult = "";
        try {

            boolean filterByOtherReceivables = (appIdentity.hasPermission("OTHERRECEIVABLES", VIEW_PERMISSION));

            if (filterByOtherReceivables) {
                entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("rotatoryFund.documentType.rotatoryFundType= #{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.RotatoryFundType','OTHER_RECEIVABLES')}"));
            }

            restrictionResult = entityQueryCompoundCondition.compile();
        } catch (MalformedEntityQueryCompoundConditionException e) {
            log.error("Malformed entity query compound condition exception, condition will not be added", e);
        }
        if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
            ejbql += " where ";
            ejbql += restrictionResult;
        }
        log.debug("ejbql: " + ejbql);
        return ejbql;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clearEmployee() {
        setEmployee(null);
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public void clearCashAccount() {
        setCashAccount(null);
    }

    public void assignCashAccount(CashAccount cashAccount) {
        try {
            cashAccount = genericService.findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        setCashAccount(cashAccount);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public RotatoryFundType getRotatoryFundType() {
        return rotatoryFundType;
    }

    public void setRotatoryFundType(RotatoryFundType rotatoryFundType) {
        this.rotatoryFundType = rotatoryFundType;
    }

    public void entryNotFoundLog() {
        log.debug("entity was removed by another user");
    }

    @Override
    public void clear() {
        setEmployee(null);
        setCashAccount(null);
        super.clear();
        update();
        search();
    }

    public void setReceivableFundTypeAndApprovedState() {
        setRotatoryFundType(RotatoryFundType.RECEIVABLE_FUND);
        getCriteria().setState(RotatoryFundState.APR);
    }
}