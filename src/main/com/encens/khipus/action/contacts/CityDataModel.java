package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.City;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for City
 *
 * @author:
 */

@Name("cityDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CITY','VIEW')}")
public class CityDataModel extends QueryDataModel<Long, City> {

    private static final String[] RESTRICTIONS =
            {"lower(city.name) like concat('%', concat(lower(#{cityDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "city.name";
    }

    @Override
    public String getEjbql() {
        return "select city from City city";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
