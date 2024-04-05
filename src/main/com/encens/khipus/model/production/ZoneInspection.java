package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(name = "ZoneInspection.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "inspeccionzona",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "inspeccionzona")
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
public class ZoneInspection implements BaseModel {

    @Id
    @Column(name = "idinspeccionzona", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ZoneInspection.tableGenerator")
    private Long id;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.TIME)
    @Column(name = "horasalida")
    private Date initHour;

    @Temporal(TemporalType.TIME)
    @Column(name = "horallegada")
    private Date endHour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductor", nullable = true)
    private RawMaterialProducer producer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = false, updatable = true, insertable = true)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version")
    private long version;

    @Override
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

    public Date getInitHour() {
        return initHour;
    }

    public void setInitHour(Date initHour) {
        this.initHour = initHour;
    }

    public Date getEndHour() {
        return endHour;
    }

    public void setEndHour(Date endHour) {
        this.endHour = endHour;
    }

    public RawMaterialProducer getProducer() {
        return producer;
    }

    public void setProducer(RawMaterialProducer producer) {
        this.producer = producer;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
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
}
