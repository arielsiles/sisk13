package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroup;
import com.encens.khipus.model.fixedassets.FixedAssetSubGroupPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.26
 */

@Name("subGroupListDataModel")
@Scope(ScopeType.PAGE)
public class SubGroupListDataModel extends QueryDataModel<FixedAssetSubGroupPk, FixedAssetSubGroup> {
    private static final String[] RESTRICTIONS = {
            "element.fixedAssetGroup = #{subGroupListDataModel.criteria.fixedAssetGroup}",
            "lower(element.description) like concat('%', concat(lower(#{subGroupListDataModel.criteria.description}), '%'))",
            "lower(element.fixedAssetSubGroupCode) like concat('%', concat(lower(#{subGroupListDataModel.criteria.fixedAssetSubGroupCode}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "element.id.fixedAssetSubGroupCode";
    }

    @Override
    public String getEjbql() {
        return "select element from FixedAssetSubGroup element";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
