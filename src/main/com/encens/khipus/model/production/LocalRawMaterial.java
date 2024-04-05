package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;

@TableGenerator(
        name = "LocalRawMaterial.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "materiasprimaszona",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE
)

@Entity
@Table(name = "materiasprimaszona")
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
public class LocalRawMaterial implements BaseModel {

    @Id
    @Column(name = "idmateriasprimaszona", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "LocalRawMaterial.tableGenerator")
    private Long id;

    @Column(name = "precioorigen")
    private BigDecimal originPrice;

    @Column(name = "preciodestino")
    private BigDecimal destinationPrice;

    @Column(name = "observacion")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false)
    private MetaProduct metaProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idinspeccionzona", nullable = false)
    private ZoneInspection zoneInspection;

    @Column(name = "version")
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    // Getters and Setters

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getOriginPrice() {
        return originPrice;
    }

    public void setOriginPrice(BigDecimal originPrice) {
        this.originPrice = originPrice;
    }

    public BigDecimal getDestinationPrice() {
        return destinationPrice;
    }

    public void setDestinationPrice(BigDecimal destinationPrice) {
        this.destinationPrice = destinationPrice;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public ZoneInspection getZoneInspection() {
        return zoneInspection;
    }

    public void setZoneInspection(ZoneInspection zoneInspection) {
        this.zoneInspection = zoneInspection;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}