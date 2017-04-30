package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.List;

/**
 * Provider
 *
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "Provider.findById", query = "select p" +
                " from Provider p" +
                " left join fetch p.entity" +
                " left join fetch p.payableAccount" +
                " left join fetch p.providerClass" +
                " where p.id=:providerId")
})

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_proveedores", schema = Constants.FINANCES_SCHEMA)
public class Provider implements BaseModel {

    @EmbeddedId
    private ProviderPk id = new ProviderPk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "COD_PROV", nullable = false, updatable = false, insertable = false)
    private String providerCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COD_PROV", nullable = false, updatable = false, insertable = false)
    private FinancesEntity entity;

    @Column(name = "CTAXPAGAR")
    private String payableAccountCode;

    @Transient
    private String payableAccountFullName;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAXPAGAR", referencedColumnName = "CUENTA", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount payableAccount;

    @Column(name = "CLASE")
    private String providerClassCode;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CLASE", referencedColumnName = "CLASE", updatable = false, insertable = false)
    })
    private ProviderClass providerClass;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "provider")
    private List<ModuleProvider> moduleProviderList;

    public ProviderPk getId() {
        return id;
    }

    public void setId(ProviderPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public FinancesEntity getEntity() {
        return entity;
    }

    public void setEntity(FinancesEntity entity) {
        this.entity = entity;
    }

    public String getPayableAccountCode() {
        return payableAccountCode;
    }

    public void setPayableAccountCode(String payableAccountCode) {
        this.payableAccountCode = payableAccountCode;
    }

    public CashAccount getPayableAccount() {
        return payableAccount;
    }

    public void setPayableAccount(CashAccount payableAccount) {
        this.payableAccount = payableAccount;
        setPayableAccountCode(this.payableAccount != null ? this.payableAccount.getAccountCode() : null);
    }

    public String getProviderClassCode() {
        return providerClassCode;
    }

    public void setProviderClassCode(String providerClassCode) {
        this.providerClassCode = providerClassCode;
    }

    public ProviderClass getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(ProviderClass providerClass) {
        this.providerClass = providerClass;
        setProviderClassCode(this.providerClass != null ? this.providerClass.getCode() : null);
    }

    public String getFullName() {
        return getProviderCode() + " - " + getEntity().getAcronym();
    }

    public List<ModuleProvider> getModuleProviderList() {
        return moduleProviderList;
    }

    public void setModuleProviderList(List<ModuleProvider> moduleProviderList) {
        this.moduleProviderList = moduleProviderList;
    }

    public String getPayableAccountFullName() {
        if (payableAccountFullName == null && getPayableAccount() != null) {
            payableAccountFullName = getPayableAccount().getFullName();
        }
        return payableAccountFullName;
    }

    public void setPayableAccountFullName(String payableAccountFullName) {
        this.payableAccountFullName = payableAccountFullName;
    }
}
