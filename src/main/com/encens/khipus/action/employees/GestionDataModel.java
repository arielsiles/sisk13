package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Gestion;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Gestion
 *
 * @author
 */

@Name("gestionDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GESTION','VIEW')}")
public class GestionDataModel extends QueryDataModel<Long, Gestion> {
    private static final String[] RESTRICTIONS =
            {"gestion.year = #{gestionDataModel.criteria.year}"};

    @Create
    public void init() {
        sortProperty = "gestion.year";
    }

    @Override
    public String getEjbql() {
        return "select gestion from Gestion gestion";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}