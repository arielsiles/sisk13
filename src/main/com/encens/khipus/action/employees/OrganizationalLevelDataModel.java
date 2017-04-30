package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.OrganizationalLevel;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Charge data model
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("organizationalLevelDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ORGANIZATIONALLEVEL','VIEW')}")
public class OrganizationalLevelDataModel extends QueryDataModel<Long, OrganizationalLevel> {

    private static final String[] RESTRICTIONS =
            {"lower(organizationalLevel.name) like concat('%', concat(lower(#{organizationalLevelDataModel.criteria.name}), '%'))"};

    @Override
    public String getEjbql() {
        return "select organizationalLevel from OrganizationalLevel organizationalLevel";
    }

    @Create
    public void defaultSort() {
        sortProperty = "organizationalLevel.name";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}