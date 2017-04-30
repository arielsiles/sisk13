package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CollectionDocument;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.service.finances.CollectionDocumentService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


/**
 * @author
 * @version 2.23
 */
@Name("collectionDocumentAction")
@Scope(ScopeType.CONVERSATION)
public class CollectionDocumentAction extends GenericAction<CollectionDocument> {
    @In
    private CollectionDocumentService collectionDocumentService;

    @In(value = "collectionDocumentAction")
    private CollectionDocumentAction collectionDocumentAction;

    @Factory(value = "collectionDocument", scope = ScopeType.STATELESS)
    public CollectionDocument initCollectionDocument() {
        return getInstance();
    }

    @Factory(value = "collectionDocumentTypes", scope = ScopeType.STATELESS)
    public CollectionDocumentType[] initCollectionDocumentTypes() {
        return CollectionDocumentType.valuesForMovements();
    }
}