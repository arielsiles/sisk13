package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Vacation;
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
@Name("vacationDataModel")
@Scope(ScopeType.PAGE)
public class VacationDataModel extends QueryDataModel<Long, Vacation> {

    private static final String[] RESTRICTIONS = {
            "vacationPlanning = #{vacationPlanning}"
    };

    @Create
    public void init() {
        sortProperty = "vacation.initDate";
    }

    @Override
    public String getEjbql() {
        return "select vacation from Vacation vacation" +
                " left join vacation.vacationGestion vacationGestion" +
                " left join vacationGestion.vacationPlanning vacationPlanning";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
