package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@TableGenerator(name = "CollectMaterial.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "acopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Table(name = "acopiomp")
@Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class CollectMaterial implements Serializable, BaseModel {

    @Id
    @Column(name = "idacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CollectMaterial.tableGenerator")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha", nullable = false)
    private Date date;

    @Column(name = "codigo", nullable = false, length = 50)
    private String code;

    @Column(name = "pesoneto", precision = 12, scale = 2, nullable = false)
    private BigDecimal netWeight;

    @Column(name = "pesobal", precision = 12, scale = 2, nullable = false)
    private BigDecimal balanceWeight;

    @Column(name = "precio", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "boleta", nullable = false)
    private String ticket;

    @Column(name = "formulario", nullable = false)
    private String form;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectMaterialState state = CollectMaterialState.PEN;

    @Column(name = "chofer", nullable = false)
    private String driver;

    @Column(name = "observacion", nullable = true)
    private String observation;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = false)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductormateriaprima", nullable = false)
    private RawMaterialProducer producer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false)
    private MetaProduct metaProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(BigDecimal netWeight) {
        this.netWeight = netWeight;
    }

    public BigDecimal getBalanceWeight() {
        return balanceWeight;
    }

    public void setBalanceWeight(BigDecimal balanceWeight) {
        this.balanceWeight = balanceWeight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public CollectMaterialState getState() {
        return state;
    }

    public void setState(CollectMaterialState state) {
        this.state = state;
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

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
