package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */

@NamedQueries({
        @NamedQuery(name = "InventoryMovement.findByState",
                query = "select movement " +
                        "from InventoryMovement movement " +
                        "where movement.id.state =:state and movement.id.transactionNumber =:transactionNumber")
})


@Entity
@Table(name = "inv_mov", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class InventoryMovement implements BaseModel {
    @EmbeddedId
    private InventoryMovementPK id = new InventoryMovementPK();

    @Column(name = "FECHA_MOV", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date movementDate;

    @Column(name = "DESCRI", nullable = true, length = 250)
    @Length(max = 250)
    private String description;

    @Column(name = "TIPO_COMPRO", nullable = true, length = 2)
    @Length(max = 2)
    private String voucherType;

    @Column(name = "NO_COMPRO", nullable = true, length = 10)
    @Length(max = 10)
    private String voucherNumber;

    @Column(name = "NO_USR", nullable = false, length = 4)
    @Length(max = 4)
    @NotNull
    private String userNumber;

    @Column(name = "FECHA_CRE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Version
    @Column(name = "VERSION")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "NO_TRANS", nullable = false, insertable = false, updatable = false)
    })
    private WarehouseVoucher warehouseVoucher;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "inventoryMovement")
    private List<MovementDetail> movementDetailList = new ArrayList<MovementDetail>(0);

    public InventoryMovementPK getId() {
        return id;
    }

    public void setId(InventoryMovementPK id) {
        this.id = id;
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public WarehouseVoucher getWarehouseVoucher() {
        return warehouseVoucher;
    }

    public void setWarehouseVoucher(WarehouseVoucher warehouseVoucher) {
        this.warehouseVoucher = warehouseVoucher;
    }

    public List<MovementDetail> getMovementDetailList() {
        return movementDetailList;
    }

    public void setMovementDetailList(List<MovementDetail> movementDetailList) {
        this.movementDetailList = movementDetailList;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
