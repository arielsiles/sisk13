package com.encens.khipus.action.employees.dto;

import com.encens.khipus.model.finances.Contract;

import java.io.Serializable;

/**
 * Un candidato de la asignacion masiva: un contrato y si esta tildado.
 *
 * @author
 * @version 6.1.0
 */
public class GroupCandidate implements Serializable {

    private Contract contract;
    private boolean selected;

    /* El grupo al que pertenece hoy, o vacio si esta libre. Se muestra al lado para que
       mover a alguien de otro grupo sea una decision informada y no un descubrimiento. */
    private String currentGroupName;

    public GroupCandidate(Contract contract, String currentGroupName) {
        this.contract = contract;
        this.currentGroupName = currentGroupName;
    }

    public String getCurrentGroupName() {
        return currentGroupName;
    }

    public boolean isInAnotherGroup() {
        return null != currentGroupName;
    }

    public Contract getContract() {
        return contract;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
