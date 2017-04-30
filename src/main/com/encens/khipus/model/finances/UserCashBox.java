package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity that relates User and CashBox
 *
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "UserCashBox.findByCashBox", query = "select u from UserCashBox u where u.cashBox =:cashBox and u.state =:state"),
                @NamedQuery(name = "UserCashBox.findByUser", query = "select u.cashBox from UserCashBox u where u.user =:user and u.state =:state")
        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cajausuario")
public class UserCashBox implements BaseModel {

    @EmbeddedId
    private UserCashBoxPk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario", nullable = false, insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcaja", referencedColumnName = "idcaja", nullable = false, insertable = false, updatable = false)
    private CashBox cashBox;

    @Column(name = "fechaapertura")
    @Temporal(TemporalType.TIMESTAMP)
    private Date openingDate;

    @Column(name = "fechacierre")
    @Temporal(TemporalType.TIMESTAMP)
    private Date closingDate;

    @Column(name = "estado", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private UserCashBoxState state;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UserCashBox() {
    }

    public UserCashBox(User user, CashBox cashBox) {
        this.user = user;
        this.cashBox = cashBox;
        this.id = new UserCashBoxPk(user.getId(), cashBox.getId());
    }

    public UserCashBoxPk getId() {
        return id;
    }

    public void setId(UserCashBoxPk id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CashBox getCashBox() {
        return cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }

    public UserCashBoxState getState() {
        return state;
    }

    public void setState(UserCashBoxState state) {
        this.state = state;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
