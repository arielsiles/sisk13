package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * @author
 * @version 2.0
 */
@Name("warehouseDocumentTypeAction")
@Scope(ScopeType.CONVERSATION)
public class DocumentTypeAction extends GenericAction<WarehouseDocumentType> {

    @Factory(value = "warehouseDocumentType", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('WAREHOUSEDOCUMENTTYPE','VIEW')}")
    public WarehouseDocumentType initDocumentType() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    protected void addDuplicatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Warehouse.common.message.duplicated", getInstance().getId().getDocumentCode());
    }

    public void clearContraAccount() {
        getInstance().setContraAccount(null);
    }

}
