package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
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

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_DOC", nullable = true, length = 3)
    @Length(max = 3)
    private String documentCode;

    @Column(name = "NO_VALE", nullable = true, length = 20)
    @Length(max = 20)
    private String number;

    @Column(name = "FECHA", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "ESTADO", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private WarehouseVoucherState state;

    @Column(name = "NO_TRANS_REC", nullable = true, length = 10)
    @Length(max = 10)
    private String receptionTransactionNumber;

    @Column(name = "COD_ALM", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @Column(name = "COD_ALM_DEST", nullable = true, length = 6)
    @Length(max = 6)
    private String targetWarehouseCode;

    @Column(name = "ID_COM_ENCOC", nullable = true, updatable = false, insertable = false)
    private Long purchaseOrderId;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "DEST_COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter targetCostCenter;

    @Column(name = "COD_CC", length = 8, nullable = false)
    @Length(max = 8)
    private String costCenterCode;

    @Column(name = "DEST_COD_CC", length = 8, nullable = true)
    @Length(max = 8)
    private String targetCostCenterCode;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_ALM", nullable = false, updatable = false, insertable = false)
    })
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_ALM_DEST", nullable = false, updatable = false, insertable = false)
    })
    private Warehouse targetWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_DOC", nullable = false, updatable = false, insertable = false)
    })
    private WarehouseDocumentType documentType;


    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "ID_COM_ENCOC", nullable = true, updatable = false, insertable = true)
    })
    private PurchaseOrder purchaseOrder;

    @Column(name = "CONTRACUENTA", length = 20)
    @Length(max = 20)
    private String contraAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CONTRACUENTA", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount contraAccount;

    @Version
    @Column(name = "VERSION")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_RESPONSABLE", nullable = true)
    private Employee responsible;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ID_RESPONSABLE_DEST", nullable = true)
    private Employee targetResponsible;

    @com.encens.khipus.validator.BusinessUnit
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit executorUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IDUNIDADNEGOCIO_DEST", updatable = true, insertable = true)
    private BusinessUnit targetExecutorUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCONTRATOPUESTOSOL", referencedColumnName = "idcontratopuesto")
    private JobContract petitionerJobContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA_RAIZ", referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "NO_TRANS_RAIZ", referencedColumnName = "NO_TRANS")
    })
    private WarehouseVoucher parentWarehouseVoucher;

    @OneToMany(mappedBy = "parentWarehouseVoucher", fetch = FetchType.LAZY)
    private List<WarehouseVoucher> partialWarehouseVoucherList;

    @Column(name = "TIPORECEPCION", length = 2)
    @Enumerated(EnumType.STRING)
    private WarehouseVoucherReceptionType warehouseVoucherReceptionType;

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

    public boolean isInput() {
        return null != documentType && WarehouseVoucherType.E.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isOutput() {
        return null != documentType && WarehouseVoucherType.S.equals(documentType.getWarehouseVoucherType());
    }

    public boolean isExecutorUnitTransfer() {
        return null != documentType && WarehouseVoucherType.M.equals(documentType.getWarehouseVoucherType());
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
}
