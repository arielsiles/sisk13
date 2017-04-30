package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.SocialWelfareEntityDuplicatedIdNumberException;
import com.encens.khipus.exception.employees.SocialWelfareEntityDuplicatedNameException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.employees.SocialWelfareEntity;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.employees.SocialWelfareEntityService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.List;

/**
 * @author
 * @version 3.5
 */
@Name("socialWelfareEntityAction")
@Scope(ScopeType.CONVERSATION)
public class SocialWelfareEntityAction extends GenericAction<SocialWelfareEntity> {
    @In
    private ExtensionService extensionService;
    @In
    private SocialWelfareEntityService socialWelfareEntityService;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    @Factory(value = "socialWelfareEntity", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SOCIALWELFAREENTITY','VIEW')}")
    public SocialWelfareEntity initSocialWelfareEntity() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }


    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SOCIALWELFAREENTITY','VIEW')}")
    public String select(SocialWelfareEntity instance) {
        String outCome = super.select(instance);
        updateShowExtension();
        return outCome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SOCIALWELFAREENTITY','CREATE')}")
    public String create() {
        try {
            socialWelfareEntityService.createEntity(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (SocialWelfareEntityDuplicatedIdNumberException e) {
            addDuplicatedFieldMessage("SocialWelfareEntity.idNumber", getInstance().getIdNumber());
            return Outcome.REDISPLAY;
        } catch (SocialWelfareEntityDuplicatedNameException e) {
            addDuplicatedFieldMessage("SocialWelfareEntity.name", getInstance().getName());
            return Outcome.REDISPLAY;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SOCIALWELFAREENTITY','CREATE')}")
    public void createAndNew() {
        try {
            socialWelfareEntityService.createEntity(getInstance());
            addCreatedMessage();
            createInstance();
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        } catch (SocialWelfareEntityDuplicatedIdNumberException e) {
            addDuplicatedFieldMessage("SocialWelfareEntity.idNumber", getInstance().getIdNumber());
        } catch (SocialWelfareEntityDuplicatedNameException e) {
            addDuplicatedFieldMessage("SocialWelfareEntity.name", getInstance().getName());
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SOCIALWELFAREENTITY','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            socialWelfareEntityService.updateEntity(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (SocialWelfareEntityDuplicatedIdNumberException e) {
            addDuplicatedFieldMessage("SocialWelfareEntity.idNumber", getInstance().getIdNumber());
            return Outcome.REDISPLAY;
        } catch (SocialWelfareEntityDuplicatedNameException e) {
            addDuplicatedFieldMessage("SocialWelfareEntity.name", getInstance().getName());
            return Outcome.REDISPLAY;
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('SOCIALWELFAREENTITY','DELETE')}")
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

    public void assignProvider(Provider provider) {
        getInstance().setProvider(provider);
        updateProviderInfo();
    }

    public void updateProviderInfo() {
        try {
            getInstance().setProvider(getService().findById(Provider.class, getInstance().getProvider().getId()));
        } catch (EntryNotFoundException ignored) {
        }
    }

    public void clearProvider() {
        getInstance().setProvider(null);
    }
}