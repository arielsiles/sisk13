package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CostCenterGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for CostCenterGroup
 *
 * @author
 * @version 1.2.1
 */

@Name("costCenterGroupDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('COSTCENTERGROUP','VIEW')}")
public class CostCenterGroupDataModel extends QueryDataModel<Long, CostCenterGroup> {
    private static final String[] RESTRICTIONS = {
            "lower(costCenterGroup.code) like concat(lower(#{costCenterGroupDataModel.criteria.code}),'%')",
            "lower(costCenterGroup.description) like concat('%', concat(lower(#{costCenterGroupDataModel.criteria.description}),'%'))"
    };

    @Create
    public void init() {
        sortProperty = "costCenterGroup.code";
    }

    @Override
    public String getEjbql() {
        return "select costCenterGroup from CostCenterGroup costCenterGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}