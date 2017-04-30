package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.AccountingStateType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author:
 */
@Name("accountingStateTypeDataModel")
@Scope(ScopeType.PAGE)
public class AccountingStateTypeDataModel extends QueryDataModel<Long, AccountingStateType> {

    private static final String[] RESTRICTIONS =
            {"lower(accountingStateType.name) like concat('%', concat(lower(#{accountingStateTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "accountingStateType.name";
    }

    @Override
    public String getEjbql() {
        return "select accountingStateType from AccountingStateType accountingStateType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}



