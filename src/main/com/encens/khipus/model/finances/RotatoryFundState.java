package com.encens.khipus.model.finances;

import java.util.ArrayList;
import java.util.List;

/**
 * RotatoryFundState, the values for this enumeration are
 * [PEN] pendiente, [APR] aprobado, [FIN] finalizado, [ANL] anulado
 *
 * @author
 * @version 2.14
 */
public enum RotatoryFundState {
    PEN("RotatoryFundState.PEN"),
    APR("RotatoryFundState.APR"),
    LIQ("RotatoryFundState.LIQ"),
    ANL("RotatoryFundState.ANL");

    private String resourceKey;

    RotatoryFundState(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<RotatoryFundState> availableStates() {
        List<RotatoryFundState> stateList = new ArrayList<RotatoryFundState>(2);
        stateList.add(RotatoryFundState.APR);
        stateList.add(RotatoryFundState.LIQ);
        return stateList;
    }

}