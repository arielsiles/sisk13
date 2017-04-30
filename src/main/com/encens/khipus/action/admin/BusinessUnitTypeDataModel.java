package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnitType;
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
 * Date: 17-dic-2008
 * Time: 16:51:09
 * To change this template use File | Settings | File Templates.
 */

@Name("businessUnitTypeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BUSINESSUNITTYPE','VIEW')}")
public class BusinessUnitTypeDataModel extends QueryDataModel<Long, BusinessUnitType> {

    private static final String[] RESTRICTIONS =
            {"lower(businessUnitType.name) like concat('%', concat(lower(#{businessUnitTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "businessUnitType.name";
    }

    @Override
    public String getEjbql() {
        return "select businessUnitType from BusinessUnitType businessUnitType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
