package com.encens.khipus.action.fixedassets;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.model.fixedassets.PurchaseOrderCause;
import com.encens.khipus.service.fixedassets.PurchaseOrderCauseFixedAssetStateService;
import com.encens.khipus.service.fixedassets.PurchaseOrderCauseService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Actions for PurchaseOrderCause
 *
 * @author
 * @version 2.26
 */

@Name("purchaseOrderCauseAction")
@Scope(ScopeType.CONVERSATION)
public class PurchaseOrderCauseAction extends GenericAction<PurchaseOrderCause> {
    @In
    public PurchaseOrderCauseService purchaseOrderCauseService;
    @In
    public PurchaseOrderCauseFixedAssetStateService purchaseOrderCauseFixedAssetStateService;

    /*lists to hold associated fixedAssetState information*/
    public List<FixedAssetState> selectedFixedAssetStates;
    public List<FixedAssetState> unSelectedFixedAssetStates;

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            getInstance().setRequiresFixedAssets(false);
            selectedFixedAssetStates = new ArrayList<FixedAssetState>();
            unSelectedFixedAssetStates = Arrays.asList(FixedAssetState.values());
        }
    }

    @Factory(value = "purchaseOrderCause", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('PURCHASEORDERCAUSE','VIEW')}")
    public PurchaseOrderCause initPurchaseOrderCause() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Restrict("#{s:hasPermission('PURCHASEORDERCAUSE','VIEW')}")
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(PurchaseOrderCause instance) {
        String outcome = super.select(instance);
        if (outcome.equals(Outcome.SUCCESS)) {
            if (getInstance().getRequiresFixedAssets()) {
                selectedFixedAssetStates = purchaseOrderCauseFixedAssetStateService.findFixedAssetStateByPurchaseOrderCause(getInstance(), null);
                unSelectedFixedAssetStates = new ArrayList<FixedAssetState>();
                for (FixedAssetState fixedAssetState : FixedAssetState.values()) {
                    if (!selectedFixedAssetStates.contains(fixedAssetState)) {
                        unSelectedFixedAssetStates.add(fixedAssetState);
                    }
                }
            } else {
                selectedFixedAssetStates = new ArrayList<FixedAssetState>();
                unSelectedFixedAssetStates = Arrays.asList(FixedAssetState.values());
            }
        }
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PURCHASEORDERCAUSE','CREATE')}")
    public String create() {
        try {
            /*clear selectedFixedAssetStates*/
            if (!getInstance().getRequiresFixedAssets()) {
                selectedFixedAssetStates.clear();
                unSelectedFixedAssetStates = Arrays.asList(FixedAssetState.values());
            }
            purchaseOrderCauseService.create(getInstance(), selectedFixedAssetStates);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    @Override
    public void createAndNew() {
        try {
            /*clear selectedFixedAssetStates*/
            if (!getInstance().getRequiresFixedAssets()) {
                selectedFixedAssetStates.clear();
                unSelectedFixedAssetStates = Arrays.asList(FixedAssetState.values());
            }
            purchaseOrderCauseService.create(getInstance(), selectedFixedAssetStates);
            addCreatedMessage();
            createInstance();
            /*reset action lists*/
            selectedFixedAssetStates.clear();
            unSelectedFixedAssetStates = Arrays.asList(FixedAssetState.values());
//            getInstance().setRequiresFixedAssets(false);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PURCHASEORDERCAUSE','UPDATE')}")
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            purchaseOrderCauseService.updatePurchaseOrderCause(getInstance(), selectedFixedAssetStates);
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            setVersion(getInstance(), currentVersion);
            addDuplicatedMessage();
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
        }
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('PURCHASEORDERCAUSE','DELETE')}")
    public String delete() {
        try {
            purchaseOrderCauseService.deletePurchaseOrderCause(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.SUCCESS;
    }


    public boolean validateFixedAssetStates() {
        return !(getInstance().getRequiresFixedAssets() && selectedFixedAssetStates.size() <= 0);
    }

    public void updateValuesByType() {
        if (getInstance().isFixedassetPartsPurchase()) {
            getInstance().setRequiresFixedAssets(true);
            setSelectedFixedAssetStates(FixedAssetState.getMovementState());
        }
    }

    /*getters and setters*/

    public List<FixedAssetState> getSelectedFixedAssetStates() {
        return selectedFixedAssetStates;
    }

    public void setSelectedFixedAssetStates(List<FixedAssetState> selectedFixedAssetStates) {
        this.selectedFixedAssetStates = selectedFixedAssetStates;
    }

    public List<FixedAssetState> getUnSelectedFixedAssetStates() {
        return unSelectedFixedAssetStates;
    }

    public void setUnSelectedFixedAssetStates(List<FixedAssetState> unSelectedFixedAssetStates) {
        this.unSelectedFixedAssetStates = unSelectedFixedAssetStates;
    }

    /*messages*/


}