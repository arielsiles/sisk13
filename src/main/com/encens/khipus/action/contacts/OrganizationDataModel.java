package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Organization;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * OrganizationDataModel
 *
 * @author
 * @version 2.26
 */
@Name("organizationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ORGANIZATION','VIEW')}")
public class OrganizationDataModel extends QueryDataModel<Long, Organization> {
    private static final String[] RESTRICTIONS =
            {"lower(organization.name) like concat('%', concat(lower(#{organizationDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "organization.name";
    }

    @Override
    public String getEjbql() {
        return "select organization from Organization organization";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
