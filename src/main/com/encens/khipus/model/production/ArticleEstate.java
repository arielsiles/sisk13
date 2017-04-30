package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.warehouse.ProductItem;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "ArticleEstate_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "ESTADOARTICULO",
        allocationSize = 10)

@Entity
@Table(name = "ESTADOARTICULO")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class ArticleEstate implements BaseModel {

    @Id
    @Column(name = "IDESTADOARTICULO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ArticleEstate_Generator")
    private Long id;

    @Column(name = "ESTADO", nullable = true)
    private String estate;

    @Column(name = "DESCRIPCION", nullable = true)
    private String description;

    @Column(name = "COD_ART", insertable = false, updatable = false, nullable = false)
    private String productItemCode;

    @Column(name = "NO_CIA", insertable = false, updatable = false, nullable = false)
    @Length(max = 2)
    private String companyNumber;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "COD_ART", referencedColumnName = "COD_ART")
    })
    private ProductItem productItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstate() {
        return estate;
    }

    public void setEstate(String estate) {
        this.estate = estate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }
}
