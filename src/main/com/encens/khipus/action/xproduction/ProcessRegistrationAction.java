package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.xproduction.XMachine;
import com.encens.khipus.model.xproduction.XMachineProcess;
import com.encens.khipus.model.xproduction.XProcess;
import com.encens.khipus.model.xproduction.XProcessState;
import com.encens.khipus.service.xproduction.XProcessService;
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
    private List<Long> selectedMachineIds = new ArrayList<Long>();

    @In
    private XProcessService xProcessService;

    @Factory(value ="xProcess", scope = ScopeType.STATELESS)
    //@Restrict("#{s:hasPermission('CUSTOMERCATEGORY','VIEW')}")
    public XProcess initXProcess() {
        //typePresetAccountingTemplates = typePresetAccountingTemplateService.getTypePresetAccountingTemplates(getInstance());
        return getInstance();
    }

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    public String select(XProcess instance) {
        String outCome = super.select(instance);
        setxMachineProcesses(xProcessService.getXMachineProcess(getInstance()));

        loadMachine(getxMachineProcesses());

        return outCome;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public void loadMachine(List<XMachineProcess> xMachineProcessList){

        for (XMachineProcess xMachineProcess : xMachineProcessList) {
            this.selectedMachines.add(xMachineProcess.getXmachine());
            this.selectedMachineIds.add(xMachineProcess.getXmachine().getId());
        }

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

    @End
    public String approve() {
        getInstance().setState(XProcessState.APR);
        update();
        return Outcome.SUCCESS;
    }

    @End
    public String annul() {
        getInstance().setState(XProcessState.ANL);
        update();
        return Outcome.SUCCESS;
    }

    public void setMachine(XMachine xMachine) {
        //if (!selectedMachines.contains(xMachine)) {
        if (!selectedMachineIds.contains(xMachine.getId())) {

            selectedMachines.add(xMachine);
            selectedMachineIds.add(xMachine.getId());

            XMachineProcess item = new XMachineProcess();
            item.setXmachine(xMachine);
            item.setxProcess(getInstance());
            getxMachineProcesses().add(item);
        }
    }

    public void removeXMachine(XMachineProcess instance) {
        selectedMachines.remove(instance.getXmachine());
        selectedMachineIds.remove(instance.getXmachine().getId());
        xMachineProcesses.remove(instance);
        xProcessService.deleteXMachineProcess(instance);
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
            return Outcome.FAIL;
        }
    }

    public boolean isPending(){
        return getInstance().getState().equals(XProcessState.PEN);
    }

    public boolean isApproved(){
        return getInstance().getState().equals(XProcessState.APR);
    }

    public boolean isAnnulled(){
        return getInstance().getState().equals(XProcessState.ANL);
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

}
