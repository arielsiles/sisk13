package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.CostCenterPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for CostCenter
 *
 * @author
 * @version 1.2.1
 */

@Name("costCenterDataModel")
@Scope(ScopeType.PAGE)
public class CostCenterDataModel extends QueryDataModel<CostCenterPk, CostCenter> {
    private static final String[] RESTRICTIONS = {
            "lower(costCenter.code) like concat(lower(#{costCenterDataModel.criteria.code}), '%')",
            "lower(costCenter.description) like concat('%', concat(lower(#{costCenterDataModel.criteria.description}), '%'))"};

    private Boolean showAll;

    @Create
    public void init() {
        sortProperty = "costCenter.groupCode,costCenter.code";
    }

    @Override
    public String getEjbql() {
        return "select costCenter from CostCenter costCenter " +
                "left join fetch costCenter.costCenterGroup" +
                " where ((#{costCenterDataModel.showAll}=#{false} and costCenter.state=#{enumerationUtil.getEnumValue('com.encens.khipus.model.finances.CostCenterState', 'VIG')} and costCenter.hasMovement=#{true}) or #{costCenterDataModel.showAll}=#{true})";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Boolean getShowAll() {
        if (showAll == null) {
            showAll = false;
        }
        return showAll;
    }

    public void setShowAll(Boolean showAll) {
        this.showAll = showAll;
    }

    public void enableShowAll() {
        if (showAll == null) {
            showAll = true;
        }
    }
}