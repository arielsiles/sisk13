package com.encens.khipus.model.production;

import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(name = "RawMaterialRejectionNote_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "NOTARECHAZOMATERIAPRIMA",
        allocationSize = 10)

@Entity
@Table(name = "NOTARECHAZOMATERIAPRIMA", uniqueConstraints = @UniqueConstraint(columnNames = {"IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class RawMaterialRejectionNote implements com.encens.khipus.model.BaseModel {
    @Id
    @Column(name = "IDNOTARECHAZOMATERIAPRIMA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialRejectionNote_Generator")
    private Long id;

    @Column(name = "FECHA",columnDefinition = "DATE" , nullable = false)
    private Date date;

    @Column(name = "CANTIDADRECHAZADA", columnDefinition = "DECIMAL(15,0)", nullable = false, length = 1000)
    private Double rejectedAmount;

    @Column(name = "ACIDA", nullable = true, length = 1000)
    private String acid;

    @Column(name = "AGUADABAJOSNG", nullable = true, length = 1000)
    private String wateryLowSNG;

    @Column(name = "SUCIA", nullable = true, length = 1000)
    private String dirty;

    @Column(name = "CALOSTRO", nullable = true, length = 1000)
    private String colostrum;

    @Column(name = "TACHOMALESTADO", nullable = true, length = 1000)
    private String disrepairCan;

    @Column(name = "OTROS", nullable = true, length = 1000)
    private String other;

    @Column(name = "OBSERVACIONES", nullable = true, length = 1000)
    private String observations;

    @Column(name = "ESTADO",nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionCollectionState state = ProductionCollectionState.PENDING;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTORMATERIAPRIMA", nullable = false, updatable = false, insertable = true)
    private RawMaterialProducer rawMaterialProducer;

    @OneToOne
    @JoinColumn(name = "IDMETAPRODUCTOPRODUCCION", nullable = false, updatable = false, insertable = true)
    private MetaProduct metaProduct;

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

    public RawMaterialProducer getRawMaterialProducer() {
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

    public MetaProduct getMetaProduct() {
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
