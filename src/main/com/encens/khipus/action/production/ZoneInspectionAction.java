package com.encens.khipus.action.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.ZoneInspection;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

@Name("zoneInspectionAction")
@Scope(ScopeType.CONVERSATION)
public class ZoneInspectionAction extends GenericAction<ZoneInspection> {

    private String activeTabName = "laboratoryTab";

    @In(create = true)
    private LaboratoryResultAction laboratoryResultAction;
    @In(create = true)
    private LocalRawMaterialAction localRawMaterialAction;

    @Factory(value = "zoneInspection", scope = ScopeType.STATELESS)
    public ZoneInspection initContinent() {
        return getInstance();
    }

    @Override
    public String create() {
        try {
            getService().create(getInstance());
            super.select(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    public String update() {
        Long currentVersion = (Long) getVersion(getInstance());
        try {
            getService().update(getInstance());
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
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    public void clearProductiveZone() {
        getInstance().setProductiveZone(null);
    }

    public void assignProductiveZone(ProductiveZone productiveZone){
        getInstance().setProductiveZone(productiveZone);
    }

    public void assignProducer(RawMaterialProducer producer){
        getInstance().setProducer(producer);
    }

    public void clearProducer() {
        getInstance().setProducer(null);
    }

    public String getActiveTabName() {
        return activeTabName;
    }

    public void setActiveTabName(String activeTabName) {
        this.activeTabName = activeTabName;
    }

    public void enableLaboratoryTab() {
        setActiveTabName("laboratoryTab");
    }

    public void enableLocalRawMaterialTab() {
        setActiveTabName("localRawMaterialTab");
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addLaboratoryResult() {
        enableLaboratoryTab();
        laboratoryResultAction.setInstance(null);
        return Outcome.SUCCESS;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addLocalRawMaterial() {
        enableLocalRawMaterialTab();
        localRawMaterialAction.setInstance(null);
        return Outcome.SUCCESS;
    }
}
