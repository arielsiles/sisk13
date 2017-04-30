package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.District;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for District
 *
 * @author:
 */

@Name("districtDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DISTRICT','VIEW')}")
public class DistrictDataModel extends QueryDataModel<Long, District> {

    private static final String[] RESTRICTIONS =
            {"lower(district.name) like concat('%', concat(lower(#{districtDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "district.name";
    }

    @Override
    public String getEjbql() {
        return "select district from District district";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
