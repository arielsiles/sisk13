package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.DocumentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Document type
 *
 * @author:
 */
@Name("documentTypeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('DOCUMENTTYPE','VIEW')}")
public class DocumentTypeDataModel extends QueryDataModel<Long, DocumentType> {

    private static final String[] RESTRICTIONS =
            {"lower(documentType.name) like concat('%', concat(lower(#{documentTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "documentType.name";
    }

    @Override
    public String getEjbql() {
        return "select documentType from DocumentType documentType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
