package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@NamedQueries({
        @NamedQuery(name = "MovementDetail.findByState",
                query = "select movementDetail from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber and movementDetail.state =:state and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.findProductItems",
                query = "select distinct(movementDetail.productItem) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber and movementDetail.state =:state and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumQuantitiesByProductItem",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber and movementDetail.state =:state and movementDetail.productItem =:productItem and movementDetail.warehouseCode =:warehouseCode and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.findByTransactionNumber",
                query = "select movementDetail from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber"),
        @NamedQuery(name = "MovementDetail.findBySourceMovementDetail",
                query = "select movementDetail from MovementDetail movementDetail where movementDetail.sourceId =:sourceMovementDetail"),
        @NamedQuery(name = "MovementDetail.findByGroup",
                query = "select movementDetail from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber and movementDetail.productItem.subGroup.group =:group and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.findByMovementDetailType",
                query = "select movementDetail from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber and movementDetail.movementType =:movementType"),
        @NamedQuery(name = "MovementDetail.findByWarehouseVoucher",
                query = "select movementDetail from MovementDetail movementDetail " +
                        "where movementDetail.companyNumber =:companyNumber " +
                        "and movementDetail.transactionNumber =:transactionNumber "),
        @NamedQuery(name = "MovementDetail.findProductCodeByVoucher",
                query = "select distinct(productItem.productItemCode) from MovementDetail movementDetail left join movementDetail.productItem productItem  where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber order by productItem.productItemCode"),
        @NamedQuery(name = "MovementDetail.deleteByTransactionNumber",
                query = "delete from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber"),
        @NamedQuery(name = "MovementDetail.deleteTargetByTransactionNumber",
                query = "delete from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.transactionNumber =:transactionNumber and movementDetail.sourceId is not null"),
        @NamedQuery(name = "MovementDetail.sumQuantityByProductItemStateDateType",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.state =:state and movementDetail.movementDetailDate <:movementDetailDate and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumQuantityByProductItemWarehouseStateTypeInBeforeDates",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.warehouseCode =:warehouseCode and movementDetail.state =:state and movementDetail.movementDetailDate <:movementDetailDate and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumQuantityByProductItemWarehouseStateTypeInRangeDate",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.warehouseCode =:warehouseCode and movementDetail.state =:state and movementDetail.movementDetailDate >=:initDate and movementDetail.movementDetailDate <=:endDate and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumAmountByProductItemStateDateType",
                query = "select sum(movementDetail.amount) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.state =:state and movementDetail.movementDetailDate <:movementDetailDate and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumAmountByProductItemWarehouseStateTypeInBeforeDates",
                query = "select sum(movementDetail.amount) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.warehouseCode =:warehouseCode and movementDetail.state =:state and movementDetail.movementDetailDate <:movementDetailDate and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumAmountByProductItemWarehouseStateTypeInRangeDate",
                query = "select sum(movementDetail.amount) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.warehouseCode =:warehouseCode and movementDetail.state =:state and movementDetail.movementDetailDate >=:initDate and movementDetail.movementDetailDate <=:endDate and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.sumAmountByStateTransactionNumberSourceIdNull",
                query = "select sum(movementDetail.amount) from MovementDetail movementDetail where movementDetail.sourceId is null and movementDetail.companyNumber =:companyNumber and movementDetail.state =:state and movementDetail.transactionNumber =:transactionNumber"),
        @NamedQuery(name = "MovementDetail.sumMovementsQuantity",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.state =:state and movementDetail.movementType =:movementDetailType and movementDetail.warehouseCode =:warehouseCode"),
        @NamedQuery(name = "MovementDetail.sumMovementsQuantityUpToDate",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.state =:state and movementDetail.movementType =:movementDetailType and movementDetail.warehouseCode =:warehouseCode and movementDetail.movementDetailDate <:endDate"),
        @NamedQuery(name = "MovementDetail.sumMovementsQuantityFromToDate",
                query = "select sum(movementDetail.quantity) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.state =:state and movementDetail.movementType =:movementDetailType and movementDetail.warehouseCode =:warehouseCode and movementDetail.movementDetailDate >=:initDate and movementDetail.movementDetailDate <=:endDate"),
        @NamedQuery(name = "MovementDetail.findLastMovement",
                query = "select max(movementDetail.id) from MovementDetail movementDetail where movementDetail.companyNumber =:companyNumber and movementDetail.productItemCode =:productItemCode and movementDetail.warehouseCode =:warehouseCode and movementDetail.state =:state"),
        @NamedQuery(name = "MovementDetail.sumAmountByVoucher",
                query = "select movementDetail from MovementDetail movementDetail" +
                        " left join movementDetail.inventoryMovement inventoryMovement" +
                        " left join inventoryMovement.warehouseVoucher warehouseVoucher" +
                        " where movementDetail.companyNumber =:companyNumber and warehouseVoucher=:warehouseVoucher and movementDetail.movementType =:movementDetailType"),
        @NamedQuery(name = "MovementDetail.changeMovementDetailOverMaximumWarning",
                query = "update MovementDetail movementDetail " +
                        "set movementDetail.warning=:overMaximumMessage " +
                        "where movementDetail.state=:pendingState " +
                        "and movementDetail.productItem in (:productItemList) " +
                        "and movementDetail.productItemCode in (   select productItem.productItemCode " +
                        "               from ProductItem productItem " +
                        "               where productItem.maximumStock is not null " +
                        "               and (productItem.maximumStock - (case when movementDetail.movementType=:inputType then movementDetail.quantity else (movementDetail.quantity *(-1)) end)) <(  " +
                        "                                               select (inventory.unitaryBalance) " +
                        "                                               from Inventory inventory " +
                        "                                               where inventory.productItem=movementDetail.productItem and inventory.warehouse= movementDetail.warehouse" +
                        "                                             )" +
                        "           ) "),
        @NamedQuery(name = "MovementDetail.changeMovementDetailUnderMinimalWarning",
                query = "update MovementDetail movementDetail set movementDetail.warning=:underMinimalMessage " +
                        "where movementDetail.state=:pendingState " +
                        "and movementDetail.productItem in (:productItemList) " +
                        "and movementDetail.productItemCode in (   select productItem.productItemCode " +
                        "               from ProductItem productItem " +
                        "               where productItem.minimalStock is not null " +
                        "               and (productItem.minimalStock - (case when movementDetail.movementType=:inputType then movementDetail.quantity else (movementDetail.quantity *(-1)) end)) >(  " +
                        "                                               select (inventory.unitaryBalance) " +
                        "                                               from Inventory inventory " +
                        "                                               where inventory.productItem=movementDetail.productItem and inventory.warehouse= movementDetail.warehouse" +
                        "                                             )" +
                        "           ) "),
        @NamedQuery(name = "MovementDetail.changeMovementDetailIdealWarning",
                query = "update MovementDetail movementDetail set movementDetail.warning=:idealMessage " +
                        "where movementDetail.state=:pendingState " +
                        "and movementDetail.productItem in (:productItemList) " +
                        "and (( movementDetail.productItemCode not in (   select productItem.productItemCode " +
                        "               from ProductItem productItem " +
                        "               where productItem.maximumStock is not null " +
                        "               and (productItem.maximumStock - (case when movementDetail.movementType=:inputType then movementDetail.quantity else (movementDetail.quantity *(-1)) end)) <(  " +
                        "                                               select (inventory.unitaryBalance) " +
                        "                                               from Inventory inventory " +
                        "                                               where inventory.productItem=movementDetail.productItem and inventory.warehouse= movementDetail.warehouse" +
                        "                                             )" +
                        "           )) and (movementDetail.productItemCode not in (   select productItem.productItemCode " +
                        "               from ProductItem productItem " +
                        "               where productItem.minimalStock is not null " +
                        "               and (productItem.minimalStock - (case when movementDetail.movementType=:inputType then movementDetail.quantity else (movementDetail.quantity *(-1)) end)) >(  " +
                        "                                               select (inventory.unitaryBalance) " +
                        "                                               from Inventory inventory " +
                        "                                               where inventory.productItem=movementDetail.productItem and inventory.warehouse= movementDetail.warehouse" +
                        "                                             )" +
                        "           )) ) ")
})
//@SequenceGenerator(name = "MovementDetail.sequenceGenerator", sequenceName = Constants.FINANCES_SCHEMA + ".INV_MOVDET_SEQ")

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "MovementDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inv_movdet",
        initialValue = 1,
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "inv_movdet", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class MovementDetail implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MovementDetail.tableGenerator")
    @Column(name = "ID_INV_MOVDET", nullable = true)
    private Long id;

    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "NO_TRANS", nullable = true, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "ESTADO", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private WarehouseVoucherState state;

    @Column(name = "COD_ALM", nullable = true, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "COD_ART", nullable = true, length = 6)
    @Length(max = 6)
    private String productItemCode;

    @Column(name = "TIPO_MOV", nullable = true, length = 1)
    @Enumerated(EnumType.STRING)
    private MovementDetailType movementType;

    @Column(name = "CANTIDAD", nullable = true)
    private BigDecimal quantity;

    @Column(name = "RESIDUO", precision = 12, scale = 2)
    private BigDecimal residue;

    @Column(name = "CUENTA_ART", nullable = true, length = 31)
    @Length(max = 31)
    private String productItemAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA_ART", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount productItemCashAccount;

    @Column(name = "COD_MED", nullable = true, length = 6)
    @Length(max = 6)
    private String measureCode;

    @Column(name = "COSTOUNITARIO", precision = 16, scale = 6)
    private BigDecimal unitCost;

    @Column(name = "MONTO", nullable = true, precision = 16, scale = 6)
    private BigDecimal amount;

    @Column(name = "PRECIOUNITCOMPRA", precision = 16, scale = 6)
    private BigDecimal unitPurchasePrice;

    @Column(name = "PRECIOCOMPRA", precision = 16, scale = 6)
    private BigDecimal purchasePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "COD_CC", nullable = true, length = 8)
    @Length(max = 8)
    private String costCenterCode;

    @Column(name = "FECHA", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date movementDetailDate;

    @Column(name = "ID_FUENTE", nullable = true)
    private Long sourceId;

    @Version
    @Column(name = "VERSION")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ART", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ALM", referencedColumnName = "COD_ALM", nullable = false, insertable = false, updatable = false)
    })
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_MED", nullable = false, updatable = false, insertable = false)
    })
    private MeasureUnit measureUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "NO_TRANS", referencedColumnName = "NO_TRANS", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ESTADO", referencedColumnName = "ESTADO", nullable = false, updatable = false, insertable = false)
    })
    private InventoryMovement inventoryMovement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA_ART", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    @Column(name = "ADVERTENCIA", length = 250)
    @Length(max = 250)
    private String warning;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INV_MOVDET_RAIZ", referencedColumnName = "ID_INV_MOVDET")
    private MovementDetail parentMovementDetail;

    @OneToMany(mappedBy = "parentMovementDetail", fetch = FetchType.LAZY)
    private List<MovementDetail> partialMovementDetailList;

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

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public WarehouseVoucherState getState() {
        return state;
    }

    public void setState(WarehouseVoucherState state) {
        this.state = state;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public MovementDetailType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementDetailType movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getProductItemAccount() {
        return productItemAccount;
    }

    public void setProductItemAccount(String productItemAccount) {
        this.productItemAccount = productItemAccount;
    }

    public CashAccount getProductItemCashAccount() {
        return productItemCashAccount;
    }

    public void setProductItemCashAccount(CashAccount productItemCashAccount) {
        this.productItemCashAccount = productItemCashAccount;
        setProductItemAccount(productItemCashAccount != null ? productItemCashAccount.getAccountCode() : null);
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        if (null != productItem) {
            setProductItemCode(productItem.getId().getProductItemCode());
        } else {
            setProductItemCode(null);
        }
    }

    public Date getMovementDetailDate() {
        return movementDetailDate;
    }

    public void setMovementDetailDate(Date movementDetailDate) {
        this.movementDetailDate = movementDetailDate;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getMeasureCode() {
        return measureCode;
    }

    public void setMeasureCode(String measureCode) {
        this.measureCode = measureCode;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
        if (null != measureUnit) {
            setMeasureCode(measureUnit.getId().getMeasureUnitCode());
        } else {
            setMeasureCode(null);
        }
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public InventoryMovement getInventoryMovement() {
        return inventoryMovement;
    }

    public void setInventoryMovement(InventoryMovement inventoryMovement) {
        this.inventoryMovement = inventoryMovement;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getUnitPurchasePrice() {
        return unitPurchasePrice;
    }

    public void setUnitPurchasePrice(BigDecimal unitPurchasePrice) {
        this.unitPurchasePrice = unitPurchasePrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public BigDecimal getResidue() {
        return residue;
    }

    public void setResidue(BigDecimal residue) {
        this.residue = residue;
    }

    public MovementDetail getParentMovementDetail() {
        return parentMovementDetail;
    }

    public void setParentMovementDetail(MovementDetail parentMovementDetail) {
        this.parentMovementDetail = parentMovementDetail;
    }

    public List<MovementDetail> getPartialMovementDetailList() {
        return partialMovementDetailList;
    }

    public void setPartialMovementDetailList(List<MovementDetail> partialMovementDetailList) {
        this.partialMovementDetailList = partialMovementDetailList;
    }

    public boolean isApproved() {
        return isInState(WarehouseVoucherState.APR);
    }

    public boolean isPending() {
        return null == getState() || isInState(WarehouseVoucherState.PEN);
    }

    public boolean isInState(WarehouseVoucherState warehouseVoucherState) {
        return null != getState() && getState().equals(warehouseVoucherState);
    }

}
