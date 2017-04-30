package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.model.warehouse.SubGroupPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.1
 */
@Name("subGroupSearchDataModel")
@Scope(ScopeType.PAGE)
public class SubGroupSearchDataModel extends QueryDataModel<SubGroupPK, SubGroup> {
    private String groupName;
    private String groupCode;

    private static final String[] RESTRICTIONS = {
            "lower(subGroup.group.name) like concat('%',concat(lower(#{subGroupSearchDataModel.groupName}), '%'))",
            "lower(subGroup.group.groupCode) like concat(lower(#{subGroupSearchDataModel.groupCode}), '%')",
            "lower(subGroup.name) like concat('%',concat(lower(#{subGroupSearchDataModel.criteria.name}), '%'))",
            "lower(subGroup.subGroupCode) like concat(lower(#{subGroupSearchDataModel.criteria.subGroupCode}), '%')",
            "subGroup.group =#{productItemCostUnitReportAction.group}",
            "subGroup.state = #{enumerationUtil.getEnumValue('com.encens.khipus.model.warehouse.SubGroupState', 'VIG')}"
    };

    @Create
    public void init() {
        sortProperty = "subGroup.name";
    }

    @Override
    public String getEjbql() {
        return "select subGroup from SubGroup subGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
