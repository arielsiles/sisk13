package com.encens.khipus.action.integration.rrhhmark;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.Mark;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * MarkDataModel
 *
 * @author
 * @version 1.4
 */
@Name("markEmployeeDataModel")
@Scope(ScopeType.PAGE)
public class MarkEmployeeDataModel extends QueryDataModel<Long, Mark> {

    private Employee employee;

    private static final String[] RESTRICTIONS =
            {"mark.marRefCard = #{markEmployeeDataModel.employee.markCode}",
                    "mark.marDate >= #{markEmployeeDataModel.criteria.startMarDate}",
                    "mark.marDate <= #{markEmployeeDataModel.criteria.endMarDate}"};

    @Create
    public void init() {
        sortProperty = "mark.marDate, mark.marTime";
    }

    @Override
    public String getEjbql() {
        return "select new com.encens.khipus.model.employees.Mark(mark,horaryBandState.type) from Mark mark " +
                " left join mark.markStateList markState" +
                " left join markState.markStateHoraryBandStateList markStateHoraryBandState" +
                " left join markStateHoraryBandState.horaryBandState horaryBandState" +
                " where #{true}=#{not empty markEmployeeDataModel.employee.markCode}";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
