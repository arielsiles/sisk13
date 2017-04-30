package com.encens.khipus.model.employees;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public interface FiscalInternalGeneralPayroll {
    BigDecimal getAbsenceMinutesDiscount();

    BigDecimal getTardinessMinutesDiscount();

    BigDecimal getLoanDiscount();

    BigDecimal getAdvanceDiscount();

    BigDecimal getWinDiscount();

    BigDecimal getOtherDiscounts();

    BigDecimal getWorkedDays();

    PayrollGenerationType getPayrollGenerationType();
}
