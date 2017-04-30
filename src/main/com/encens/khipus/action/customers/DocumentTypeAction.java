package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.EntityType;
import com.encens.khipus.model.customers.DocumentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Document Type
 *
 * @version 2.7
 * @author:
 */
@Name("documentTypeAction")
@Scope(ScopeType.CONVERSATION)
public class DocumentTypeAction extends GenericAction<DocumentType> {

    @Factory(value = "documentType", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('DOCUMENTTYPE','VIEW')}")
    public DocumentType initDocumentType() {
        return getInstance();
    }

    @Factory("documentTypeEntityTypeList")
    public EntityType[] getEntityType() {
        return EntityType.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
