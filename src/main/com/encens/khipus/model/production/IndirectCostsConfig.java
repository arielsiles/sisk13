package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 *
 *
 * @author Diego Loza
 * @version 1.2.1
 */
@TableGenerator(name = "IndirectCostsConfig.tableGenerator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "COSTOSINDIRECTOSCONF",
        allocationSize = 10)

@Entity
@Table(name = "COSTOSINDIRECTOSCONF")
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
public class IndirectCostsConfig implements BaseModel {

    @Id
    @Column(name = "IDCOSTOSINDIRECTOSCONF", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "IndirectCostsConfig.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @Column(name = "COD_GRU", insertable = true, updatable = false, nullable = true)
    private String groupCode;

    @Column(name = "NO_CIA", insertable = true, updatable = false, nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "ESTADO")
    private String estate;

    @Column(name = "DESCRIPCION")
    private String description;

    @Column(name = "PREDEFINIDO")
    private Boolean predefined;

    @Column(name = "CUENTA", insertable = true, updatable = false, nullable = true)
    @Length(max = 20)
    private String account;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA",insertable = false, updatable = false),
            @JoinColumn(name = "COD_GRU", referencedColumnName = "COD_GRU",insertable = false, updatable = false)
    })
    private Group group;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA",insertable = false, updatable = false),
            @JoinColumn(name = "CUENTA", referencedColumnName = "CUENTA",insertable = false, updatable = false)
    })
    private CashAccount cashAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public String getEstate() {
        return estate;
    }

    public void setEstate(String estate) {
        this.estate = estate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPredefined() {
        return predefined;
    }

    public void setPredefined(Boolean predefined) {
        this.predefined = predefined;
    }
}
