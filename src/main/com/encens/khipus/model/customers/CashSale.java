package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CashSale entity
 *
 * @author Ariel Siles
 * @version
 */

@NamedQueries(
        {
                @NamedQuery(name = "CashSale.findAll", query = "select c from CashSale c"),
                /** Solo ventas osby **/
                @NamedQuery(name = "CashSale.findBetweenDates",
                        query = "select c from CashSale c " +
                                "where c.date between :startDate and :endDate " +
                                "and c.state <> 'ANULADO' " +
                                "and c.user.id=5 " +
                                "and c.flagct = :flagct "),
                /** Solo ventas osby **/
                @NamedQuery(name = "CashSale.calculateCashTransferAmount",
                        query = "select sum(c.total) from CashSale c " +
                                "where c.date between :startDate and :endDate " +
                                "and c.state <> 'ANULADO' " +
                                "and c.user.id=5 " +
                                "and c.flagct = :flagct ")
        }
)

@javax.persistence.Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "ventadirecta")
public class CashSale implements BaseModel {

    @Id
    /*@GeneratedValue(strategy = GenerationType.TABLE, generator = "CashSale.tableGenerator")*/
    @Column(name = "idventadirecta")
    private Long id;

    @Column(name = "fecha_pedido")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "estado")
    private String state;

    @Column(name = "observacion")
    private String observation;

    @Column(name = "totalimporte")
    private Double total;

    @Column(name = "codigo")
    private Long code;

    @Column(name = "flagct", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean flagct = Boolean.FALSE;

    @JoinColumn(name = "idcliente", referencedColumnName = "idpersonacliente")
    @ManyToOne(optional = false)
    private Client client;

    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    @ManyToOne(optional = false)
    private User user;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getFlagct() {
        return flagct;
    }

    public void setFlagct(Boolean flagct) {
        this.flagct = flagct;
    }
}
