package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByBranchInstanceFactory implements InstanceFactory<IncomeByInvoiceByBranch> {
    public IncomeByInvoiceByBranch createInstance(IncomeByInvoiceByBranch cachedObject, Object[] row) {
        IncomeByInvoiceByBranch instance = new IncomeByInvoiceByBranch();
        instance.setCode(Integer.valueOf(row[0].toString()));
        instance.setMonth((String) row[3]);
        instance.setUniquePaymentBs((BigDecimal) row[4]);
        instance.setUniquePaymentUsd((BigDecimal) row[5]);
        instance.setHalfYearPaymentBs((BigDecimal) row[6]);
        instance.setHalfYearPaymentUsd((BigDecimal) row[7]);
        instance.setVariableIncomeBs((BigDecimal) row[8]);
        instance.setVariableIncomeUsd((BigDecimal) row[9]);
        instance.setFeeAmountBs((BigDecimal) row[10]);
        instance.setFeeAmountUsd((BigDecimal) row[11]);
        instance.setSoldProductBs((BigDecimal) row[12]);
        instance.setSoldProductUsd((BigDecimal) row[13]);
        instance.setRentalBs((BigDecimal) row[14]);
        instance.setRentalUsd((BigDecimal) row[15]);
        instance.setReserveBs((BigDecimal) row[16]);
        instance.setReserveUsd((BigDecimal) row[17]);
        instance.setBs((BigDecimal) row[18]);
        instance.setUsd((BigDecimal) row[19]);
        instance.setMainTotalUsd((BigDecimal) row[20]);
        instance.setExchangeRate((BigDecimal) row[21]);
        instance.setTotalValues();
        return instance;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
