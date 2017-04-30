package com.encens.khipus.action.employees;


import com.encens.khipus.exception.employees.MalformedEntityQueryCompoundConditionException;
import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.MovementType;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryCompoundCondition;
import com.encens.khipus.util.query.EntityQueryConditionOperator;
import com.encens.khipus.util.query.EntityQuerySingleCondition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("employeeContractDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('EMPLOYEE','VIEW')}")
public class EmployeeContractDataModel extends QueryDataModel<Long, Employee> {
    @Logger
    private Log log;

    @In
    private SalaryMovementAction salaryMovementAction;

    private static final String[] RESTRICTIONS = {
            "lower(employee.lastName) like concat('%', concat(lower(#{employeeContractDataModel.criteria.lastName}), '%'))",
            "lower(employee.maidenName) like concat('%', concat(lower(#{employeeContractDataModel.criteria.maidenName}), '%'))",
            "lower(employee.firstName) like concat('%', concat(lower(#{employeeContractDataModel.criteria.firstName}), '%'))",
            "employee.idNumber like concat(#{employeeContractDataModel.criteria.idNumber}, '%')"
    };

    @Override
    public String getEjbql() {
        return "select distinct employee from Employee employee " +
                "left join employee.contractList contract " +
                "left join contract.jobContractList jobContract " +
                "left join jobContract.job.jobCategory jobCategory ";
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        entityQuery.setEjbql(addConditions(getEjbql()));
    }

    public String addConditions(String ejbql) {

        EntityQueryCompoundCondition entityQueryCompoundCondition = new EntityQueryCompoundCondition();
        String restrictionResult = "";
        try {

            boolean filterByActiveForTaxPayroll = null != salaryMovementAction.getInstance()
                    && null != salaryMovementAction.getInstance().getSalaryMovementType()
                    && salaryMovementAction.getInstance().getSalaryMovementType().getMovementType().equals(MovementType.OTHER_INCOME);
            if (filterByActiveForTaxPayroll) {
                entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.activeForTaxPayrollGeneration= #{salaryMovementAction.activeForTaxPayrollGeneration}"));
            }

            EntityQueryCompoundCondition contractEntityQueryCompoundCondition = new EntityQueryCompoundCondition();

            EntityQueryCompoundCondition firstEntityQueryCompoundCondition = new EntityQueryCompoundCondition();
            firstEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.endDate is null AND contract.initDate <= #{salaryMovementAction.instance.gestionPayroll.endDate}"));
            contractEntityQueryCompoundCondition.addCondition(firstEntityQueryCompoundCondition);
            contractEntityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.OR);

            EntityQueryCompoundCondition secondEntityQueryCompoundCondition = new EntityQueryCompoundCondition();
            secondEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.initDate <= #{salaryMovementAction.instance.gestionPayroll.initDate} "));
            secondEntityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.AND);
            secondEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.endDate >= #{salaryMovementAction.instance.gestionPayroll.endDate} "));
            contractEntityQueryCompoundCondition.addCondition(secondEntityQueryCompoundCondition);
            contractEntityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.OR);

            EntityQueryCompoundCondition thirdEntityQueryCompoundCondition = new EntityQueryCompoundCondition();
            thirdEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.initDate >= #{salaryMovementAction.instance.gestionPayroll.initDate} "));
            thirdEntityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.AND);
            thirdEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.initDate <= #{salaryMovementAction.instance.gestionPayroll.endDate} "));
            contractEntityQueryCompoundCondition.addCondition(thirdEntityQueryCompoundCondition);
            contractEntityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.OR);

            EntityQueryCompoundCondition forthEntityQueryCompoundCondition = new EntityQueryCompoundCondition();
            forthEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.endDate >= #{salaryMovementAction.instance.gestionPayroll.initDate} "));
            forthEntityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.AND);
            forthEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("contract.endDate <= #{salaryMovementAction.instance.gestionPayroll.endDate} "));
            contractEntityQueryCompoundCondition.addCondition(forthEntityQueryCompoundCondition);
            if (filterByActiveForTaxPayroll) {
                entityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.AND);
            }
            entityQueryCompoundCondition.addCondition(contractEntityQueryCompoundCondition);
            entityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.AND);

            EntityQueryCompoundCondition fifthEntityQueryCompoundCondition = new EntityQueryCompoundCondition();
            fifthEntityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition("jobCategory = #{salaryMovementAction.instance.gestionPayroll.jobCategory}"));

            entityQueryCompoundCondition.addCondition(fifthEntityQueryCompoundCondition);

            restrictionResult = entityQueryCompoundCondition.compile();
        } catch (MalformedEntityQueryCompoundConditionException e) {
            log.error("Malformed entity query compound condition exception, condition will not be added", e);
        }
        if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
            ejbql += "where ";
            ejbql += restrictionResult;
        }
        log.debug("ejbql: " + ejbql);
        return ejbql;
    }


    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public SalaryMovementAction getSalaryMovementAction() {
        return salaryMovementAction;
    }

    public void setSalaryMovementAction(SalaryMovementAction salaryMovementAction) {
        this.salaryMovementAction = salaryMovementAction;
    }
}
