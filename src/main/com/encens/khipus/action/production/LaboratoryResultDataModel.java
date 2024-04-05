package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.LaboratoryResult;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("laboratoryResultDataModel")
@Scope(ScopeType.PAGE)
public class LaboratoryResultDataModel extends QueryDataModel<Long, LaboratoryResult> {

    private static final String[] RESTRICTIONS = {
            "laboratoryResult.zoneInspection = #{zoneInspection}"
    };

    public void init() {
        sortProperty = "laboratoryResult.date";
    }

    @Override
    public String getEjbql() {
        return "select laboratoryResult from LaboratoryResult laboratoryResult";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}