package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Charge;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Charge data model
 *
 * @author
 * @version 2.5
 */
@Name("chargeDataModel")
@Scope(ScopeType.PAGE)
public class ChargeDataModel extends QueryDataModel<Long, Charge> {

    private static final String[] RESTRICTIONS =
            {"charge.code  = #{chargeDataModel.criteria.code}"
                    , "lower(charge.name) like concat('%',concat(lower(#{chargeDataModel.criteria.name}), '%'))"};

    @Override
    public String getEjbql() {
        return "select charge from Charge charge";
    }

    @Create
    public void defaultSort() {
        sortProperty = "charge.name";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}