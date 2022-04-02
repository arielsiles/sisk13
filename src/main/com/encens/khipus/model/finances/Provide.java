package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemByProviderHistory;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.2
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Provide.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "articulo_por_proveedor",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "Provide.findByProviderAndProductItem",
                query = "select provide from Provide provide where provide.productItem =:productItem and provide.provider =:provider")
})

@Entity
@EntityListeners({CompanyNumberListener.class})
@Table(name = "articulo_por_proveedor", schema = Constants.FINANCES_SCHEMA, uniqueConstraints = {@UniqueConstraint(columnNames = {"no_cia", "cod_art", "cod_prov"})})
public class Provide implements BaseModel {

    @Id
    @Column(name = "id_articulo_por_proveedor", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Provide.tableGenerator")
    private Long id;

    @Column(name = "no_cia", nullable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "cod_art", nullable = false, length = 6)
    @Length(max = 6)
    private String productItemCode;

    @Column(name = "cod_prov", nullable = false, length = 6)
    @Length(max = 6)
    private String providerCode;

    @Column(name = "precio_grupo", nullable = false, precision = 12, scale = 6)
    private BigDecimal groupAmount;

    @Column(name = "entrega", nullable = false)
    private Integer delivery = 1;

    @Column(name = "cod_med_may", nullable = true, length = 6)
    @Length(max = 6)
    private String groupMeasureCode;

    @Version
    @Column(name = "version")
    private long version;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_prov", nullable = false, insertable = false, updatable = false)
    })
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cod_med_may", nullable = false, updatable = false, insertable = false)
    })
    private MeasureUnit groupMeasureUnit;

    @OneToMany(mappedBy = "provide", fetch = FetchType.LAZY)
    private List<ProductItemByProviderHistory> productItemByProviderHistoryList = new ArrayList<ProductItemByProviderHistory>(0);


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

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public Integer getDelivery() {
        return delivery;
    }

    public void setDelivery(Integer delivery) {
        this.delivery = delivery;
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
            setProductItemCode(productItem.getProductItemCode());
            setCompanyNumber(productItem.getCompanyNumber());
        } else {
            setProductItemCode(null);
            setCompanyNumber(null);
        }

    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }


    public BigDecimal getGroupAmount() {
        return groupAmount;
    }

    public void setGroupAmount(BigDecimal groupAmount) {
        this.groupAmount = groupAmount;
    }

    public String getGroupMeasureCode() {
        return groupMeasureCode;
    }

    public void setGroupMeasureCode(String groupMeasureCode) {
        this.groupMeasureCode = groupMeasureCode;
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

    public List<ProductItemByProviderHistory> getProductItemByProviderHistoryList() {
        return productItemByProviderHistoryList;
    }

    public void setProductItemByProviderHistoryList(List<ProductItemByProviderHistory> productItemByProviderHistoryList) {
        this.productItemByProviderHistoryList = productItemByProviderHistoryList;
    }

    @Override
    public String toString() {
        return "Provide{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", productItemCode='" + productItemCode + '\'' +
                ", groupAmount=" + groupAmount +
                ", delivery=" + delivery +
                ", groupMeasureCode='" + groupMeasureCode + '\'' +
                ", providerCode='" + providerCode + '\'' +
                '}';
    }
}
