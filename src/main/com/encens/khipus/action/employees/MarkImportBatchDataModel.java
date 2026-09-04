package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.MarkImportBatch;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Name("markImportBatchDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('MARKIMPORT','VIEW')}")
public class MarkImportBatchDataModel extends QueryDataModel<Long, MarkImportBatch> {

    private static final String[] RESTRICTIONS = {
            "lower(markImportBatch.fileName) like concat('%', concat(lower(#{markImportBatchDataModel.criteria.fileName}), '%'))"};

    @Create
    public void init() {
        sortProperty = "markImportBatch.uploadDate";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select markImportBatch from MarkImportBatch markImportBatch";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
