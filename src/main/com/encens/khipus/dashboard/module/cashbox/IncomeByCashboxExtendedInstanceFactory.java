package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeByCashboxExtendedInstanceFactory implements InstanceFactory<IncomeByCashboxExtended> {
    public IncomeByCashboxExtended createInstance(IncomeByCashboxExtended cachedObject, Object[] row) {
        IncomeByCashboxExtended instance = new IncomeByCashboxExtended();
        instance.setCode(Integer.valueOf(row[0].toString()));
        instance.setMonth((String) row[3]);
        instance.setLaPazBs((BigDecimal) row[4]);
        instance.setLaPazUsd((BigDecimal) row[5]);
        instance.setSantaCruzBs((BigDecimal) row[6]);
        instance.setSantaCruzUsd((BigDecimal) row[7]);
        instance.setCochabambaBs((BigDecimal) row[8]);
        instance.setCochabambaUsd((BigDecimal) row[9]);
        instance.setOruroBs((BigDecimal) row[10]);
        instance.setOruroUsd((BigDecimal) row[11]);
        instance.setBs((BigDecimal) row[12]);
        instance.setUsd((BigDecimal) row[13]);
        instance.setMainTotalUsd((BigDecimal) row[14]);
        instance.setExchangeRate((BigDecimal) row[15]);
        instance.setTotalValues();
        return instance;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
