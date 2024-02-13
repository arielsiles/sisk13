package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountPk;
import com.encens.khipus.model.finances.PresetAccountingTemplate;
import com.encens.khipus.model.finances.TypePresetAccountingTemplate;
import com.encens.khipus.model.xproduction.XMachine;
import com.encens.khipus.model.xproduction.XMachineProcess;
import com.encens.khipus.model.xproduction.XProcess;
import com.encens.khipus.service.finances.TypePresetAccountingTemplateService;
import com.encens.khipus.service.xproduction.XProcessService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Actions for Customer category
 *
 * @author:
 */

@Name("xprocessAction")
@Scope(ScopeType.CONVERSATION)
public class ProcessRegistrationAction extends GenericAction<XProcess> {

    private List<XMachineProcess> xMachineProcesses = new ArrayList<XMachineProcess>();
    private List<XMachine> selectedMachines = new ArrayList<XMachine>();

    @In
    private XProcessService xProcessService;

    @Factory(value ="xProcess", scope = ScopeType.STATELESS)
    //@Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
    public XProcess initPresetAccountingTemplate() {
        //typePresetAccountingTemplates = typePresetAccountingTemplateService.getTypePresetAccountingTemplates(getInstance());
        return getInstance();
}

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    public String select(XProcess instance) {
        String outCome = super.select(instance);
        setxMachineProcesses(xProcessService.getXMachineProcess(getInstance()));
        return outCome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    /*@End(beforeRedirect = true)*/
    @Override
    public String update(){

        Long currentVersion = (Long) getVersion(getInstance());
        try {
            xProcessService.updateXMachineProcesses(xMachineProcesses);
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
        }
        addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @Override
    @End
    public String create() {

        try {
            getService().create(getInstance());
            super.select(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }
    @Override
    @End
    public String delete() {
        try {
            getService().delete(getInstance());
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
        }

        return Outcome.NEXT;
    }

    public List<XMachineProcess> getxMachineProcesses() {
        return xMachineProcesses;
    }

    public void setxMachineProcesses(List<XMachineProcess> xMachineProcesses) {
        this.xMachineProcesses = xMachineProcesses;
    }

    public List<XMachine> getSelectedMachines() {
        return selectedMachines;
    }

    public void setSelectedMachines(List<XMachine> selectedMachines) {
        this.selectedMachines = selectedMachines;
    }

    public XProcessService getxProcessService() {
        return xProcessService;
    }

    public void setxProcessService(XProcessService xProcessService) {
        this.xProcessService = xProcessService;
    }
}
