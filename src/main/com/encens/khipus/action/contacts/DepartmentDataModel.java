package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Department;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 */
@Name("departmentDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DEPARTMENT','VIEW')}")
public class DepartmentDataModel extends QueryDataModel<Long, Department> {

    private static final String[] RESTRICTIONS =
            {"lower(department.name) like concat('%', concat(lower(#{departmentDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "department.name";
    }

    @Override
    public String getEjbql() {
        return "select department from Department department";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
