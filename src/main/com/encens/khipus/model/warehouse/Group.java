package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "Group.findByMovementDetail",
                query = "select distinct(mvDetail.productItem.subGroup.group) from MovementDetail mvDetail where mvDetail.companyNumber =:companyNumber and mvDetail.transactionNumber =:transactionNumber"),
        @NamedQuery(name = "Group.countByCode",
                query = "select count(g.id.groupCode) " +
                        "from Group g " +
                        "where lower(g.id.groupCode)=lower(:groupCode) " +
                        "and g.id.companyNumber=:companyNumber")
})

@Entity
@Table(name = "inv_grupos", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class Group implements BaseModel {

    @EmbeddedId
    private GroupPK id = new GroupPK();

    @Column(name = "cod_gru", nullable = false, insertable = false, updatable = false)
    private String groupCode;

    @Column(name = "descri", nullable = true, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "cuenta_inv", nullable = true, length = 31)
    @Length(max = 31)
    private String inventoryAccount;

    @Column(name = "cta_costo", nullable = true, length = 31)
    @Length(max = 31)
    private String costAccount;

    @Column(name = "cta_gasto", nullable = true, length = 31)
    @Length(max = 31)
    private String expenseAccount;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cuenta_inv", referencedColumnName = "cuenta", updatable = false, insertable = false)
    })
    private CashAccount inventoryCashAccount;

    @ManyToOne(optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cta_costo", referencedColumnName = "cuenta", updatable = false, insertable = false)
    })
    private CashAccount costCashAccount;

    @ManyToOne(optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cta_gasto", referencedColumnName = "cuenta", updatable = false, insertable = false)
    })
    private CashAccount expenseCashAccount;

    @Version
    @Column(name = "version")
    private long version;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<SubGroup> subGroupList = new ArrayList<SubGroup>(0);

    public GroupPK getId() {
        return id;
    }

    public void setId(GroupPK id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInventoryAccount() {
        return inventoryAccount;
    }

    public void setInventoryAccount(String inventoryAccount) {
        this.inventoryAccount = inventoryAccount;
    }

    public CashAccount getInventoryCashAccount() {
        return inventoryCashAccount;
    }

    public void setInventoryCashAccount(CashAccount inventoryCashAccount) {
        this.inventoryCashAccount = inventoryCashAccount;
        setInventoryAccount(inventoryCashAccount != null ? inventoryCashAccount.getAccountCode() : null);
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<SubGroup> getSubGroupList() {
        return subGroupList;
    }

    public void setSubGroupList(List<SubGroup> subGroupList) {
        this.subGroupList = subGroupList;
    }


    public String getFullName() {
        return getGroupCode() + " - " + getName();
    }

    public String getCostAccount() {
        return costAccount;
    }

    public void setCostAccount(String costAccount) {
        this.costAccount = costAccount;
    }

    public CashAccount getCostCashAccount() {
        return costCashAccount;
    }

    public void setCostCashAccount(CashAccount costCashAccount) {
        this.costCashAccount = costCashAccount;
    }

    public String getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(String expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public CashAccount getExpenseCashAccount() {
        return expenseCashAccount;
    }

    public void setExpenseCashAccount(CashAccount expenseCashAccount) {
        this.expenseCashAccount = expenseCashAccount;
    }
}
