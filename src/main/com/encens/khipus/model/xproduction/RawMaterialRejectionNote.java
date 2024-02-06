package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.xproduction.MetaProduct;
import com.encens.khipus.model.xproduction.ProductionCollectionState;
import com.encens.khipus.model.xproduction.RawMaterialProducer;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(name = "RawMaterialRejectionNote_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "notarechazomateriaprima",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "notarechazomateriaprima", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class RawMaterialRejectionNote implements com.encens.khipus.model.BaseModel {
    @Id
    @Column(name = "idnotarechazomateriaprima", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialRejectionNote_Generator")
    private Long id;

    @Column(name = "fecha",columnDefinition = "DATE" , nullable = false)
    private Date date;

    @Column(name = "cantidadrechazada", columnDefinition = "DECIMAL(15,0)", nullable = false, length = 1000)
    private Double rejectedAmount;

    @Column(name = "acida", nullable = true, length = 1000)
    private String acid;

    @Column(name = "aguadabajosng", nullable = true, length = 1000)
    private String wateryLowSNG;

    @Column(name = "sucia", nullable = true, length = 1000)
    private String dirty;

    @Column(name = "calostro", nullable = true, length = 1000)
    private String colostrum;

    @Column(name = "tachomalestado", nullable = true, length = 1000)
    private String disrepairCan;

    @Column(name = "otros", nullable = true, length = 1000)
    private String other;

    @Column(name = "observaciones", nullable = true, length = 1000)
    private String observations;

    @Column(name = "estado",nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionCollectionState state = ProductionCollectionState.PENDING;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductormateriaprima", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.xproduction.RawMaterialProducer rawMaterialProducer;

    @OneToOne
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.xproduction.MetaProduct metaProduct;

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

    public Double getRejectedAmount() {
        return rejectedAmount;
    }

    public void setRejectedAmount(Double rejectedAmount) {
        this.rejectedAmount = rejectedAmount;
    }

    public String getAcid() {
        return acid;
    }

    public void setAcid(String acid) {
        this.acid = acid;
    }

    public String getWateryLowSNG() {
        return wateryLowSNG;
    }

    public void setWateryLowSNG(String wateryLowSNG) {
        this.wateryLowSNG = wateryLowSNG;
    }

    public String getDirty() {
        return dirty;
    }

    public void setDirty(String dirty) {
        this.dirty = dirty;
    }

    public String getColostrum() {
        return colostrum;
    }

    public void setColostrum(String colostrum) {
        this.colostrum = colostrum;
    }

    public String getDisrepairCan() {
        return disrepairCan;
    }

    public void setDisrepairCan(String disrepairCan) {
        this.disrepairCan = disrepairCan;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }

    public com.encens.khipus.model.xproduction.RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
    }

    @Transient
    public String getFullNameOfRawMaterialProducer() {
        return (rawMaterialProducer == null ? "" : rawMaterialProducer.getFullName());
    }

    public void setFullNameOfRawMaterialProducer(String fullNameOfRawMaterialProducer) {

    }

    public com.encens.khipus.model.xproduction.MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public ProductionCollectionState getState() {
        return state;
    }

    public void setState(ProductionCollectionState state) {
        this.state = state;
    }
}
