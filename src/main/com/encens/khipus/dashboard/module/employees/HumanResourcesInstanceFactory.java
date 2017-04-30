package com.encens.khipus.dashboard.module.employees;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class HumanResourcesInstanceFactory implements InstanceFactory<HumanResources> {
    public HumanResources createInstance(HumanResources cachedObject, Object[] row) {
        HumanResources element = new HumanResources();
        element.setMonthNumber(Integer.valueOf(row[0].toString()));
        element.setYear(Integer.valueOf(row[1].toString()));
        element.setLocalCurrencyAmount((BigDecimal) row[2]);
        element.setExchangeCurrencyAmount((BigDecimal) row[3]);
        element.setMonthName((String) row[4]);

        return element;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
