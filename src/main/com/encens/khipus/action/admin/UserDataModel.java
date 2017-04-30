package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.Employee;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 1.0
 */
@Name("userDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('USER','VIEW')}")
public class UserDataModel extends QueryDataModel<Long, User> {

    private Employee employee;

    private static final String[] RESTRICTIONS = {
            "lower(user.username) like concat('%', concat(lower(#{userDataModel.criteria.username}), '%'))",
            "user.employee.idNumber like concat(#{userDataModel.employee.idNumber}, '%')",
            "lower(user.employee.firstName) like concat('%', concat(lower(#{userDataModel.employee.firstName}), '%'))",
            "lower(user.employee.lastName) like concat('%', concat(lower(#{userDataModel.employee.lastName}), '%'))",
            "lower(user.employee.maidenName) like concat('%', concat(lower(#{userDataModel.employee.maidenName}), '%'))"};

    @Override
    public String getEjbql() {
        return "select user from User user";
    }

    @Create
    public void init() {
        sortProperty = "user.username";
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public User createInstance() {
        setEmployee(new Employee());
        return super.createInstance();
    }

    @Override
    public void clear() {
        setEmployee(new Employee());
        super.clear();
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
