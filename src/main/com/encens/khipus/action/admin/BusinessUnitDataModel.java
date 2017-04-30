package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: macmac
 * Date: 21-dic-2008
 * Time: 23:26:54
 * To change this template use File | Settings | File Templates.
 */

@Name("businessUnitDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BUSINESSUNIT','VIEW')}")
public class BusinessUnitDataModel extends QueryDataModel<Long, BusinessUnit> {

    private static final String[] RESTRICTIONS =
            {"lower(businessUnit.descriptionBU) like concat('%', concat(lower(#{businessUnitDataModel.criteria.descriptionBU}), '%'))"};

    @Override
    public String getEjbql() {
        //select businessUnit, organization from BusinessUnit businessUnit JOIN businessUnit.organization organization
        return "select businessUnit from BusinessUnit businessUnit";
    }

    @Create
    public void defaultSort() {
        sortProperty = "businessUnit.organization.name";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
