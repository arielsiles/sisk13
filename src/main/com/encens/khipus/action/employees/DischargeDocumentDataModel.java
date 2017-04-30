package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Name("dischargeDocumentDataModel")
@Scope(ScopeType.PAGE)
@Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','VIEW')}")
public class DischargeDocumentDataModel extends QueryDataModel<Long, DischargeDocument> {

    private static final String[] RESTRICTIONS = {
            "lower(document.name) like concat('%', concat(lower(#{dischargeDocumentDataModel.criteria.name}), '%'))",
            "lower(document.number) like concat('%', concat(lower(#{dischargeDocumentDataModel.criteria.number}), '%'))",
            "document.jobContract.contract.employee = #{dischargeDocumentDataModel.employee}",
            "document.gestionPayroll = #{dischargeDocumentDataModel.gestionPayroll}"
    };

    private Employee employee;

    private GestionPayroll gestionPayroll;

    @Create
    public void init() {
        sortProperty = "document.date";
    }

    @Override
    public String getEjbql() {
        return "select document from DischargeDocument document";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public void assignGestionPayroll(GestionPayroll gestionPayroll) {
        setGestionPayroll(gestionPayroll);
    }

    public void cleanGestionPayroll() {
        setGestionPayroll(null);
    }

    public void assignEmployee(Employee employee) {
        setEmployee(employee);
    }

    public void cleanEmployee() {
        setEmployee(null);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    @Override
    public void clear() {
        cleanEmployee();
        cleanGestionPayroll();
        super.clear();
    }
}
