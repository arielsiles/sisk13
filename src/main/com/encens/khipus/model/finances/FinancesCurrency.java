package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for FinancesCurrency
 *
 * @author
 * @version 2.3
 */
@NamedQueries(
        {
                @NamedQuery(name = "FinancesCurrency.findAll", query = "select o from FinancesCurrency o order by o.id asc"),
                @NamedQuery(name = "FinancesCurrency.countFinancesCurrency", query = "select count(o.id) from FinancesCurrency o ")
        }
)

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cg_moneda", schema = Constants.FINANCES_SCHEMA)
public class FinancesCurrency implements BaseModel {
    @EmbeddedId
    private FinancesCurrencyPk id = new FinancesCurrencyPk();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASE_CAMBIO")
    private ExchangeKind exchangeKind;

    @Column(name = "DESCRI", nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String description;

    @Column(name = "ABREV", nullable = false, length = 10)
    @NotNull
    @Length(max = 10)
    private String acronym;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyState state;

    public FinancesCurrencyPk getId() {
        return id;
    }

    public void setId(FinancesCurrencyPk id) {
        this.id = id;
    }

    public ExchangeKind getExchangeKind() {
        return exchangeKind;
    }

    public void setExchangeKind(ExchangeKind exchangeKind) {
        this.exchangeKind = exchangeKind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public FinancesCurrencyState getState() {
        return state;
    }

    public void setState(FinancesCurrencyState state) {
        this.state = state;
    }

    public String getFullName() {
        return FormatUtils.toCodeName(getAcronym(), getDescription());
    }
}
