package com.encens.khipus.util.employees;

import com.encens.khipus.model.employees.SMNRate;
import com.encens.khipus.model.employees.SeniorityBonusDetail;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.employees.payroll.structure.Calculator;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
public class SeniorityBonusDetailAmountCalculator extends Calculator<SeniorityBonusDetail> {
    private final BigDecimal smnRateNumber = new BigDecimal(3);
    private SMNRate smnRate;

    public static SeniorityBonusDetailAmountCalculator getInstance(SMNRate smnRate) {
        return new SeniorityBonusDetailAmountCalculator(smnRate);
    }

    private SeniorityBonusDetailAmountCalculator(SMNRate smnRate) {
        this.smnRate = smnRate;
    }

    @Override
    public void execute(SeniorityBonusDetail instance) {
        if (!BigDecimalUtil.isZeroOrNull(instance.getPercent()) && smnRate.getRate() != null) {
            // the operation has been calculated with the following Formula MONTO = 3 x SNM x PORCENTAJES
            BigDecimal multiplyResult = BigDecimalUtil.multiply(6, smnRateNumber, smnRate.getRate(), instance.getPercent());
            instance.setAmount(BigDecimalUtil.divide(multiplyResult, BigDecimalUtil.ONE_HUNDRED));
        } else {
            instance.setAmount(BigDecimal.ZERO);
        }
    }
}
