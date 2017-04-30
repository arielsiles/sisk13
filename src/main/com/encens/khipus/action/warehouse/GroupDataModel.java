package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.model.warehouse.GroupPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("groupDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GROUP','VIEW')}")
public class GroupDataModel extends QueryDataModel<GroupPK, Group> {

    private static final String[] RESTRICTIONS =
            {"lower(warehouseGroup.name) like concat('%', concat(lower(#{groupDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "warehouseGroup.name";
    }

    @Override
    public String getEjbql() {
        return "select warehouseGroup from Group warehouseGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
