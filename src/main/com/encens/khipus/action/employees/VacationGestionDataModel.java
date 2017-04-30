package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.VacationGestion;
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
@Name("vacationGestionDataModel")
@Scope(ScopeType.PAGE)
public class VacationGestionDataModel extends QueryDataModel<Long, VacationGestion> {

    private static final String[] RESTRICTIONS = {
            "vacationPlanning = #{vacationPlanning}"
    };

    @Create
    public void init() {
        sortProperty = "vacationGestion.gestion";
    }

    @Override
    public String getEjbql() {
        return "select vacationGestion from VacationGestion vacationGestion" +
                " left join vacationGestion.vacationPlanning vacationPlanning";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
