package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.sales.Incoterm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Incoterm catalog.
 *
 * @author
 * @version 1.0
 */
@Name("incotermDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('INCOTERM','VIEW')}")
public class IncotermDataModel extends QueryDataModel<Long, Incoterm> {

    private static final String[] RESTRICTIONS = {
            "lower(incoterm.code) like concat(lower(#{incotermDataModel.criteria.code}), '%')",
            "lower(incoterm.name) like concat('%', concat(lower(#{incotermDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "incoterm.code";
    }

    @Override
    public String getEjbql() {
        return "select incoterm from Incoterm incoterm";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
