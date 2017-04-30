package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.MetaProduct;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "WarehouseSlot.findByProcessedProductAndFinishedGoodsWarehouse",
                    query = "select warehouseSlot " +
                            "from WarehouseSlot warehouseSlot " +
                            "where warehouseSlot.metaProduct = :processedProduct and warehouseSlot.finishedGoodsWarehouse = :finishedGoodsWarehouse"),
        @NamedQuery(name = "WarehouseSlot.findByMetaProduct",
                    query = "select warehouseSlot " +
                            "from WarehouseSlot warehouseSlot " +
                            "where warehouseSlot.metaProduct = :metaProduct " +
                            "order by warehouseSlot.finishedGoodsWarehouse.name, warehouseSlot.code")
})

@TableGenerator(name = "WarehouseSlot_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "AMBIENTEDEPOSITO",
        allocationSize = 10)

@Entity
@Table(name = "AMBIENTEDEPOSITO", uniqueConstraints = @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class WarehouseSlot implements BaseModel {

    @Id
    @Column(name = "IDAMBIENTEDEPOSITO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WarehouseSlot_Generator")
    private Long id;

    @Column(name = "CODIGO", nullable = false, length = 100)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDDEPOSITOPRODUCTOTERMINADO", nullable = false, updatable = false, insertable = true)
    private FinishedGoodsWarehouse finishedGoodsWarehouse;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDMETAPRODUCTOPRODUCCION", nullable = false, updatable = false, insertable = true)
    private MetaProduct metaProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Transient
    private int auxiliarityKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FinishedGoodsWarehouse getFinishedGoodsWarehouse() {
        return finishedGoodsWarehouse;
    }

    public void setFinishedGoodsWarehouse(FinishedGoodsWarehouse finishedGoodsWarehouse) {
        this.finishedGoodsWarehouse = finishedGoodsWarehouse;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Transient
    public String getFullNameOfMetaProduct() {
        return (metaProduct == null ? "" : metaProduct.getFullName());
    }

    public void setFullNameOfMetaProduct(String fullName) {

    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public int getAuxiliarityKey() {
        return auxiliarityKey;
    }

    public void setAuxiliarityKey(int auxiliarityKey) {
        this.auxiliarityKey = auxiliarityKey;
    }

    @Transient
    public String getFullName() {
        return "[" + finishedGoodsWarehouse.getName() + "] " + code;
    }
}
