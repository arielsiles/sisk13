package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Customer categories
 *
 * @author:
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "TypePresetAccountingTemplate.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipoplantillacontablepredefinida",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "tipoplantillacontablepredefinida", uniqueConstraints = @UniqueConstraint(columnNames = {"idtipoplantillacontablepredefinida", "cuenta"}))
public class TypePresetAccountingTemplate implements BaseModel {

    @Id
    @Column(name = "idtipoplantillacontablepredefinida", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TypePresetAccountingTemplate.tableGenerator")
    private Long id;

    @Column(name = "cuenta", insertable = false, updatable = false)
    private String accountCode;

    @Column(name = "no_cia", insertable = true, updatable = true)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cuenta", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplantillacontablepredefinida", nullable = false, updatable = false, insertable = true)
    private PresetAccountingTemplate presetAccountingTemplate;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }


    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public PresetAccountingTemplate getPresetAccountingTemplate() {
        return presetAccountingTemplate;
    }

    public void setPresetAccountingTemplate(PresetAccountingTemplate presetAccountingTemplate) {
        this.presetAccountingTemplate = presetAccountingTemplate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
