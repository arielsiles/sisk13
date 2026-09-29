package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.ContractDuration;
import com.encens.khipus.model.employees.ContractMode;
import com.encens.khipus.model.employees.ContractMovement;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.service.employees.ContractConditionService;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Cambiar la condicion de un contrato y dar de baja.
 * <p/>
 * Vive fuera del formulario del contrato a proposito: son actos que dejan rastro, no ediciones
 * de campos. Quien mira el contrato dentro de un ano tiene que poder saber desde cuando es
 * laboral, por que, y quien lo decidio.
 *
 * @author
 * @version 6.1.0
 */
@Name("contractConditionAction")
@Scope(ScopeType.SESSION)
public class ContractConditionAction implements Serializable {

    @Logger
    private Log log;

    @In
    private ContractConditionService contractConditionService;

    @In(create = true)
    private FacesMessages facesMessages;

    /* El contrato sobre el que se esta actuando. */
    private Contract contract;

    /* Cambio de condicion. */
    private ContractMode newContractMode;
    private ContractDuration newDuration = ContractDuration.INDEFINITE;
    private Date newEndDate;
    /* Vacia a proposito: con la fecha de hoy puesta nadie la cambia, y esa fecha es la
       que queda en el historial como el momento en que rige la condicion nueva. */
    private Date changeDate;
    private String changeReason;

    /* Baja. */
    private Date departureDate;
    private String terminationReason;

    // ------------------------------------------------------------ seleccion

    /** Prepara los dos formularios con lo que el contrato tiene hoy. */
    public void select(Contract selected) {
        this.contract = selected;
        this.newContractMode = null == selected ? null : selected.getContractMode();
        this.newDuration = null == selected ? ContractDuration.INDEFINITE : selected.getDuration();
        this.newEndDate = null == selected ? null : selected.getEndDate();
        this.changeDate = null;
        this.changeReason = null;
        this.departureDate = null;
        this.terminationReason = null;
    }

    /** Mes en el que se abre el calendario de la fecha de fin. */
    public Date getEndCalendarMonth() {
        if (null != newEndDate) {
            return newEndDate;
        }
        return null == contract ? null : contract.getEndDate();
    }

    public void setEndCalendarMonth(Date endCalendarMonth) {
        /* Lo escribe el calendario al navegar de mes; no hay nada que guardar. */
    }

    public boolean isSelected() {
        return null != contract;
    }

    public List<ContractMovement> getMovements() {
        return null == contract ? new ArrayList<ContractMovement>()
                : contractConditionService.findMovements(contract);
    }

    // ------------------------------------------------- cambio de condicion

    @Restrict("#{s:hasPermission('CONTRACTCONDITION','UPDATE')}")
    public void changeCondition() {
        if (!validateChange()) {
            return;
        }
        try {
            contractConditionService.changeCondition(contract, newContractMode, newDuration,
                    newEndDate, changeDate, changeReason);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "ContractCondition.info.changed",
                    contract.getEmployee().getFullName());
            contract = null;
            refreshList();
        } catch (Exception e) {
            log.error("No se pudo cambiar la condicion del contrato", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.changeFailed");
        }
    }

    private boolean validateChange() {
        if (null == contract) {
            return false;
        }
        if (null == changeDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.dateRequired");
            return false;
        }
        if (null == changeReason || changeReason.trim().length() == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.reasonRequired");
            return false;
        }
        /* Un plazo fijo sin fecha de fin no es un plazo fijo. */
        if (ContractDuration.FIXED_TERM.equals(newDuration) && null == newEndDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.endDateRequired");
            return false;
        }
        if (ContractDuration.FIXED_TERM.equals(newDuration)
                && null != contract.getInitDate() && !newEndDate.after(contract.getInitDate())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.endBeforeInit");
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------- baja

    @Restrict("#{s:hasPermission('CONTRACTCONDITION','UPDATE')}")
    public void terminate() {
        if (!validateTermination()) {
            return;
        }
        try {
            contractConditionService.terminate(contract, departureDate, terminationReason);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "ContractCondition.info.terminated",
                    contract.getEmployee().getFullName());
            contract = null;
            refreshList();
        } catch (Exception e) {
            log.error("No se pudo registrar la baja del contrato", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.terminateFailed");
        }
    }

    /**
     * El listado no se entera solo. El reRender redibuja lo que el modelo tiene guardado, asi
     * que sin esto la fila seguia mostrando el contrato como estaba antes hasta que alguien
     * apretaba Buscar. Se busca sin crearlo: si la pantalla no esta abierta, no hay nada que
     * refrescar.
     */
    private void refreshList() {
        ContractConditionDataModel dataModel = (ContractConditionDataModel)
                Component.getInstance("contractConditionDataModel", false);
        if (null != dataModel) {
            dataModel.refresh();
        }
    }

    private boolean validateTermination() {
        if (null == contract) {
            return false;
        }
        if (null == departureDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.dateRequired");
            return false;
        }
        if (null == terminationReason || terminationReason.trim().length() == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.reasonRequired");
            return false;
        }
        if (null != contract.getInitDate() && departureDate.before(contract.getInitDate())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ContractCondition.error.departureBeforeInit");
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------- getters

    public Contract getContract() {
        return contract;
    }

    public ContractMode getNewContractMode() {
        return newContractMode;
    }

    public void setNewContractMode(ContractMode newContractMode) {
        this.newContractMode = newContractMode;
    }

    public ContractDuration getNewDuration() {
        return newDuration;
    }

    public void setNewDuration(ContractDuration newDuration) {
        this.newDuration = newDuration;
    }

    public boolean isFixedTermSelected() {
        return ContractDuration.FIXED_TERM.equals(newDuration);
    }

    public Date getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(Date newEndDate) {
        this.newEndDate = newEndDate;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public ContractDuration[] getDurations() {
        return ContractDuration.values();
    }
}
