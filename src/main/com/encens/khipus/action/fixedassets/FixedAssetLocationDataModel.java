package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.fixedassets.FixedAssetLocation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.28.2
 */
@Name("fixedAssetLocationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FIXEDASSETLOCATION','VIEW')}")
public class FixedAssetLocationDataModel extends QueryDataModel<Long, FixedAssetLocation> {

    private static final String[] RESTRICTIONS =
            {"lower(fixedAssetLocation.name) like concat('%', concat(#{fixedAssetLocationDataModel.criteria.name}, '%'))"};

    @Create
    public void init() {
        sortProperty = "fixedAssetLocation.name";
    }

    @Override
    public String getEjbql() {
        return "select fixedAssetLocation from FixedAssetLocation fixedAssetLocation";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
