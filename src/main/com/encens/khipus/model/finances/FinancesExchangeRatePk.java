package com.encens.khipus.model.finances;

import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * FinancesExchangeRatePk embeddable class to use like Pk
 *
 * @author
 * @version 2.3
 */
@Embeddable
public class FinancesExchangeRatePk implements Serializable {

    @Column(name = "CLASE_CAMBIO", nullable = false, updatable = false)
    @Length(max = 2)
    private String exchangeKind;

    @Column(name = "FECHA", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date date = new Date();

    public FinancesExchangeRatePk() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinancesExchangeRatePk)) {
            return false;
        }

        FinancesExchangeRatePk that = (FinancesExchangeRatePk) o;

        if (date != null ? !date.equals(that.date) : that.date != null) {
            return false;
        }
        if (exchangeKind != null ? !exchangeKind.equals(that.exchangeKind) : that.exchangeKind != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = exchangeKind != null ? exchangeKind.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    public String getExchangeKind() {
        return exchangeKind;
    }

    public void setExchangeKind(String exchangeKind) {
        this.exchangeKind = exchangeKind;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
