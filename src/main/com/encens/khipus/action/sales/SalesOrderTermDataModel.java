package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.sales.SalesOrderTerm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for sales order header term catalog.
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderTermDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALESORDERTERM','VIEW')}")
public class SalesOrderTermDataModel extends QueryDataModel<Long, SalesOrderTerm> {

    private static final String[] RESTRICTIONS = {
            "lower(salesOrderTerm.text) like concat('%', concat(lower(#{salesOrderTermDataModel.criteria.text}), '%'))"};

    @Create
    public void init() {
        sortProperty = "salesOrderTerm.sortOrder,salesOrderTerm.id";
    }

    @Override
    public String getEjbql() {
        return "select salesOrderTerm from SalesOrderTerm salesOrderTerm";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
