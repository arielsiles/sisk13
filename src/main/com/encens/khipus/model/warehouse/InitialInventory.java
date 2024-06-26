package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.0
 */
@NamedQueries({
        @NamedQuery(name = "initialInventory.findInitialInventory",
                query = "select initialInventory " +
                        "from InitialInventory initialInventory " +
                        "where initialInventory.warehouseCode =:warehouseCode " +
                        "and initialInventory.year =:year " +
                        "order by initialInventory.productItemName asc "),
        @NamedQuery(name = "initialInventory.findUnitCostbyCode",
                query = "select initialInventory " +
                        "from InitialInventory initialInventory " +
                        "where initialInventory.productItemCode =:productItemCode " +
                        "and initialInventory.year =:year ")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "InitialInventory.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "inv_inicio",
        initialValue = 1,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Table(name = "inv_inicio", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class InitialInventory implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InitialInventory.tableGenerator")
    @Column(name = "idinvinicio", nullable = true)
    private Long id;

    @Column(name = "cod_art")
    private String productItemCode;

    @Column(name = "nombre")
    private String productItemName;

    @Column(name = "cantidad")
    private BigDecimal quantity;

    @Column(name = "alm")
    private String warehouseCode;

    @Column(name = "costo_uni")
    private BigDecimal unitCost;

    @Column(name = "gestion")
    private String year;

    @Column(name = "no_cia")
    @Length(max = 2)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    /*@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "COD_ALM", referencedColumnName = "COD_ALM", nullable = false, insertable = false, updatable = false)
    })
    private Warehouse warehouse;*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public String getProductItemName() {
        return productItemName;
    }

    public void setProductItemName(String productItemName) {
        this.productItemName = productItemName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
