package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.warehouse.Group;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 *
 *
 * @author Diego Loza
 * @version 1.2.1
 */
@TableGenerator(name = "IndirectCostConfig.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "costosindirectosconf",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "costosindirectosconf")
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
public class IndirectCostConfig implements BaseModel {

    @Id
    @Column(name = "idcostosindirectosconf", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "IndirectCostConfig.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "cod_gru", insertable = false, updatable = false, nullable = true)
    private String groupCode;

    @Column(name = "no_cia", insertable = false, updatable = false, nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "estado")
    private String estate;

    @Column(name = "cuenta", insertable = false, updatable = false, nullable = true)
    @Length(max = 20)
    private String account;
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia",insertable = false, updatable = false),
            @JoinColumn(name = "cod_gru", referencedColumnName = "cod_gru",insertable = false, updatable = false)
    })
    private Group group;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia",insertable = false, updatable = false),
            @JoinColumn(name = "cuenta", referencedColumnName = "cuenta",insertable = false, updatable = false)
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
}
