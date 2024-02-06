package com.encens.khipus.model.xproduction;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

@TableGenerator(name = "ProductProcessing_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "productoreprocesado",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "productoreprocesado")
public class ProductProcessing implements BaseModel {

    @Id
    @Column(name = "idproductoreprocesado", columnDefinition = "NUMBER(24,0)", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductProcessing_Generator")
    private Long id;

    @Column(name = "unidades", nullable = true)
    private Integer units;

    @Column(name = "volumen", nullable = true ,columnDefinition = "DECIMAL(8,2)")
    private Double volume;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false, updatable = false, insertable = true)
    private MetaProduct metaProduct;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idproductobase", nullable = false, updatable = false, insertable = true)
    private BaseProduct baseProduct;

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

    public BaseProduct getBaseProduct() {
        return baseProduct;
    }

    public void setBaseProduct(BaseProduct baseProduct) {
        this.baseProduct = baseProduct;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }
}
