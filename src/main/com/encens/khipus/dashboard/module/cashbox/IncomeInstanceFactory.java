package com.encens.khipus.dashboard.module.cashbox;

import com.encens.khipus.dashboard.component.factory.InstanceFactory;

import java.math.BigDecimal;

/**
 * @author
 * @version 2.7
 */
public class IncomeInstanceFactory implements InstanceFactory<Income> {
    public Income createInstance(Income cachedObject, Object[] row) {
        Income income = new Income();

        income.setMonth(Integer.valueOf(row[0].toString()));
        income.setYear(Integer.valueOf(row[1].toString()));
        income.setMonthName((String) row[2]);
        income.setBsAmount((BigDecimal) row[3]);
        income.setUsdAmount((BigDecimal) row[4]);
        income.setTotalAmount((BigDecimal) row[5]);
        income.setExchangeRate((BigDecimal) row[6]);

        return income;
    }

    public Object getIdentifierValue(Object[] row) {
        return row[0];
    }
}
