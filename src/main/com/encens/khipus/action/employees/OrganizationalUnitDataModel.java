package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.OrganizationalUnit;
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
@Name("organizationalUnitDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ORGANIZATIONALUNIT','VIEW')}")
public class OrganizationalUnitDataModel extends QueryDataModel<Long, OrganizationalUnit> {

    private static final String[] RESTRICTIONS =
            {"lower(organizationalUnit.name) like concat('%', concat(lower(#{organizationalUnitDataModel.criteria.name}), '%'))"};

    @Override
    public String getEjbql() {
        return "select organizationalUnit from OrganizationalUnit organizationalUnit";
    }

    @Create
    public void defaultSort() {
        sortProperty = "organizationalUnit.name";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}