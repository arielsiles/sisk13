package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.cashbox.Branch;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.4
 */

@NamedQueries({
        @NamedQuery(name = "SoldProduct.findByInvoiceNumber",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber"),
        @NamedQuery(name = "SoldProduct.findByInvoiceNumberWithoutCutCheese",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.productItemCode <> :codCutCheese"),
        @NamedQuery(name = "SoldProduct.findByInvoiceNumberWithoutCutCheeseAndEDAM",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.productItemCode <> :codCutCheese and soldProduct.productItemCode <> :codEDAMCheese"),
        @NamedQuery(name = "SoldProduct.findByInvoiceNumberWithoutEDAM",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.productItemCode <> :codEDAMCheese"),
        @NamedQuery(name = "SoldProduct.findDelivery",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber"),
        @NamedQuery(name = "SoldProduct.findByCashSale",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.orderNumber is null"),
        @NamedQuery(name = "SoldProduct.findByCashOrder",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.orderNumber is not null"),
        @NamedQuery(name = "SoldProduct.findByCashOrderDate",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.orderNumber is not null"),
        @NamedQuery(name = "SoldProduct.findByInvoiceNumberAndState",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.state=:state"),
        @NamedQuery(name = "SoldProduct.findByProductItem",
                query = "select distinct(soldProduct.productItem) from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber"),
        @NamedQuery(name = "SoldProduct.findByProductItemWithouCutCheese",
                query = "select distinct(soldProduct.productItem) from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.productItemCode <> :codCutCheese"),
        @NamedQuery(name = "SoldProduct.findByProductItemWithouCutCheeseAndEDAM",
                query = "select distinct(soldProduct.productItem) from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.productItemCode <> :codCutCheese and soldProduct.productItemCode <> :codEDAMCheese"),
        @NamedQuery(name = "SoldProduct.sunQuantitiesByProductItem",
                query = "select sum(soldProduct.quantity) from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.productItem =:productItem and soldProduct.companyNumber =:companyNumber"),
        @NamedQuery(name = "SoldProduct.findByState",
                query = "select soldProduct from SoldProduct soldProduct where soldProduct.invoiceNumber =:invoiceNumber and soldProduct.companyNumber =:companyNumber and soldProduct.state =:state")
})

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "inv_ventart", schema = Constants.FINANCES_SCHEMA)
public class SoldProduct implements BaseModel {
    @Id
    @Column(name = "ID_MOV", nullable = false)
    private Long id;

    @Column(name = "NOMBRES", nullable = false, length = 100)
    @Length(max = 100)
    private String names;

    @Column(name = "APELLIDOPATERNO", nullable = false, length = 65)
    @Length(max = 65)
    private String firstName;

    @Column(name = "APELLIDOMATERNO", nullable = false, length = 65)
    @Length(max = 65)
    private String secondName;

    @Column(name = "CANTIDAD", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "COD_PER", nullable = false, length = 20)
    @Length(max = 20)
    private String personalCode;

    @Column(name = "NODOC_PER", nullable = false, length = 20)
    @Length(max = 20)
    private String personalIdentification;

    @Column(name = "COD_DOSI", nullable = false, length = 20)
    @Length(max = 20)
    private String dosificationCode;

    @Column(name = "NO_CIA", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    private SoldProductState state;

    @Column(name = "NO_FACT", nullable = false, length = 10)
    @Length(max = 10)
    private String invoiceNumber;

    @Column(name = "PEDIDO", nullable = true, length = 10)
    @Length(max = 10)
    private String orderNumber;

    @Column(name = "NO_VALE", nullable = true, length = 20)
    @Length(max = 20)
    private String numberVoucher;

    @Column(name = "COD_ALM", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_ALM", nullable = false, updatable = false, insertable = false)
    })
    private Warehouse warehouse;

    @Column(name = "COD_ART", nullable = false, length = 6)
    @Length(max = 6)
    private String productItemCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ART", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COD_EST")
    private Branch branch;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IDENTARTICULO")
    private ProductDelivery productDelivery;

    @Version
    @Column(name = "version")
    private long version;

    @Transient
    private String fullName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    public void setPersonalCode(String personalCode) {
        this.personalCode = personalCode;
    }

    public String getPersonalIdentification() {
        return personalIdentification;
    }

    public void setPersonalIdentification(String personalIdentification) {
        this.personalIdentification = personalIdentification;
    }

    public String getDosificationCode() {
        return dosificationCode;
    }

    public void setDosificationCode(String dosificationCode) {
        this.dosificationCode = dosificationCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public SoldProductState getState() {
        return state;
    }

    public void setState(SoldProductState state) {
        this.state = state;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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
            setWarehouseCode(warehouse.getWarehouseCode());
        } else {
            setWarehouseCode(null);
        }
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        if (null != productItem) {
            setProductItemCode(productItem.getProductItemCode());
        } else {
            setProductItemCode(null);
        }
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public ProductDelivery getProductDelivery() {
        return productDelivery;
    }

    public void setProductDelivery(ProductDelivery productDelivery) {
        this.productDelivery = productDelivery;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getNumberVoucher() {
        return numberVoucher;
    }

    public void setNumberVoucher(String numberVoucher) {
        this.numberVoucher = numberVoucher;
    }

    public String getFullName() {

        return this.names + " " + this.firstName + " " + this.secondName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
