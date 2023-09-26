package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.EntityType;
import com.encens.khipus.model.warehouse.Destination;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Actions for Destination
 *
 * @version 2.7
 * @author:
 */
@Name("destinationAction")
@Scope(ScopeType.CONVERSATION)
public class DestinationAction extends GenericAction<Destination> {

    @Factory(value = "destination", scope = ScopeType.STATELESS)
    public Destination initDocumentType() {
        return getInstance();
    }

    @Factory("destinationEntityTypeList")
    public EntityType[] getEntityType() {
        return EntityType.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
