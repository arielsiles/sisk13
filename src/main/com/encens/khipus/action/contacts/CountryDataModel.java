package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Country;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 */
@Name("countryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('COUNTRY','VIEW')}")
public class CountryDataModel extends QueryDataModel<Long, Country> {

    private static final String[] RESTRICTIONS = {
            "lower(country.name) like concat('%', concat(lower(#{countryDataModel.criteria.name}), '%'))",
            "lower(country.areaCode) like concat(lower(#{countryDataModel.criteria.areaCode}), '%')",
            "lower(country.prefix) like concat(lower(#{countryDataModel.criteria.prefix}), '%')"};

    @Create
    public void init() {
        sortProperty = "country.name";
    }

    @Override
    public String getEjbql() {
        return "select country from Country country";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
