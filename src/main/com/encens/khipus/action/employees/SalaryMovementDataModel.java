package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.SalaryMovement;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for SalaryMovementDataModel
 *
 * @author
 */

@Name("salaryMovementDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALARYMOVEMENT','VIEW')}")
public class SalaryMovementDataModel extends QueryDataModel<Long, SalaryMovement> {
    private static final String[] RESTRICTIONS = {
            "employee.idNumber like concat(#{salaryMovementDataModel.idNumber}, '%')",
            "salaryMovement.amount = #{salaryMovementDataModel.criteria.amount}",
            "salaryMovement.date = #{salaryMovementDataModel.criteria.date}",
            "employee = #{salaryMovementDataModel.criteria.employee}",
            "salaryMovement.salaryMovementType = #{salaryMovementDataModel.criteria.salaryMovementType}",
            "gestionPayroll = #{salaryMovementDataModel.criteria.gestionPayroll}"
    };

    private SalaryMovement criteria;

    private String idNumber;

    @Create
    public void init() {
        sortProperty = "salaryMovement.date";
        criteria = new SalaryMovement();
        criteria.setDate(null);
    }

    @Override
    public String getEjbql() {
        return "select salaryMovement from SalaryMovement salaryMovement" +
                " left join fetch salaryMovement.salaryMovementType salaryMovementType" +
                " left join fetch salaryMovement.employee employee" +
                " left join fetch salaryMovement.currency currency" +
                " left join fetch salaryMovement.gestionPayroll gestionPayroll";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    @Override
    public SalaryMovement getCriteria() {
        return this.criteria;
    }

    @Override
    public void setCriteria(SalaryMovement criteria) {
        this.criteria = criteria;
    }

    public void clearEmployee() {
        getCriteria().setEmployee(null);
    }

    public void assignGestionPayroll(GestionPayroll gestionPayroll) {
        getCriteria().setGestionPayroll(gestionPayroll);
    }

    public void cleanGestionPayroll() {
        getCriteria().setGestionPayroll(null);
    }
}