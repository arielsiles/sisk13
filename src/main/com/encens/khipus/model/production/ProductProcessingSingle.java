package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;

import javax.persistence.*;

@TableGenerator(name = "ProductProcessingSingle_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "PRODUCTOSIMPLEPROCESADO",
        allocationSize = 10)

@Entity
@Table(name = "PRODUCTOSIMPLEPROCESADO")
public class ProductProcessingSingle implements BaseModel {

    @Id
    @Column(name = "IDPRODUCTOSIMPLEPROCESADO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductProcessingSingle_Generator")
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDMETAPRODUCTOPRODUCCION", nullable = false, updatable = true, insertable = true)
    private MetaProduct metaProduct;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDPRODUCTOSIMPLE", nullable = false, updatable = false, insertable = true)
    private SingleProduct singleProduct;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public SingleProduct getSingleProduct() {
        return singleProduct;
    }

    public void setSingleProduct(SingleProduct singleProduct) {
        this.singleProduct = singleProduct;
    }
}
