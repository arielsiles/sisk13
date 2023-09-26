package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.0
 */

@NamedQueries({
        @NamedQuery(name = "WarehouseVoucher.countByWarehouseCode",
                query = "select count(*) from WarehouseVoucher warehouseVoucher where warehouseVoucher.warehouse =:warehouse and warehouseVoucher.state in (:states)"),
        @NamedQuery(name = "WarehouseVoucher.findByPK",
                query = "select warehouseVoucher from WarehouseVoucher warehouseVoucher where warehouseVoucher.id =:pk"),
        @NamedQuery(name = "WarehouseVoucher.findByState",
                query = "select warehouseVoucher from WarehouseVoucher warehouseVoucher where warehouseVoucher.id.companyNumber =:companyNumber and warehouseVoucher.state =:state and warehouseVoucher.date <=:endDate and warehouseVoucher.date >=:startDate"),
        @NamedQuery(name = "WarehouseVoucher.findByNumber", query = "select w from WarehouseVoucher w where w.number =:number"),
        @NamedQuery(name = "WarehouseVoucher.updateStateByPartialDetails",
                query = "update WarehouseVoucher warehouseVoucher " +
                        "set warehouseVoucher.state=:approvedState " +
                        "where warehouseVoucher=:parentWarehouseVoucher " +
                        "and 1=(select distinct count(detail.state) from MovementDetail detail where detail in (:parentMovementDetailList)) " +
                        "and (select count(detail.state) from MovementDetail detail where detail in (:parentMovementDetailList) and detail.state=:approvedState)>0 "
        )
})

@Entity
@Table(name = "inv_vales", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.BUSINESS_UNIT_FILTER_NAME)
public class WarehouseVoucher implements BaseModel {
    @EmbeddedId
    private WarehouseVoucherPK id = new WarehouseVoucherPK();

    @Column(name = "no_cia", nullable = false, updatable = false, insertable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "no_trans", nullable = false, updatable = false, insertable = false, length = 10)
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "cod_doc", nullable = true, length = 3)
    @Length(max = 3)
    private String documentCode;

    @Column(name = "no_vale", nullable = true, length = 20)
    @Length(max = 20)
    private String number;

    @Column(name = "fecha", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "estado", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private WarehouseVoucherState state;

    @Column(name = "oper", nullable = true)
    @Enumerated(EnumType.STRING)
    private VoucherOperation operation;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "dest", referencedColumnName = "no_trans", updatable = false, insertable = false)
    })
    private WarehouseVoucher destiny;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "orig", referencedColumnName = "no_trans", updatable = false, insertable = false)
    })
    private WarehouseVoucher origin;

    @Column(name = "dest")
    private String notransDestiny;

    @Column(name = "orig")
    private String notransOrigin;

    @Column(name = "no_trans_rec", nullable = true, length = 10)
    @Length(max = 10)
    private String receptionTransactionNumber;

    @Column(name = "cod_alm", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "cod_alm_dest", nullable = true, length = 6)
    @Length(max = 6)
    private String targetWarehouseCode;

    @Column(name = "id_com_encoc", nullable = true, updatable = false, insertable = false)
    private Long purchaseOrderId;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "warehouseVoucher")
    private List<InventoryMovement> inventoryMovementList = new ArrayList<InventoryMovement>(0);

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "dest_cod_cc", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter targetCostCenter;

    @Column(name = "cod_cc", length = 8, nullable = false)
    @Length(max = 8)
    private String costCenterCode;

    @Column(name = "dest_cod_cc", length = 8, nullable = true)
    @Length(max = 8)
    private String targetCostCenterCode;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_alm", nullable = false, updatable = false, insertable = false)
    })
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_alm_dest", nullable = false, updatable = false, insertable = false)
    })
    private Warehouse targetWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_doc", nullable = false, updatable = false, insertable = false)
    })
    private WarehouseDocumentType documentType;


    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "id_com_encoc", nullable = true, updatable = false, insertable = true)
    })
    private PurchaseOrder purchaseOrder;

    @Column(name = "contracuenta", length = 20)
    @Length(max = 20)
    private String contraAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", insertable = false, updatable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "contracuenta", insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount contraAccount;

    @Version
    @Column(name = "version")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_responsable", nullable = true)
    private Employee responsible;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_responsable_dest", nullable = true)
    private Employee targetResponsible;

    @com.encens.khipus.validator.BusinessUnit
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idunidadnegocio", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idunidadnegocio_dest", updatable = true, insertable = true)
    private BusinessUnit targetExecutorUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontratopuestosol", referencedColumnName = "idcontratopuesto")
    private JobContract petitionerJobContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia_raiz", referencedColumnName = "no_cia"),
            @JoinColumn(name = "no_trans_raiz", referencedColumnName = "no_trans")
    })
    private WarehouseVoucher parentWarehouseVoucher;

    @OneToMany(mappedBy = "parentWarehouseVoucher", fetch = FetchType.LAZY)
    private List<WarehouseVoucher> partialWarehouseVoucherList;

    @Column(name = "tiporecepcion", length = 2)
    @Enumerated(EnumType.STRING)
    private WarehouseVoucherReceptionType warehouseVoucherReceptionType;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idordenproduccion", nullable = true, updatable = false, insertable = true)
    private ProductionOrder productionOrder;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idproductobase", nullable = true, updatable = false, insertable = true)
    private BaseProduct baseProduct;

    @ManyToOne
    @JoinColumn(name = "idtmpenc", nullable = true)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpedidostr", nullable = true)
    private CustomerOrder transferCustomerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddestino", nullable = true)
    private Destination destination;

    public WarehouseVoucherPK getId() {
        return id;
    }

    public void setId(WarehouseVoucherPK id) {
        this.id = id;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public WarehouseVoucherState getState() {
        return state;
    }

    public void setState(WarehouseVoucherState state) {
        this.state = state;
    }

    public String getReceptionTransactionNumber() {
        return receptionTransactionNumber;
    }

    public void setReceptionTransactionNumber(String receptionTransactionNumber) {
        this.receptionTransactionNumber = receptionTransactionNumber;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public WarehouseDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(WarehouseDocumentType documentType) {
        this.documentType = documentType;

        if (null != documentType) {
            setDocumentCode(documentType.getId().getDocumentCode());

            if (documentType.getWarehouseVoucherType().equals(WarehouseVoucherType.B))
                setOperation(VoucherOperation.BA);
            if (documentType.getWarehouseVoucherType().equals(WarehouseVoucherType.D))
                setOperation(VoucherOperation.DE);

        } else {
            setDocumentCode(null);
        }
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;

        if (null != warehouse) {
            setWarehouseCode(warehouse.getId().getWarehouseCode());
        } else {
            setWarehouseCode(null);
        }
    }

    public String getTargetWarehouseCode() {
        return targetWarehouseCode;
    }

    public void setTargetWarehouseCode(String targetWarehouseCode) {
        this.targetWarehouseCode = targetWarehouseCode;
    }

    public Warehouse getTargetWarehouse() {
        return targetWarehouse;
    }

    public void setTargetWarehouse(Warehouse targetWarehouse) {
        this.targetWarehouse = targetWarehouse;

        if (null != targetWarehouse) {
            setTargetWarehouseCode(targetWarehouse.getId().getWarehouseCode());
        } else {
            setTargetWarehouseCode(null);
        }
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public boolean hasPurchaseOrder(){
        return (getPurchaseOrder() != null);
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getContraAccountCode() {
        return contraAccountCode;
    }

    public void setContraAccountCode(String contraAccountCode) {
        this.contraAccountCode = contraAccountCode;
    }

    public CashAccount getContraAccount() {
        return contraAccount;
    }

    public void setContraAccount(CashAccount contraAccount) {
        this.contraAccount = contraAccount;
        setContraAccountCode(contraAccount != null ? contraAccount.getAccountCode() : null);
    }

    public boolean isConsumption() {
        return null != documentType && WarehouseVoucherType.C.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isDevolution() {
        return null != documentType && WarehouseVoucherType.D.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isTransfer() {
        return null != documentType && WarehouseVoucherType.T.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isReception() {
        return null != documentType && WarehouseVoucherType.R.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isReceptionByTransfer() {
        return null != documentType && WarehouseVoucherType.RT.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isInput() {
        return null != documentType && WarehouseVoucherType.E.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isOutput() {
        return null != documentType && WarehouseVoucherType.S.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isLow() {
        return null != documentType && WarehouseVoucherType.B.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isRework() {
        return null != documentType && WarehouseVoucherType.W.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isExecutorUnitTransfer() {
        return null != documentType && WarehouseVoucherType.M.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isTransferProduct() {
        return null != documentType && WarehouseVoucherType.P.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isRelatedWithPurchaseOrder() {
        return null != purchaseOrderId;
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

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public Employee getResponsible() {
        return responsible;
    }

    public void setResponsible(Employee responsible) {
        this.responsible = responsible;
    }

    public Employee getTargetResponsible() {
        return targetResponsible;
    }

    public void setTargetResponsible(Employee targetResponsible) {
        this.targetResponsible = targetResponsible;
    }

    public CostCenter getTargetCostCenter() {
        return targetCostCenter;
    }

    public void setTargetCostCenter(CostCenter targetCostCenter) {
        this.targetCostCenter = targetCostCenter;
        if (null != targetCostCenter) {
            setTargetCostCenterCode(targetCostCenter.getCode());
        } else {
            setTargetCostCenterCode(null);
        }
    }

    public String getTargetCostCenterCode() {
        return targetCostCenterCode;
    }

    public void setTargetCostCenterCode(String targetCostCenterCode) {
        this.targetCostCenterCode = targetCostCenterCode;
    }

    public BusinessUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(BusinessUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public BusinessUnit getTargetExecutorUnit() {
        return targetExecutorUnit;
    }

    public void setTargetExecutorUnit(BusinessUnit targetExecutorUnit) {
        this.targetExecutorUnit = targetExecutorUnit;
    }

    public JobContract getPetitionerJobContract() {
        return petitionerJobContract;
    }

    public void setPetitionerJobContract(JobContract petitionerJobContract) {
        this.petitionerJobContract = petitionerJobContract;
    }

    public WarehouseVoucher getParentWarehouseVoucher() {
        return parentWarehouseVoucher;
    }

    public void setParentWarehouseVoucher(WarehouseVoucher parentWarehouseVoucher) {
        this.parentWarehouseVoucher = parentWarehouseVoucher;
    }

    public List<WarehouseVoucher> getPartialWarehouseVoucherList() {
        return partialWarehouseVoucherList;
    }

    public void setPartialWarehouseVoucherList(List<WarehouseVoucher> partialWarehouseVoucherList) {
        this.partialWarehouseVoucherList = partialWarehouseVoucherList;
    }

    public WarehouseVoucherReceptionType getWarehouseVoucherReceptionType() {
        return warehouseVoucherReceptionType;
    }

    public void setWarehouseVoucherReceptionType(WarehouseVoucherReceptionType warehouseVoucherReceptionType) {
        this.warehouseVoucherReceptionType = warehouseVoucherReceptionType;
    }

    public boolean isPartial() {
        return isInState(WarehouseVoucherState.PAR);
    }

    public boolean isInState(WarehouseVoucherState warehouseVoucherState) {
        return null != getState() && getState().equals(warehouseVoucherState);
    }

    public ProductionOrder getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public BaseProduct getBaseProduct() {
        return baseProduct;
    }

    public void setBaseProduct(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }


    public VoucherOperation getOperation() {
        return operation;
    }

    public void setOperation(VoucherOperation operation) {
        this.operation = operation;
    }

    public List<InventoryMovement> getInventoryMovementList() {
        return inventoryMovementList;
    }

    public void setInventoryMovementList(List<InventoryMovement> inventoryMovementList) {
        this.inventoryMovementList = inventoryMovementList;
    }


    public WarehouseVoucher getDestiny() {
        return destiny;
    }

    public void setDestiny(WarehouseVoucher destiny) {
        this.destiny = destiny;
    }

    public WarehouseVoucher getOrigin() {
        return origin;
    }

    public void setOrigin(WarehouseVoucher origin) {
        this.origin = origin;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getNotransDestiny() {
        return notransDestiny;
    }

    public void setNotransDestiny(String notransDestiny) {
        this.notransDestiny = notransDestiny;
    }

    public String getNotransOrigin() {
        return notransOrigin;
    }

    public void setNotransOrigin(String notransOrigin) {
        this.notransOrigin = notransOrigin;
    }

    public String getGloss(){
        String result = getInventoryMovementList().get(0).getDescription();
        return result;
    }

    public CustomerOrder getTransferCustomerOrder() {
        return transferCustomerOrder;
    }

    public void setTransferCustomerOrder(CustomerOrder transferCustomerOrder) {
        this.transferCustomerOrder = transferCustomerOrder;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
