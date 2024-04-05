package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.LocalRawMaterial;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("localRawMaterialDataModel")
@Scope(ScopeType.PAGE)
public class LocalRawMaterialDataModel extends QueryDataModel<Long, LocalRawMaterial> {

    private static final String[] RESTRICTIONS = {
            "localRawMaterial.zoneInspection = #{zoneInspection}"
    };

    public void init() {
        sortProperty = "localRawMaterial.id";
    }

    @Override
    public String getEjbql() {
    return "select localRawMaterial from LocalRawMaterial localRawMaterial";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}