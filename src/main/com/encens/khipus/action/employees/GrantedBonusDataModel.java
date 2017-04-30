package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.GrantedBonus;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.26
 */

@Name("grantedBonusDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GRANTEDBONUS','VIEW')}")
public class GrantedBonusDataModel extends QueryDataModel<Long, GrantedBonus> {
    private static final String[] RESTRICTIONS = {
            /* since this is a nested conversation that allows to view both entities in the same form
            * we select the instance which evolves the other*/
            "grantedBonus.payrollGenerationCycle = #{payrollGenerationCycle}"
    };

    @Create
    public void init() {
        sortProperty = "grantedBonus.id";
    }

    @Override
    public String getEjbql() {
        return "select grantedBonus from GrantedBonus grantedBonus";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}