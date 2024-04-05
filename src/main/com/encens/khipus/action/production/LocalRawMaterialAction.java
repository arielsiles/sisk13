package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.LocalRawMaterial;
import com.encens.khipus.model.production.ZoneInspection;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;


@Name("localRawMaterialAction")
@Scope(ScopeType.CONVERSATION)
public class LocalRawMaterialAction extends GenericAction<LocalRawMaterial> {

    @In(create = true)
    private ZoneInspectionAction zoneInspectionAction;

    @Factory(value = "localRawMaterial", scope = ScopeType.STATELESS)
    public LocalRawMaterial initLaboratoryResult() {
        return getInstance();
    }

    @Override
    @Begin(join = true, flushMode = FlushModeType.MANUAL)
    public String create() {

        try {
            ZoneInspection zoneInspection = zoneInspectionAction.getInstance();
            getInstance().setZoneInspection(zoneInspection);
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String select(LocalRawMaterial localRawMaterial) {
        setOp(OP_UPDATE);

        setInstance(localRawMaterial);
        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    public String update() {
        return super.update();
    }

}
