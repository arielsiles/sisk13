package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.Formulation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Name("formulationDataModel")
@Scope(ScopeType.PAGE)
public class FormulationDataModel extends QueryDataModel<Long, Formulation> {

    private static final String[] RESTRICTIONS =
            {"lower(formulation.name) like concat('%', concat(lower(#{formulationDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        //sortProperty = "";
    }

    @Override
    public String getEjbql() {
        return "select formulation from Formulation formulation";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}