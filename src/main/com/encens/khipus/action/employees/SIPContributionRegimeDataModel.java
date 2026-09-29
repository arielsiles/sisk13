package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.SIPContributionRegime;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 6.0.129
 */
@Name("sipContributionRegimeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SIPCONTRIBUTIONREGIME','VIEW')}")
public class SIPContributionRegimeDataModel extends QueryDataModel<Long, SIPContributionRegime> {

    private static final String[] RESTRICTIONS = {
            "lower(sipContributionRegime.name) like concat('%', concat(lower(#{sipContributionRegimeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "sipContributionRegime.name";
    }

    @Override
    public String getEjbql() {
        return "select sipContributionRegime from SIPContributionRegime sipContributionRegime";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
