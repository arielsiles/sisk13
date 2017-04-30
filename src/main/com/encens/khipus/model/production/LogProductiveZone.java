package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "LogProductiveZone_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "LOGZONAPRODUCTIVA",
        allocationSize = 10)

@Entity
@Table(name = "LOGZONAPRODUCTIVA")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class LogProductiveZone implements BaseModel {

    @Id
    @Column(name = "IDLOGZONAPRODUCTIVA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "LogProductiveZone_Generator")
    private Long id;

    @Column(name = "FECHA", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "ESTADO", nullable = true)
    private String state;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTORMATERIAPRIMA", nullable = false)
    private RawMaterialProducer rawMaterialProducer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDZONAPRODUCTIVA", nullable = false)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
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
}
