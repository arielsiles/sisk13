package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.0
 */

@NamedQueries({
        @NamedQuery(name = "Inventory.sumUnitaryBalancesByArticleCode",
                query = "select sum(inventory.unitaryBalance) from Inventory inventory where inventory.id.companyNumber =:companyNumber and inventory.id.articleCode =:articleNumber"),
        @NamedQuery(name = "Inventory.findUnitaryBalanceByProductItemAndArticle",
                query = "select inventory.unitaryBalance from Inventory inventory " +
                        "where inventory.productItem.id=:productItemId and inventory.warehouse.id=:warehouseId"),
        @NamedQuery(name = "Inventory.findInventoryByProductItemCode",
                query = "select inventory from Inventory inventory " +
                        "where inventory.productItem.productItemCode=:productItemCode"),
        @NamedQuery(name = "Inventory.findWarehouseByItemArticle",
        query = "select inventory.warehouse from Inventory inventory " +
                "where inventory.productItem.id = :productItemId")
})

@Entity
@Table(name = "inv_inventario", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class Inventory implements BaseModel {
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "companyNumber", column = @Column(name = "no_cia", nullable = false, insertable = true)),
            @AttributeOverride(name = "warehouseCode", column = @Column(name = "cod_alm", nullable = false, insertable = true)),
            @AttributeOverride(name = "articleCode", column = @Column(name = "cod_art", nullable = false, insertable = true))
    })
    private InventoryPK id;

    @Column(name = "cod_alm", nullable = false, updatable = false, insertable = false)
    private String warehouseCode;

    @Column(name = "cod_art", nullable = false, updatable = false, insertable = false)
    private String articleCode;

    @Column(name = "saldo_uni", precision = 12, scale = 2)
    private BigDecimal unitaryBalance;

    @Version
    @Column(name = "version")
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_alm", nullable = false, insertable = false, updatable = false)
    })
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    public InventoryPK getId() {
        return id;
    }

    public void setId(InventoryPK id) {
        this.id = id;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    public BigDecimal getUnitaryBalance() {
        return unitaryBalance;
    }

    public void setUnitaryBalance(BigDecimal unitaryBalance) {
        this.unitaryBalance = unitaryBalance;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }
}
