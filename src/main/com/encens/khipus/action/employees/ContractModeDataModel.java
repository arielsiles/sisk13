package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ContractMode;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for ContractMode
 *
 * @author
 */

@Name("contractModeDataModel")
@Scope(ScopeType.PAGE)
public class ContractModeDataModel extends QueryDataModel<Long, ContractMode> {
    private static final String[] RESTRICTIONS =
            {"lower(contractMode.name) like concat('%', concat(lower(#{contractModeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "contractMode.name";
    }

    @Override
    public String getEjbql() {
        return "select contractMode from ContractMode contractMode";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}