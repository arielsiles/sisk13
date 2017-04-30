package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByInvoiceByCategoryInstanceFactory implements InstanceFactory<IncomeByInvoiceByCategory> {
    public IncomeByInvoiceByCategory createInstance(IncomeByInvoiceByCategory cachedObject, Object[] row) {
        IncomeByInvoiceByCategory instance = new IncomeByInvoiceByCategory();
        instance.setCode(Integer.valueOf(row[0].toString()));
        instance.setMonth((String) row[3]);
        instance.setRequiredServiceBs((BigDecimal) row[4]);
        instance.setRequiredServiceUsd((BigDecimal) row[5]);
        instance.setOptionalServiceBs((BigDecimal) row[6]);
        instance.setOptionalServiceUsd((BigDecimal) row[7]);
        instance.setSoldProductBs((BigDecimal) row[8]);
        instance.setSoldProductUsd((BigDecimal) row[9]);
        instance.setRentalBs((BigDecimal) row[10]);
        instance.setRentalUsd((BigDecimal) row[11]);
        instance.setTotalBs((BigDecimal) row[12]);
        instance.setTotalUsd((BigDecimal) row[13]);
        instance.setFinalTotalUsd((BigDecimal) row[14]);
        instance.setExchangeRate((BigDecimal) row[15]);
        instance.setTotalValues();
        return instance;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
