package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.xproduction.IndirectCosts;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Diego on 08/07/2014.
 */
@Name("indirectCostsDataModel")
@Scope(ScopeType.PAGE)
public class IndirectCostsDataModel extends QueryDataModel<Long,IndirectCosts> {

    private Gestion gestion;
    private Integer month;


    @Factory(value = "monthEnumIndirectCosts")
    public Month[] getMonthEnum() {
        return Month.values();
    }


    private static final String[] RESTRICTIONS = {
            " indirectCosts.periodIndirectCost.gestion = #{indirectCostsDataModel.gestion}",
            " indirectCosts.periodIndirectCost.month = #{indirectCostsDataModel.month}"
    };

    @Override
    public String getEjbql(){
        return " select indirectCosts from IndirectCosts indirectCosts " +
               " where indirectCosts.periodIndirectCost is not null";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
}
