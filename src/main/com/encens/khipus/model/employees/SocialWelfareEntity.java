package com.encens.khipus.model.employees;

/**
 * @author
 * @version 3.5
 */

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Organization;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "SocialWelfareEntity.countByIdNumber",
                query = "select count(entity.id) from SocialWelfareEntity entity where entity.id<>:entityId and lower(entity.idNumber)=lower(:idNumber)"),
        @NamedQuery(name = "SocialWelfareEntity.countByName",
                query = "select count(entity.id) from SocialWelfareEntity entity where entity.id<>:entityId and lower(entity.name)=lower(:name)")
})

@Entity
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "entidadbensocial", schema = Constants.KHIPUS_SCHEMA)
@PrimaryKeyJoinColumn(name = "identidadbensocial", referencedColumnName = "idinstitucion")
public class SocialWelfareEntity extends Organization {

    @Column(name = "no_cia", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "cod_prov", nullable = false, length = 6)
    @Length(max = 6)
    private String providerCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_prov", nullable = false, insertable = false, updatable = false)
    })
    private Provider provider;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialWelfareEntityType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(provider != null ? provider.getProviderCode() : null);
    }

    public SocialWelfareEntityType getType() {
        return type;
    }

    public void setType(SocialWelfareEntityType type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
