package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * Entity for ExchangeKind
 *
 * @author
 * @version 2.3
 */
@NamedQueries(
        {
                @NamedQuery(name = "ExchangeKind.findAll", query = "select o from ExchangeKind o order by o.id asc"),
                @NamedQuery(name = "ExchangeKind.countExchangeKind", query = "select count(o.id) from ExchangeKind o ")
        }
)

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "arcgcc", schema = Constants.FINANCES_SCHEMA)
public class ExchangeKind implements BaseModel {

    @Id
    @Column(name = "CLASE_CAMBIO", nullable = false)
    @Length(max = 2)
    private String id;

    @Column(name = "DESCRIPCION")
    @Length(max = 30)
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
