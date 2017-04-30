package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashBoxStatusType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Cash box status type
 *
 * @author:
 */
@Name("cashBoxStatusTypeDataModel")
@Scope(ScopeType.PAGE)
public class CashBoxStatusTypeDataModel extends QueryDataModel<Long, CashBoxStatusType> {

    private static final String[] RESTRICTIONS =
            {"lower(cashBoxStatusType.name) like concat('%', concat(lower(#{cashBoxStatusTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "cashBoxStatusType.name";
    }

    @Override
    public String getEjbql() {
        return "select cashBoxStatusType from CashBoxStatusType cashBoxStatusType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
