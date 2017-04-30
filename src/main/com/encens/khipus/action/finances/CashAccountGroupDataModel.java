package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashAccountGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * CashAccountGroupDataModel
 *
 * @author
 * @version 2.27
 */
@Name("cashAccountGroupDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CASHACCOUNTGROUP','VIEW')}")
public class CashAccountGroupDataModel extends QueryDataModel<Long, CashAccountGroup> {
    private static final String[] RESTRICTIONS = {
            "lower(cashAccountGroup.name) like concat('%', concat(lower(#{cashAccountGroupDataModel.criteria.name}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "cashAccountGroup.name";
    }

    @Override
    public String getEjbql() {
        return "select cashAccountGroup from CashAccountGroup cashAccountGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
