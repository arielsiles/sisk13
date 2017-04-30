package com.encens.khipus.model.employees;

import java.util.ArrayList;
import java.util.List;

/**
 * PayrollGenerationType
 *
 * @author
 * @version 1.4
 */
public enum PayrollGenerationType {
    GENERATION_BY_SALARY("PayrollGenerationType.bySalary"),
    GENERATION_BY_TIME("PayrollGenerationType.byTime"),
    GENERATION_BY_PERIODSALARY("PayrollGenerationType.byPeriodSalary");
    private String resourceKey;

    PayrollGenerationType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<PayrollGenerationType> getSalaryAndPeriodSalaryValues() {
        List<PayrollGenerationType> types = new ArrayList<PayrollGenerationType>(2);
        types.add(GENERATION_BY_SALARY);
        types.add(GENERATION_BY_PERIODSALARY);
        return types;

    }
}
