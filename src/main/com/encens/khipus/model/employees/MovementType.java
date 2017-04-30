package com.encens.khipus.model.employees;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Ariel
 * Date: 15-06-2010
 * Time: 06:41:19 PM
 */
public enum MovementType {

    LOAN("MovementType.loan"),
    WIN("MovementType.win"),
    ADVANCE_PAYMENT("MovementType.advancePayment"),
    OTHER_DISCOUNT("MovementType.otherDiscount"),
    AFP("MovementType.afp"),
    RCIVA("MovementType.rciva"),
    TARDINESS_MINUTES("MovementType.tardinessMinutes"),
    DISCOUNT_OUT_OF_RETENTION("MovementType.discountOutOfRetention"),
    OTHER_INCOME("MovementType.otherIncome"),
    INCOME_OUT_OF_RETENTION("MovementType.incomeOutOfRetention");

    private String resourceKey;

    MovementType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static List<MovementType> discountTypeGeneratedByPayrollGeneration() {
        List<MovementType> result = new ArrayList<MovementType>(4);
        result.add(MovementType.RCIVA);
        result.add(MovementType.TARDINESS_MINUTES);
        return result;
    }

    public static List<MovementType> discountTypeValues() {
        List<MovementType> result = new ArrayList<MovementType>(8);
        result.add(MovementType.LOAN);
        result.add(MovementType.WIN);
        result.add(MovementType.ADVANCE_PAYMENT);
        result.add(MovementType.OTHER_DISCOUNT);
        result.add(MovementType.AFP);
        result.add(MovementType.RCIVA);
        result.add(MovementType.TARDINESS_MINUTES);
        result.add(MovementType.DISCOUNT_OUT_OF_RETENTION);
        return result;
    }

    public static Boolean isAvailableValue(MovementType movementType) {
        return MovementType.WIN.equals(movementType) ||
                MovementType.OTHER_DISCOUNT.equals(movementType) ||
                MovementType.OTHER_INCOME.equals(movementType);
    }
}
