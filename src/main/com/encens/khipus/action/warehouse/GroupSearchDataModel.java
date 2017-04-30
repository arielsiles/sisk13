package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.model.warehouse.GroupPK;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * This class is a DataModel for the group modal panel
 * @author
 * @version 2.3
 */
@Name("groupSearchDataModel")
@Scope(ScopeType.PAGE)
public class GroupSearchDataModel extends QueryDataModel<GroupPK, Group> {
    private static final String[] RESTRICTIONS =
            {
                    "lower(o.id.groupCode) like concat(lower(#{groupSearchDataModel.criteria.id.groupCode}), '%')",
                    "lower(o.name) like concat('%',concat(lower(#{groupSearchDataModel.criteria.name}), '%'))"
            };

    @Create
    public void init() {
        sortProperty = "o.name";
    }

    @Override
    public String getEjbql() {
        return "select o from Group o";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
