package com.encens.khipus.model.xproduction;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.xproduction.MetaProduct;
import com.encens.khipus.model.xproduction.SingleProduct;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

@TableGenerator(name = "ProductProcessingSingle_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "productosimpleprocesado",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "productosimpleprocesado")
public class ProductProcessingSingle implements BaseModel {

    @Id
    @Column(name = "idproductosimpleprocesado", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductProcessingSingle_Generator")
    private Long id;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false, updatable = true, insertable = true)
    private MetaProduct metaProduct;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idproductosimple", nullable = false, updatable = false, insertable = true)
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
