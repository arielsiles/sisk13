package com.encens.khipus.util.employees;

import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.ObservableMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * AccountingRecordMap
 *
 * @author
 * @version 2.1
 */
public class AccountingRecordMap<K extends String, V extends AccountingRecordData> extends HashMap<K, V> implements Observer, AccountingRecordDataProvider {

    private String observerId;
    private BigDecimal nationalTotalAmount = BigDecimal.ZERO;
    private BigDecimal foreignTotalAmount = BigDecimal.ZERO;

    public AccountingRecordMap(String observerId) {
        this.observerId = observerId;
    }

    public V get(Object key) {
        V data = super.get(key);
        if (data == null) {
            super.put((K) key, data = (V) new AccountingRecordData(this));
        }
        return data;
    }

    public void update(Observable obs, Object arg) {
        if (arg instanceof ObservableMap.ObserverEntry) {
            ObservableMap.ObserverEntry observerEntry = (ObservableMap.ObserverEntry) arg;
            BigDecimal currentAmount = BigDecimalUtil.toBigDecimal(observerEntry.getValue());
            if (AccountingRecordData.Property.EXCHANGE_RATE.equals(observerEntry.getObserverValueId())) {
                get(observerEntry.getKey()).setExchangeRate(currentAmount);
            } else if (AccountingRecordData.Property.AMOUNT.equals(observerEntry.getObserverValueId())) {
                get(observerEntry.getKey()).setAmount(currentAmount);
            }
        }
    }

    public String getObserverId() {
        return observerId;
    }

    public void setObserverId(String observerId) {
        this.observerId = observerId;
    }

    public boolean contentEqualsTo(Object o) {
        return super.equals(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AccountingRecordMap that = (AccountingRecordMap) o;

        if (observerId != null ? !observerId.equals(that.observerId) : that.observerId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (observerId != null ? observerId.hashCode() : 0);
        return result;
    }

    public void calculeTotalAmounts() {
        BigDecimal nationalTotalAmountTemp = BigDecimal.ZERO;
        BigDecimal foreignTotalAmountTemp = BigDecimal.ZERO;

        for (AccountingRecordData data : values()) {
            Boolean empty = data.isEmpty();
            if (!empty) {
                nationalTotalAmountTemp = nationalTotalAmountTemp.add(FinancesCurrencyType.P.equals(data.getCurrency()) ? data.getAmount() : FinancesCurrencyType.P.equals(data.getEquivalentCurrency()) ? data.getEquivalentAmount() : BigDecimal.ZERO);
                foreignTotalAmountTemp = foreignTotalAmountTemp.add(FinancesCurrencyType.D.equals(data.getCurrency()) ? data.getAmount() : FinancesCurrencyType.D.equals(data.getEquivalentCurrency()) ? data.getEquivalentAmount() : BigDecimal.ZERO);
            }
        }
        setNationalTotalAmount(nationalTotalAmountTemp);
        setForeignTotalAmount(foreignTotalAmountTemp);
    }

    public BigDecimal getNationalTotalAmount() {
        return nationalTotalAmount;
    }

    public void setNationalTotalAmount(BigDecimal nationalTotalAmount) {
        this.nationalTotalAmount = nationalTotalAmount;
    }

    public BigDecimal getForeignTotalAmount() {
        return foreignTotalAmount;
    }

    public void setForeignTotalAmount(BigDecimal foreignTotalAmount) {
        this.foreignTotalAmount = foreignTotalAmount;
    }
}
