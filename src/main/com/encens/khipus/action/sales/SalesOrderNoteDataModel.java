package com.encens.khipus.action.sales;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.sales.SalesOrderNote;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for sales order note catalog.
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderNoteDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALESORDERNOTE','VIEW')}")
public class SalesOrderNoteDataModel extends QueryDataModel<Long, SalesOrderNote> {

    private static final String[] RESTRICTIONS = {
            "lower(salesOrderNote.text) like concat('%', concat(lower(#{salesOrderNoteDataModel.criteria.text}), '%'))"};

    @Create
    public void init() {
        sortProperty = "salesOrderNote.sortOrder,salesOrderNote.id";
    }

    @Override
    public String getEjbql() {
        return "select salesOrderNote from SalesOrderNote salesOrderNote";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
