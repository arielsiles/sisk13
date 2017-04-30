package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashBoxType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Cash box type
 *
 * @author:
 */

@Name("cashBoxTypeDataModel")
@Scope(ScopeType.PAGE)
public class CashBoxTypeDataModel extends QueryDataModel<Long, CashBoxType> {

    private static final String[] RESTRICTIONS =
            {"lower(cashBoxType.name) like concat('%', concat(lower(#{cashBoxTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "cashBoxType.name";
    }

    @Override
    public String getEjbql() {
        return "select cashBoxType from CashBoxType cashBoxType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
