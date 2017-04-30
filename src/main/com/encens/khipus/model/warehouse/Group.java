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

    @Column(name = "COD_GRU", nullable = false, insertable = false, updatable = false)
    private String groupCode;

    @Column(name = "DESCRI", nullable = true, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "CUENTA_INV", nullable = true, length = 31)
    @Length(max = 31)
    private String inventoryAccount;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA_INV", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount inventoryCashAccount;

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
}
