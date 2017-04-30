package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * @author
 * @version 1.0
 */

@NamedQueries({
        @NamedQuery(name = "ModuleProvider.findByProvider",
                query = "select moduleProvider " +
                        "from ModuleProvider moduleProvider " +
                        "where moduleProvider.provider = :provider"),
        @NamedQuery(name = "ModuleProvider.findByProviderAndType",
                query = "select moduleProvider " +
                        "from ModuleProvider moduleProvider " +
                        "where moduleProvider.provider = :provider " +
                        "and moduleProvider.moduleProviderType = :moduleProviderType")
})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ModuleProvider.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "moduloprov",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@EntityListeners({CompanyNumberListener.class})
@Table(schema = com.encens.khipus.util.Constants.FINANCES_SCHEMA, name = "moduloprov")
public class ModuleProvider implements BaseModel {
    @Id
    @Column(name = "IDMODULOPROV", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ModuleProvider.tableGenerator")
    private Long id;

    @Column(name = "NO_CIA", length = 2, nullable = false)
    private String companyNumber;

    @Column(name = "COD_PROV", length = 6, nullable = false)
    @Length(max = 6)
    @NotNull
    private String providerCode;

    @Column(name = "MODULO")
    @Enumerated(EnumType.STRING)
    private ModuleProviderType moduleProviderType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_PROV", referencedColumnName = "COD_PROV", nullable = false, updatable = false, insertable = false)
    })
    private Provider provider;

    public ModuleProvider() {
    }

    public ModuleProvider(ModuleProviderType moduleProviderType) {
        this.moduleProviderType = moduleProviderType;
    }

    public ModuleProvider(String providerCode, ModuleProviderType moduleProviderType) {
        this.providerCode = providerCode;
        this.moduleProviderType = moduleProviderType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public ModuleProviderType getModuleProviderType() {
        return moduleProviderType;
    }

    public void setModuleProviderType(ModuleProviderType moduleProviderType) {
        this.moduleProviderType = moduleProviderType;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
        setProviderCode(this.provider != null && this.provider.getId() != null ? this.provider.getId().getProviderCode() : null);
    }
}
