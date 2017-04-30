package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.2
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "ProductInventoryRecord.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inv_product_register",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

/*
@NamedQueries({
        @NamedQuery(name = "ProductInventory.findByCode",
                query = "select productInventory from ProductInventory productInventory where productInventory.productItemCode =:productItemCode")
})*/


@Entity
@Table(name = "inv_product_register", schema = Constants.FINANCES_SCHEMA)
public class ProductInventoryRecord implements BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductInventoryRecord.tableGenerator")
    @Column(name = "idregistroinvproducto", nullable = false)
    private Long id;

    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    @Column(name = "cod_art", length = 6)
    @Length(max = 6)
    private String productItemCode;

    @Column(name = "cantidad", precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "tipo", length = 64)
    @Enumerated(EnumType.STRING)
    private ProductInventoryRecordType type;

    @Column(name = "tipomovimiento", length = 64)
    private String movementType;

    @Column(name = "descripcion", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_inv_prod", updatable = true, insertable = true)
    private ProductInventory productInventory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductInventory getProductInventory() {
        return productInventory;
    }

    public void setProductInventory(ProductInventory productInventory) {
        this.productInventory = productInventory;
    }

    public ProductInventoryRecordType getType() {
        return type;
    }

    public void setType(ProductInventoryRecordType type) {
        this.type = type;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }
}
