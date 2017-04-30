package com.encens.khipus.model.products;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "Product.findByCode", query = "select p from Product p where p.code =:code"),
                @NamedQuery(name = "Product.findDiscountRules", query = "select p.discountRules from Product p where p =:product")}
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Product.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "producto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "producto", uniqueConstraints = {@UniqueConstraint(columnNames = {"idcompania", "nombreproducto"}), @UniqueConstraint(columnNames = {"idcompania", "codigoproducto"})})
public class Product implements BaseModel {

    @Id
    @Column(name = "idproducto")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Product.tableGenerator")
    private Long id;

    @Column(name = "nombreproducto", nullable = false, length = 150)
    private String name;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Column(name = "fechaestado")
    @Temporal(TemporalType.DATE)
    private Date stateDate;

    @Column(name = "preciocompra", precision = 13, scale = 2)
    private BigDecimal buyPrice;

    @Column(name = "precioventa", precision = 13, scale = 2)
    private BigDecimal sellPrice;

    @Column(name = "cuentaactivo", length = 200)
    private String assetAccount;

    @Column(name = "cuentapasivo", length = 200)
    private String debtAccount;

    @Column(name = "codigoproducto", nullable = false, length = 100)
    private String code;

    @ManyToOne
    @JoinColumn(name = "idtipoproducto", referencedColumnName = "idtipoproducto", nullable = false)
    private ProductType productType;

    @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<ProductDiscountRule> discountRules = new ArrayList<ProductDiscountRule>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getAssetAccount() {
        return assetAccount;
    }

    public void setAssetAccount(String assetAccount) {
        this.assetAccount = assetAccount;
    }

    public String getDebtAccount() {
        return debtAccount;
    }

    public void setDebtAccount(String debtAccount) {
        this.debtAccount = debtAccount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public List<ProductDiscountRule> getDiscountRules() {
        return discountRules;
    }

    public void setDiscountRules(List<ProductDiscountRule> discountRules) {
        this.discountRules = discountRules;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
