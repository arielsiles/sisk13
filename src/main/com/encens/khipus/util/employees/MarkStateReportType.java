package com.encens.khipus.util.employees;

import com.encens.khipus.model.employees.HoraryBandStateType;
import com.encens.khipus.model.employees.MarkStateType;

/**
 * @author
 * @version 3.3
 */
public enum MarkStateReportType {
    PENDING("MarkStateReportType.PENDING", HoraryBandStateType.PENDING, null),
    MISSING("MarkStateReportType.MISSING", HoraryBandStateType.MISSING, null),
    LATE("MarkStateReportType.LATE", null, MarkStateType.LATE),
    ON_TIME("MarkStateReportType.ON_TIME", HoraryBandStateType.ON_TIME, null);
    private String resourceKey;
    private HoraryBandStateType horaryBandStateType;
    private MarkStateType markStateType;

    MarkStateReportType(String resourceKey, HoraryBandStateType horaryBandStateType, MarkStateType markStateType) {
        this.resourceKey = resourceKey;
        this.horaryBandStateType = horaryBandStateType;
        this.markStateType = markStateType;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public HoraryBandStateType getHoraryBandStateType() {
        return horaryBandStateType;
    }

    public void setHoraryBandStateType(HoraryBandStateType horaryBandStateType) {
        this.horaryBandStateType = horaryBandStateType;
    }

    public MarkStateType getMarkStateType() {
        return markStateType;
    }

    public void setMarkStateType(MarkStateType markStateType) {
        this.markStateType = markStateType;
    }
}
