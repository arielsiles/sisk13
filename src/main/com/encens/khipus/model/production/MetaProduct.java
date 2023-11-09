package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 10:12 AM
 * To change this template use File | Settings | File Templates.
 */
@TableGenerator(name = "MetaProduct_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "metaproductoproduccion",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Table(name = "metaproductoproduccion", uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "idcompania"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@javax.persistence.Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class MetaProduct implements Serializable, BaseModel {

    @Id
    @Column(name = "idmetaproductoproduccion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MetaProduct_Generator")
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String code;

    @Column(name = "nombre", nullable = false, length = 200)
    private String name;

    @Column(name = "descripcion", nullable = true, length = 500)
    private String description;

    @Column(name = "esacopiable", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean collectable = Boolean.FALSE;

    @Column(name = "cod_art", insertable = false, updatable = false, nullable = false)
    private String productItemCode;

    @Column(name = "precio", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "no_cia", insertable = false, updatable = false, nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia"),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art")
    })
    private ProductItem productItem;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadmedidaproduccion", nullable = true, updatable = true, insertable = true)
    private MeasureUnitProduction measureUnitProduction;

    /*@ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "IDPRODUCTOBASE", columnDefinition = "NUMBER(24,0)", nullable = false, updatable = false, insertable = true)
    private BaseProduct baseProduct;*/

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public MeasureUnitProduction getMeasureUnitProduction() {
        return measureUnitProduction;
    }

    public void setMeasureUnitProduction(MeasureUnitProduction measureUnitProduction) {
        this.measureUnitProduction = measureUnitProduction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getNameCode(){
        return this.code + '-' + name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Transient
    public String getFullName() {
        if (getCode() == null || getName() == null) {
            return "";
        } else {
            return "[" + getCode() + "] " + getName();
        }
    }

    public void setFullName(String fullName) {

    }

    @Transient
    public String getFullNameRawMaterial() {
        if (getCode() == null || getName() == null) {
            return "";
        } else {
            return "[" + getCode() + "] " + getName() + printMeasureUnitProduction();
        }
    }

    public void setFullNameRawMaterial(String fullNameRawMaterial) {

    }

    private String printMeasureUnitProduction() {
        if (measureUnitProduction == null)
            return "";
        else
            return " , " + measureUnitProduction.getName();
    }

    public Boolean getCollectable() {
        return collectable;
    }

    public void setCollectable(Boolean collectable) {
        this.collectable = collectable;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /*public BaseProduct getBaseProduct() {
        return baseProduct;
    }

    public void setBaseProduct(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }*/
}
