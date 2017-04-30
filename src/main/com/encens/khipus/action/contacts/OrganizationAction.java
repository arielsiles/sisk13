package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.contacts.Organization;
import com.encens.khipus.service.customers.ExtensionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.List;

/**
 * OrganizationAction
 *
 * @author
 * @version 2.26
 */
@Name("organizationAction")
@Scope(ScopeType.CONVERSATION)
public class OrganizationAction extends GenericAction<Organization> {
    @In
    private ExtensionService extensionService;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    @Factory(value = "organization", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ORGANIZATION','VIEW')}")
    public Organization initOrganization() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }


    @Override
    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('ORGANIZATION','VIEW')}")
    public String select(Organization instance) {
        String outCome = super.select(instance);
        updateShowExtension();
        return outCome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ORGANIZATION','CREATE')}")
    public String create() {
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('ORGANIZATION','CREATE')}")
    public void createAndNew() {
        super.createAndNew();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ORGANIZATION','UPDATE')}")
    public String update() {
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ORGANIZATION','DELETE')}")
    public String delete() {
        return super.delete();
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().setExtensionSite(null);
        }
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
    }
}
