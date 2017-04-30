package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.17
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "FixedAssetPart.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "parteactfijo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "FixedAssetPart.findNextNumber",
                query = "select max(fixedAssetPart.number) from FixedAssetPart fixedAssetPart where fixedAssetPart.fixedAsset =:fixedAsset"),
        @NamedQuery(name = "FixedAssetPart.findByFixedAsset",
                query = "select fixedAssetPart from FixedAssetPart fixedAssetPart where fixedAssetPart.fixedAsset =:fixedAsset")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({UpperCaseStringListener.class, CompanyListener.class})
@Table(name = "PARTEACTFIJO", schema = Constants.KHIPUS_SCHEMA)
public class FixedAssetPart implements BaseModel {

    @Id
    @Column(name = "IDPARTEACTFIJO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetPart.tableGenerator")
    private Long id;

    @Column(name = "DESCRIPCION", nullable = false, length = 250)
    @Length(max = 250)
    private String description;

    @Column(name = "NUMERO", nullable = false)
    private Long number;

    @Column(name = "PRECIOUNI", nullable = false, precision = 16, scale = 6)
    private BigDecimal unitPrice;

    @Version
    @Column(name = "version")
    private long version;

    @Column(name = "SERIE", nullable = true, length = 250)
    @Length(max = 250)
    private String serialNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDACTIVOFIJO", nullable = false, updatable = false, insertable = true)
    private FixedAsset fixedAsset;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", nullable = false, updatable = true, insertable = true),
            @JoinColumn(name = "UNIDADMEDIDA", nullable = false, updatable = true, insertable = true)
    })
    private MeasureUnit measureUnit;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
