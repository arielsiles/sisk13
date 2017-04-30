package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.CollectionDocument;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.23
 */
@Name("collectionDocumentDataModel")
@Scope(ScopeType.PAGE)
public class CollectionDocumentDataModel extends QueryDataModel<Long, CollectionDocument> {
    private static final String[] RESTRICTIONS = {
    };

    @Create
    public void init() {
        sortProperty = "collectionDocument.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select collectionDocument from CollectionDocument collectionDocument";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}