package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.Role;
import com.encens.khipus.service.admin.RoleService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.List;

/**
 * Role data model
 *
 * @author
 * @version 1.0
 */
@Name("roleDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ROLE','VIEW')}")
public class RoleDataModel extends QueryDataModel<Long, Role> {

    @In
    private transient RoleService roleService;

    private Role criteria;

    @Create
    public void init() {
        sortProperty = "role.name";
        criteria = new Role();
    }

    public Long getCount() {
        return roleService.getCount(sortProperty, sortAsc, criteria);
    }

    public List<Role> getList(Integer firstRow, Integer maxResults) {
        return roleService.getList(firstRow, maxResults, sortProperty, sortAsc, criteria);
    }

    public Role getCriteria() {
        return criteria;
    }

    public void setCriteria(Role criteria) {
        this.criteria = criteria;
    }
}
