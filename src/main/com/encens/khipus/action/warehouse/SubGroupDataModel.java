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
 * @version 2.0
 */
@Name("subGroupDataModel")
@Scope(ScopeType.PAGE)
public class SubGroupDataModel extends QueryDataModel<SubGroupPK, SubGroup> {

    private static final String[] RESTRICTIONS = {
            "subGroup.groupCode= #{warehouseGroup.groupCode}"
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
}
