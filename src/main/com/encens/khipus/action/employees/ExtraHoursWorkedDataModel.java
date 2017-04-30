package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ExtraHoursWorked;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Extra hours worked data model
 *
 * @author
 * @version 2.26
 */
@Name("extraHoursWorkedDataModel")
@Scope(ScopeType.PAGE)
public class ExtraHoursWorkedDataModel extends QueryDataModel<Long, ExtraHoursWorked> {

    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "extraHoursWorked.payrollGenerationCycle = #{payrollGenerationCycleAction.instance}"
    };

    @Override
    public String getEjbql() {
        return "select extraHoursWorked from ExtraHoursWorked extraHoursWorked";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
