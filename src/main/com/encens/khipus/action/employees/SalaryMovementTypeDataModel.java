package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.SalaryMovementType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("salaryMovementTypeDataModel")
@Scope(ScopeType.PAGE)
public class SalaryMovementTypeDataModel extends QueryDataModel<Long, SalaryMovementType> {
    private static final String[] RESTRICTIONS = {
            "lower(salaryMovementType.name) like concat('%', concat(lower(#{salaryMovementTypeDataModel.criteria.name}), '%'))",
            "salaryMovementType.movementType= #{salaryMovementTypeDataModel.criteria.movementType}"
    };

    @Create
    public void init() {
        sortProperty = "salaryMovementType.name";
    }

    @Override
    public String getEjbql() {
        return "select salaryMovementType from SalaryMovementType salaryMovementType" +
                " left join fetch salaryMovementType.cashAccount cashAccount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
