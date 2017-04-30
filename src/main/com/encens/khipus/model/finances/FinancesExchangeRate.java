package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for FinancesExchangeRate
 *
 * @author
 * @version 2.3
 */
@NamedQueries(
        {
                @NamedQuery(name = "FinancesExchangeRate.findAll", query = "select o from FinancesExchangeRate o order by o.id asc"),
                @NamedQuery(name = "FinancesExchangeRate.countFinancesExchangeRate", query = "select count(o.id) from FinancesExchangeRate o "),
                @NamedQuery(name = "FinancesExchangeRate.findExchangeRateByDateByCurrency", query = "select o from FinancesExchangeRate o " +
                        "where o.exchangeKind=:exchangeKind and o.id.date=:date"),
                @NamedQuery(name = "FinancesExchangeRate.findLastDateByFinancesCurrency", query = "select max(o.id.date) from FinancesExchangeRate o " +
                        "where o.exchangeKind=:exchangeKind "),
                @NamedQuery(name = "FinancesExchangeRate.findLastFinancesExchangeRateDate4SusBs",
                        query = "select max(o1.id.date) from FinancesExchangeRate o1, FinancesExchangeRate o2 " +
                                " where o1.id.date=o2.id.date and o1.exchangeKind=:ufvExchangeKind and o2.exchangeKind=:susExchangeKind ")

        }
)

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "arcgtc", schema = Constants.FINANCES_SCHEMA)
public class FinancesExchangeRate implements BaseModel {
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "CLASE_CAMBIO", column = @Column(name = "CLASE_CAMBIO", nullable = false, insertable = true))
    })
    private FinancesExchangeRatePk id = new FinancesExchangeRatePk();

    @Column(name = "TIPO_CAMBIO", precision = 10, scale = 6)
    private BigDecimal rate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CLASE_CAMBIO", nullable = false, insertable = false, updatable = false)
    private ExchangeKind exchangeKind;

    public FinancesExchangeRatePk getId() {
        return id;
    }

    public void setId(FinancesExchangeRatePk id) {
        this.id = id;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public ExchangeKind getExchangeKind() {
        return exchangeKind;
    }

    public void setExchangeKind(ExchangeKind exchangeKind) {
        this.exchangeKind = exchangeKind;
    }
}
