package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.customers.EconomicActivity;
import com.encens.khipus.model.customers.MeasureUnitSIN;
import com.encens.khipus.model.customers.ProductsServices;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.production.MeasurementUnit;
import com.encens.khipus.model.production.OrderMaterial;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@NamedQueries({
        @NamedQuery(name = "ProductItem.countByCode",
                query = "select count(p.id.productItemCode) " +
                        "from ProductItem p " +
                        "where lower(p.id.productItemCode)=lower(:productItemCode) " +
                        "and p.id.companyNumber=:companyNumber"),
        @NamedQuery(name = "ProductItem.findByWarehouseVoucher",
                query = "select movementDetail.productItem from MovementDetail movementDetail " +
                        "where movementDetail.inventoryMovement.warehouseVoucher=:warehouseVoucher "),
        @NamedQuery(name = "ProductItem.findInProductItemList",
                query = "select productItem from ProductItem productItem " +
                        "where productItem in (:productItemList) "),
        @NamedQuery(name = "ProductItem.findByCode", query = "select p from ProductItem p where p.productItemCode=:productItemCode"),
        @NamedQuery(name = "ProductItem.findByWarehouseCode", query = "select p from ProductItem p where p.warehouseCode=:warehouseCode")

})

@Entity
@Table(name = "inv_articulos", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class ProductItem implements BaseModel {

    @EmbeddedId
    private ProductItemPK id = new ProductItemPK();

    @Column(name = "NO_CIA", insertable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_ART", insertable = false, updatable = false)
    private String productItemCode;

    @Column(name = "CODSIN")
    private String productItemCodeSin;

    @Column(name = "COD_MEDS")
    private Integer measureUnitSin;

    @Column(name = "CODEQ", insertable = false, updatable = false)
    private String productItemCodeEq;

    @Column(name = "COD_ALM")
    private String warehouseCode;

    @Column(name = "DESCRI", nullable = true, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "NOMBRECORTO", nullable = true, length = 14)
    @Length(max = 14)
    private String nameShort;

    @Column(name = "ESTADO", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private ProductItemState state;

    @Column(name = "COD_MED", nullable = true, length = 6)
    @Length(max = 6)
    private String usageMeasureCode;

    @Column(name = "COD_MED_MAY", nullable = true, length = 6)
    @Length(max = 6)
    private String groupMeasureCode;

    @Column(name = "CANTIAD_EQUI", precision = 10, scale = 2, nullable = true)
    private BigDecimal equivalentQuantity;

    @Column(name = "CONTROL_VALORADO", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean controlValued;

    @Column(name = "CUENTA_ART", nullable = true, length = 31)
    @Length(max = 31)
    private String productItemAccount;

    @Column(name = "SALDO_MON", precision = 20, scale = 6, nullable = true)
    private BigDecimal investmentAmount;

    @Column(name = "COSTO_UNI", precision = 16, scale = 6, nullable = true)
    private BigDecimal unitCost;

    @Column(name = "PRECIO_VENTA", precision = 10, scale = 2, nullable = true)
    private BigDecimal  salePrice;

    @Column(name = "CU", precision = 16, scale = 6, nullable = true)
    private BigDecimal cu;

    @Column(name = "CT", precision = 20, scale = 6, nullable = true)
    private BigDecimal ct;

    @Column(name = "COD_GRU", nullable = false, updatable = true, insertable = true, length = 3)
    @Length(max = 3)
    private String groupCode;

    @Column(name = "COD_SUB", nullable = false, updatable = true, insertable = true, length = 3)
    @Length(max = 3)
    private String subGroupCode;


    @Column(name = "VENDIBLE", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean saleable;

    @Column(name = "fix")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean fixSale = Boolean.FALSE;

    @Column(name = "sigla", nullable = true, length = 14)
    @Length(max = 14)
    private String acronym;

    @Column(name = "pos", nullable = true)
    private Integer position;

    @Column(name = "med_pr")
    @Enumerated(EnumType.STRING)
    private MeasurementUnit basicMeasure;

    @Column(name = "cant_pr", nullable = true)
    private BigDecimal basicQuantity;

    @Version
    @Column(name = "version")
    private long version;

    @ManyToOne
    @JoinColumn(name = "idprodutoservicio", referencedColumnName = "idprodutoservicio")
    private ProductsServices productService;

    @ManyToOne
    @JoinColumn(name = "caeb", referencedColumnName = "codigo")
    private EconomicActivity economicActivity;

    @ManyToOne
    @JoinColumn(name = "cod_meds", referencedColumnName = "nro")
    private MeasureUnitSIN measureUnit;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ALM", nullable = false, insertable = false, updatable = false)
    })
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_GRU", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_SUB", nullable = false, updatable = false, insertable = false)
    })
    private SubGroup subGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_MED", nullable = false, updatable = false, insertable = false)
    })
    private MeasureUnit usageMeasureUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_MED_MAY", nullable = false, updatable = false, insertable = false)
    })
    private MeasureUnit groupMeasureUnit;

    @OneToMany(mappedBy = "productItem", fetch = FetchType.LAZY)
    private List<Inventory> inventories = new ArrayList<Inventory>(0);

    @OneToMany(mappedBy = "productItem", fetch = FetchType.LAZY)
    private List<MovementDetail> movementDetailList = new ArrayList<MovementDetail>(0);

    @OneToMany(mappedBy = "productItem", fetch = FetchType.LAZY)
    private List<OrderMaterial> orderMaterials = new ArrayList<OrderMaterial>(0);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA_ART", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount cashAccount;

    @Column(name = "STOCKMINIMO", precision = 16, scale = 6)
    private BigDecimal minimalStock;

    @Column(name = "STOCKMAXIMO", precision = 16, scale = 6)
    private BigDecimal maximumStock;

    public ProductItemPK getId() {
        return id;
    }

    public void setId(ProductItemPK id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductItemState getState() {
        return state;
    }

    public void setState(ProductItemState state) {
        this.state = state;
    }

    public String getUsageMeasureCode() {
        return usageMeasureCode;
    }

    public void setUsageMeasureCode(String usageMeasureCode) {
        this.usageMeasureCode = usageMeasureCode;
    }

    public String getGroupMeasureCode() {
        return groupMeasureCode;
    }

    public void setGroupMeasureCode(String groupMeasureCode) {
        this.groupMeasureCode = groupMeasureCode;
    }

    public BigDecimal getEquivalentQuantity() {
        return equivalentQuantity;
    }

    public void setEquivalentQuantity(BigDecimal equivalentQuantity) {
        this.equivalentQuantity = equivalentQuantity;
    }

    public Boolean getControlValued() {
        return controlValued;
    }

    public void setControlValued(Boolean controlValued) {
        this.controlValued = controlValued;
    }

    public Boolean getSaleable() {
        return saleable;
    }

    public void setSaleable(Boolean saleable) {
        this.saleable = saleable;
    }

    public String getProductItemAccount() {
        return productItemAccount;
    }

    public void setProductItemAccount(String productItemAccount) {
        this.productItemAccount = productItemAccount;
    }

    public BigDecimal getInvestmentAmount() {
        return investmentAmount;
    }

    public void setInvestmentAmount(BigDecimal investmentAmount) {
        this.investmentAmount = investmentAmount;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getNameShort() {
        return nameShort;
    }

    public void setNameShort(String nameShort) {
        this.nameShort = nameShort;
    }

    public SubGroup getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(SubGroup subGroup) {
        this.subGroup = subGroup;
        if (null != subGroup) {
            setSubGroupCode(subGroup.getSubGroupCode());
            setGroupCode(subGroup.getGroupCode());
        } else {
            setSubGroupCode(null);
            setGroupCode(null);
        }
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getSubGroupCode() {
        return subGroupCode;
    }

    public void setSubGroupCode(String subGroupCode) {
        this.subGroupCode = subGroupCode;
    }

    public MeasureUnit getUsageMeasureUnit() {
        return usageMeasureUnit;
    }

    public void setUsageMeasureUnit(MeasureUnit usageMeasureUnit) {
        this.usageMeasureUnit = usageMeasureUnit;
        if (null != usageMeasureUnit) {
            setUsageMeasureCode(usageMeasureUnit.getId().getMeasureUnitCode());
        } else {
            setUsageMeasureCode(null);
        }
    }

    public MeasureUnit getGroupMeasureUnit() {
        return groupMeasureUnit;
    }

    public void setGroupMeasureUnit(MeasureUnit groupMeasureUnit) {
        this.groupMeasureUnit = groupMeasureUnit;
        if (null != groupMeasureUnit) {
            setGroupMeasureCode(groupMeasureUnit.getId().getMeasureUnitCode());
        } else {
            setGroupMeasureCode(null);
        }
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public List<Inventory> getInventories() {
        return inventories;
    }

    public void setInventories(List<Inventory> inventories) {
        this.inventories = inventories;
    }

    public List<MovementDetail> getMovementDetailList() {
        return movementDetailList;
    }

    public void setMovementDetailList(List<MovementDetail> movementDetailList) {
        this.movementDetailList = movementDetailList;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
        if (null != cashAccount) {
            setProductItemAccount(cashAccount.getAccountCode());
        } else {
            setProductItemAccount(null);
        }
    }

    public BigDecimal getMinimalStock() {
        return minimalStock;
    }

    public void setMinimalStock(BigDecimal minimalStock) {
        this.minimalStock = minimalStock;
    }

    public BigDecimal getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(BigDecimal maximumStock) {
        this.maximumStock = maximumStock;
    }

    public String getFullName() {
        return getProductItemCode() + "-" + getName();
    }
    public String getFullName2() {
        return "["+getProductItemCode() + "] " + getName();
    }

    public List<OrderMaterial> getOrderMaterials() {
        return orderMaterials;
    }

    public void setOrderMaterials(List<OrderMaterial> orderMaterials) {
        this.orderMaterials = orderMaterials;
    }

    public BigDecimal getCu() {
        return cu;
    }

    public void setCu(BigDecimal cu) {
        this.cu = cu;
    }

    public BigDecimal getCt() {
        return ct;
    }

    public void setCt(BigDecimal ct) {
        this.ct = ct;
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
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public MeasurementUnit getBasicMeasure() {
        return basicMeasure;
    }

    public void setBasicMeasure(MeasurementUnit basicMeasure) {
        this.basicMeasure = basicMeasure;
    }

    public BigDecimal getBasicQuantity() {
        return basicQuantity;
    }

    public void setBasicQuantity(BigDecimal basicQuantity) {
        this.basicQuantity = basicQuantity;
    }

    public Boolean getFixSale() {
        return fixSale;
    }

    public void setFixSale(Boolean fixSale) {
        this.fixSale = fixSale;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getProductItemCodeEq() {
        return productItemCodeEq;
    }

    public void setProductItemCodeEq(String productItemCodeEq) {
        this.productItemCodeEq = productItemCodeEq;
    }

    public String getProductItemCodeSin() {
        return productItemCodeSin;
    }

    public void setProductItemCodeSin(String productItemCodeSin) {
        this.productItemCodeSin = productItemCodeSin;
    }

    public Integer getMeasureUnitSin() {
        return measureUnitSin;
    }

    public void setMeasureUnitSin(Integer measureUnitSin) {
        this.measureUnitSin = measureUnitSin;
    }

    public ProductsServices getProductService() {
        return productService;
    }

    public void setProductService(ProductsServices productService) {
        this.productService = productService;
    }

    public EconomicActivity getEconomicActivity() {
        return economicActivity;
    }

    public void setEconomicActivity(EconomicActivity economicActivity) {
        this.economicActivity = economicActivity;
    }


    public MeasureUnitSIN getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnitSIN measureUnit) {
        this.measureUnit = measureUnit;
    }
}
