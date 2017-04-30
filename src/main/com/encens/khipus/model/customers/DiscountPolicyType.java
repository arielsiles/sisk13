package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Discount Policy Types
 *
 * @author:
 */

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipopoliticadescuento")
public class DiscountPolicyType implements BaseModel {

    @Id
    @Column(name = "idtipopoliticadescuento", nullable = false)
    private Long id;

    @Column(name = "recurso", nullable = false, length = 50)
    private String resourceKey;

    @Column(name = "medicion", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private DiscountPolicyMeasurementType measurement;

    @Column(name = "destino", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private DiscountPolicyTargetType target;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscountPolicyMeasurementType getMeasurement() {
        return measurement;
    }

    public void setMeasurement(DiscountPolicyMeasurementType measurement) {
        this.measurement = measurement;
    }

    public DiscountPolicyTargetType getTarget() {
        return target;
    }

    public void setTarget(DiscountPolicyTargetType target) {
        this.target = target;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
