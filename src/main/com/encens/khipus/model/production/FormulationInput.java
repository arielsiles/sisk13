package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.model.warehouse.ProductItem;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity fo Product
 *
 * @author:
 */

@NamedQueries({})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FormulationInput.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pr_insumoformula",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pr_insumoformula")
public class FormulationInput implements BaseModel {

    @Id
    @Column(name = "idinsumoformula")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FormulationInput.tableGenerator")
    private Long id;

    @Column(name = "cantidad")
    private BigDecimal quantity;

    @Column(name = "cod_art")
    private String productItemCode;

    @Column(name = "DEFECTO")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean inputDefault = Boolean.TRUE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            /*@JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA"),*/
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art", updatable = false, insertable = false)
    })
    private ProductItem productItem;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idformula", nullable = false, updatable = false, insertable = true)
    private Formulation formulation;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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
    }

    public Formulation getFormulation() {
        return formulation;
    }

    public void setFormulation(Formulation formulation) {
        this.formulation = formulation;
    }

    public Boolean getInputDefault() {
        return inputDefault;
    }

    public void setInputDefault(Boolean inputDefault) {
        this.inputDefault = inputDefault;
    }
}
