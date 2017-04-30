package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetGroup;
import com.encens.khipus.model.fixedassets.FixedAssetGroupPk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAssetGroup
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetGroupDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETGROUP','VIEW')}")
public class FixedAssetGroupDataModel extends QueryDataModel<FixedAssetGroupPk, FixedAssetGroup> {
    private static final String[] RESTRICTIONS =
            {"lower(fixedAssetGroup.groupCode) like concat('%', concat(lower(#{fixedAssetGroupDataModel.criteria.groupCode}),'%'))",
                    "lower(fixedAssetGroup.description) like concat('%', concat(lower(#{fixedAssetGroupDataModel.criteria.description}),'%'))"};

    @Create
    public void init() {
        sortProperty = "fixedAssetGroup.groupCode";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetGroup from FixedAssetGroup fixedAssetGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}