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
    @Column(name = "id_mov", nullable = false)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    @Length(max = 100)
    private String names;

    @Column(name = "apellidopaterno", nullable = false, length = 65)
    @Length(max = 65)
    private String firstName;

    @Column(name = "apellidomaterno", nullable = false, length = 65)
    @Length(max = 65)
    private String secondName;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "cod_per", nullable = false, length = 20)
    @Length(max = 20)
    private String personalCode;

    @Column(name = "nodoc_per", nullable = false, length = 20)
    @Length(max = 20)
    private String personalIdentification;

    @Column(name = "cod_dosi", nullable = false, length = 20)
    @Length(max = 20)
    private String dosificationCode;

    @Column(name = "no_cia", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private SoldProductState state;

    @Column(name = "no_fact", nullable = false, length = 10)
    @Length(max = 10)
    private String invoiceNumber;

    @Column(name = "pedido", nullable = true, length = 10)
    @Length(max = 10)
    private String orderNumber;

    @Column(name = "no_vale", nullable = true, length = 20)
    @Length(max = 20)
    private String numberVoucher;

    @Column(name = "cod_alm", nullable = false, length = 6)
    @Length(max = 6)
    private String warehouseCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_alm", nullable = false, updatable = false, insertable = false)
    })
    private Warehouse warehouse;

    @Column(name = "cod_art", nullable = false, length = 6)
    @Length(max = 6)
    private String productItemCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cod_est")
    private Branch branch;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "identarticulo")
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
