package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.xproduction.RawMaterialCollectionSession;
import com.encens.khipus.model.xproduction.RawMaterialProducer;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "CollectedRawMaterial.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "acopiomateriaprima",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "acopiomateriaprima", uniqueConstraints = @UniqueConstraint(columnNames = {"idsesionacopio", "idproductormateriaprima"}))
public class CollectedRawMaterial implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "idacopiomateriaprima", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CollectedRawMaterial.tableGenerator")
    private Long id;

    @Column(name = "cantidad", nullable = false, columnDefinition = "DECIMAL(16,2)")
    private Double amount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductormateriaprima", nullable = false, updatable = false, insertable = true)
    private RawMaterialProducer rawMaterialProducer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idsesionacopio", nullable = false, updatable = false, insertable = true)
    private RawMaterialCollectionSession rawMaterialCollectionSession;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Transient
    private String rawMaterialProducerLastName;

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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public RawMaterialCollectionSession getRawMaterialCollectionSession() {
        return rawMaterialCollectionSession;
    }

    public void setRawMaterialCollectionSession(RawMaterialCollectionSession rawMaterialCollectionSession) {
        this.rawMaterialCollectionSession = rawMaterialCollectionSession;
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

    public String getRawMaterialProducerLastName() {
        return rawMaterialProducer.getLastName();
    }

    public void setRawMaterialProducerLastName(String rawMaterialProducerLastName) {
        this.rawMaterialProducerLastName = rawMaterialProducerLastName;
    }
}


