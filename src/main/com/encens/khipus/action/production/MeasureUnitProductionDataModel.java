package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.MeasureUnitProduction;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("measureUnitProductionDataModel")
@Scope(ScopeType.PAGE)
public class MeasureUnitProductionDataModel extends QueryDataModel<Long, MeasureUnitProduction> {

    private static final String[] RESTRICTIONS = {
            "upper(measureUnit.name) like concat(concat('%',upper(#{measureUnitProductionDataModel.criteria.name})), '%')",
            "upper(measureUnit.description) like concat(concat('%',upper(#{measureUnitProductionDataModel.criteria.description})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "measureUnitProduction.name";
    }

    @Override
    public String getEjbql() {
        String query =  "select measureUnitProduction " +
                        "from MeasureUnitProduction measureUnitProduction ";
        return query;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
