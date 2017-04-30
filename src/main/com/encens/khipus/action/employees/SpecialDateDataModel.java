package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.SpecialDate;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Charge data model
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("specialDateDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SPECIALDATE','VIEW')}")
public class SpecialDateDataModel extends QueryDataModel<Long, SpecialDate> {

    private static final String[] RESTRICTIONS = {
            "lower(specialDate.title) like concat('%', concat(lower(#{specialDateDataModel.criteria.title}), '%'))",
            "specialDate.specialDateTarget = #{specialDateDataModel.criteria.specialDateTarget}",
            "specialDate.initPeriod >= #{specialDateDataModel.criteria.initPeriod}",
            "specialDate.endPeriod <= #{specialDateDataModel.criteria.endPeriod}",
            "specialDate.credit = #{specialDateDataModel.criteria.credit}",
            "employee.idNumber like concat(#{specialDateDataModel.idNumber}, '%')",
            "lower(employee.lastName) like concat('%', concat(lower(#{specialDateDataModel.lastName}), '%'))",
            "lower(employee.maidenName) like concat('%', concat(lower(#{specialDateDataModel.maidenName}), '%'))",
            "lower(employee.firstName) like concat('%', concat(lower(#{specialDateDataModel.firstName}), '%'))",
            "lower(organizationalUnit.name) like concat('%', concat(lower(#{specialDateDataModel.organizationalUnitName}), '%'))",
            "businessUnit = #{specialDateDataModel.businessUnit}"};

    private String idNumber;
    private String lastName;
    private String maidenName;
    private String firstName;
    private String organizationalUnitName;
    private BusinessUnit businessUnit;

    @Override
    public String getEjbql() {
        return "select distinct specialDate from SpecialDate specialDate" +
                " left join specialDate.employee employee" +
                " left join specialDate.organizationalUnit organizationalUnit" +
                " left join specialDate.businessUnit businessUnit";
    }

    @Create
    public void defaultSort() {
        sortProperty = "specialDate.title";
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getOrganizationalUnitName() {
        return organizationalUnitName;
    }

    public void setOrganizationalUnitName(String organizationalUnitName) {
        this.organizationalUnitName = organizationalUnitName;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    @Override
    public void clear() {
        super.clear();
        setIdNumber(null);
        setLastName(null);
        setMaidenName(null);
        setFirstName(null);
        setOrganizationalUnitName(null);
        setBusinessUnit(null);
    }
}