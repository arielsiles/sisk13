package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CashBox;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Cash box
 *
 * @author:
 */

@Name("cashBoxDataModel")
@Scope(ScopeType.PAGE)
public class CashBoxDataModel extends QueryDataModel<Long, CashBox> {

    private static final String[] RESTRICTIONS =
            {"lower(cashBox.description) like concat('%', concat(lower(#{cashBoxDataModel.criteria.description}), '%'))"};

    @Create
    public void init() {
        sortProperty = "cashBox.description";
    }

    @Override
    public String getEjbql() {
        return "select cashBox from CashBox cashBox";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
