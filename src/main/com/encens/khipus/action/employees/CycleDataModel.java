package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Cycle;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Cycle
 *
 * @author
 * @version 2.9
 */

@Name("cycleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CYCLE','VIEW')}")
public class CycleDataModel extends QueryDataModel<Long, Cycle> {
    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "lower(cycle.name) like concat('%', concat(lower(#{cycleDataModel.criteria.name}), '%'))",
            "cycle.gestion.year = #{cycleDataModel.criteria.gestion.year}",
            "cycle.startDate >= #{cycleDataModel.criteria.startDate}",
            "cycle.endDate >= #{cycleDataModel.criteria.endDate}",
            "cycle.cycleType=#{cycleDataModel.criteria.cycleType}"
    };

    @Create
    public void init() {
        sortProperty = "cycle.name";
    }

    @Override
    public String getEjbql() {
        return "select cycle from Cycle cycle";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}