package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.CreditType;
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
@Name("creditTypeDataModel")
@Scope(ScopeType.PAGE)
public class CreditTypeDataModel extends QueryDataModel<Long, CreditType> {
    private static final String[] RESTRICTIONS = {
            "lower(creditType.name) like concat('%', concat(lower(#{creditTypeDataModel.criteria.name}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "creditType.name";
    }

    @Override
    public String getEjbql() {
        return "select creditType from CreditType creditType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
