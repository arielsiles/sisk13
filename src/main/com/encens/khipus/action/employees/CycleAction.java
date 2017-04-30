package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.service.employees.CycleService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;


/**
 * Actions for Cycle
 *
 * @author
 * @version 2.9
 */

@Name("cycleAction")
@Scope(ScopeType.CONVERSATION)
public class CycleAction extends GenericAction<Cycle> {
    @In
    private CycleService cycleService;

    private Sector sector;

    @Factory(value = "cycle", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('CYCLE','VIEW')}")
    public Cycle initCurrency() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('CYCLE','VIEW')}")
    public String select(Cycle instance) {
        String outcome = super.select(instance);
        sector = getInstance().getCycleType().getSector();
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CYCLE','CREATE')}")
    public String create() {
        /* When a cycle is created it can replace the last active cycle defined for the corresponding sector*/
        if (getInstance().getActive() && cycleService.isThereActiveCycleForSector(getInstance().getCycleType().getSector())) {
            cycleService.unActiveCycleForSector(getInstance().getCycleType().getSector());
        }
        return super.create();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('CYCLE','UPDATE')}")
    public String update() {
        /* When a cycle is updated it can replace the last active cycle defined for the corresponding sector*/
        if (!cycleService.isActiveInDataBase(getInstance()) &&
                getInstance().getActive() && cycleService.isThereActiveCycleForSector(getInstance().getCycleType().getSector())) {
            cycleService.unActiveCycleForSector(getInstance().getCycleType().getSector());
        }
        return super.update();
    }

    private void addSectorAlreadyHasActiveCycleMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Cycle.error.sectorAlreadyHasActiveCycle");
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}