package com.encens.khipus.model.production;

import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "RawMaterialProducerDiscount.findWithGreatestCodeByRawMaterialProducer",
                query = "select rawMaterialProducerDiscount " +
                        "from RawMaterialProducerDiscount rawMaterialProducerDiscount " +
                        "left join fetch rawMaterialProducerDiscount.rawMaterialPayRecord " +
                        "where rawMaterialProducerDiscount.rawMaterialProducer = :rawMaterialProducer and " +
                        "rawMaterialProducerDiscount.code >= (" +
                        "   select discount.code " +
                        "   from RawMaterialProducerDiscount discount " +
                        "   where discount.rawMaterialProducer = :rawMaterialProducer)"),
        @NamedQuery(name = "RawMaterialProducerDiscount.findOpenByRawMaterialProducer",
                query = "select rawMaterialProducerDiscount " +
                        "from RawMaterialProducerDiscount rawMaterialProducerDiscount " +
                        "where rawMaterialProducerDiscount.rawMaterialProducer = :rawMaterialProducer and rawMaterialProducerDiscount.rawMaterialPayRecord is NULL")
})

@TableGenerator(name = "RawMaterialProducerDiscount_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "descuentproductmateriaprima",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "descuentproductmateriaprima", uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "idproductormateriaprima", "idcompania"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class RawMaterialProducerDiscount implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "iddescuentproductmateriaprima",nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialProducerDiscount_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idproductormateriaprima", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.production.RawMaterialProducer rawMaterialProducer;

    @Column(name = "codigo", nullable = false)
    private long code;

    @Column(name = "yogurt",columnDefinition = "DECIMAL(16,2)", nullable = false)
    private double yogurt = 0.0;

    @Column(name = "veterinario",columnDefinition = "DECIMAL(16,2)", nullable = false)
    private double veterinary = 0.0;

    @Column(name = "credito",columnDefinition = "DECIMAL(16,2)" , nullable = false)
    private double credit = 0.0;

    @Column(name = "tachos",columnDefinition = "DECIMAL(16,2)" ,nullable = false)
    private double cans = 0.0;

    @Column(name = "otrosdescuentos", nullable = false ,columnDefinition = "DECIMAL(16,2)")
    private double otherDiscount = 0.0;

    @Column(name = "otrosingresos", nullable = false,columnDefinition = "DECIMAL(16,2)")
    private double otherIncoming = 0.0;

    @Column(name = "retencion", nullable = false,columnDefinition = "DECIMAL(16,2)")
    private double withholdingTax = 0.0;

    @Column(name = "alcohol", nullable = false,columnDefinition = "DECIMAL(16,2)")
    private double alcohol = 0.0;

    @Column(name = "concentrados", nullable = false,columnDefinition = "DECIMAL(16,2)")
    private double concentrated = 0.0;

    @Column(name = "comision", nullable = false,columnDefinition = "DECIMAL(16,2)")
    private double commission = 0.0;

    @OneToOne(mappedBy = "rawMaterialProducerDiscount", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private com.encens.khipus.model.production.RawMaterialPayRecord rawMaterialPayRecord;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
    }

    public double getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(double alcohol) {
        this.alcohol = alcohol;
    }

    public double getConcentrated() {
        return concentrated;
    }

    public void setConcentrated(double concentrated) {
        this.concentrated = concentrated;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public double getYogurt() {
        return yogurt;
    }

    public void setYogurt(double yogurt) {
        this.yogurt = yogurt;
    }

    public double getVeterinary() {
        return veterinary;
    }

    public void setVeterinary(double veterinary) {
        this.veterinary = veterinary;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public double getCans() {
        return cans;
    }

    public void setCans(double cans) {
        this.cans = cans;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(double withholdingTax) {
        this.withholdingTax = withholdingTax;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }

    public RawMaterialPayRecord getRawMaterialPayRecord() {
        return rawMaterialPayRecord;
    }

    public void setRawMaterialPayRecord(RawMaterialPayRecord rawMaterialPayRecord) {
        this.rawMaterialPayRecord = rawMaterialPayRecord;
    }

    public double getOtherDiscount() {
        return otherDiscount;
    }

    public void setOtherDiscount(double otherDiscount) {
        this.otherDiscount = otherDiscount;
    }

    public double getOtherIncoming() {
        return otherIncoming;
    }

    public void setOtherIncoming(double otherIncoming) {
        this.otherIncoming = otherIncoming;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }
}
