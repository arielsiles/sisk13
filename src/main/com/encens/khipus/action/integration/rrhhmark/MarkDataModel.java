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
@Name("markDataModel")
@Scope(ScopeType.PAGE)
public class MarkDataModel extends QueryDataModel<Long, Mark> {

    private static final String[] RESTRICTIONS =
            {"mark.marRefCard = #{markDataModel.criteria.marRefCard}",
                    "mark.marDate >= #{markDataModel.criteria.startMarDate}",
                    "mark.marDate <= #{markDataModel.criteria.endMarDate}"};

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
                " where #{true}=#{not empty markDataModel.criteria.marRefCard}";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
