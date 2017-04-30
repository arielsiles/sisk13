package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetMovementType;
import com.encens.khipus.model.fixedassets.FixedAssetMovementTypePk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for FixedAssetMovementType
 *
 * @author
 * @version 2.0
 */

@Name("fixedAssetMovementTypeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETMOVEMENTTYPE','VIEW')}")
public class FixedAssetMovementTypeDataModel extends QueryDataModel<FixedAssetMovementTypePk, FixedAssetMovementType> {
    private static final String[] RESTRICTIONS = {
            "lower(fixedAssetMovementType.movementCode) like concat(lower(#{fixedAssetMovementTypeDataModel.criteria.movementCode}),'%')",
            "fixedAssetMovementType.fixedAssetMovementTypeEnum = #{fixedAssetMovementTypeDataModel.criteria.fixedAssetMovementTypeEnum}",
            "fixedAssetMovementType.fixedAssetMovementTypeState = #{fixedAssetMovementTypeDataModel.criteria.fixedAssetMovementTypeState}",
    };

    @Create
    public void init() {
        sortProperty = "fixedAssetMovementType.movementCode";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetMovementType from FixedAssetMovementType fixedAssetMovementType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}