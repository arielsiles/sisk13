package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.finances.MeasureUnitPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * MeasureUnitDataModel
 *
 * @author
 * @version 2.0
 */
@Name("measureUnitDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('MEASUREUNIT','VIEW')}")
public class MeasureUnitDataModel extends QueryDataModel<MeasureUnitPk, MeasureUnit> {

    private static final String[] RESTRICTIONS = {
            "lower(measureUnit.measureUnitCode) like concat(lower(#{measureUnitDataModel.criteria.measureUnitCode}), '%')",
            "lower(measureUnit.name) like concat('%', concat(lower(#{measureUnitDataModel.criteria.name}), '%'))",
            "lower(measureUnit.description) like concat('%', concat(lower(#{measureUnitDataModel.criteria.description}), '%'))"};

    @Create
    public void init() {
        sortProperty = "measureUnit.measureUnitCode";
    }

    @Override
    public String getEjbql() {
        return "select measureUnit from MeasureUnit measureUnit";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
