package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.jboss.seam.security.Identity;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(name = "ProducerTax_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "impuestoproductor",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "impuestoproductor")
public class ProducerTax implements BaseModel {

    @Id
    @Column(name = "idimpuestoproductor", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProducerTax_Generator")
    private Long id;

    @Column(name = "numeroformulario",nullable = false)
    private String formNumber;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "idproductormateriaprima", columnDefinition = "NUMBER(24,0)", nullable = false, updatable = false, insertable = true)
    private RawMaterialProducer rawMaterialProducerTax;

    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "idgestionimpuesto", columnDefinition = "NUMBER(24,0)", nullable = false, updatable = false, insertable = true)
    private GestionTax gestionTax;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.createdBy = getCurrentUser();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
        this.updatedBy = getCurrentUser();
    }

    private String getCurrentUser() {
        return Identity.instance().isLoggedIn() ? Identity.instance().getPrincipal().getName() : "unknown";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GestionTax getGestionTax() {
        return gestionTax;
    }

    public void setGestionTax(GestionTax gestionTax) {
        this.gestionTax = gestionTax;
    }

    public String getFormNumber() {
        return formNumber;
    }

    public void setFormNumber(String formNumber) {
        this.formNumber = formNumber;
    }

    public RawMaterialProducer getRawMaterialProducerTax() {
        return rawMaterialProducerTax;
    }

    public void setRawMaterialProducerTax(RawMaterialProducer rawMaterialProducerTax) {
        this.rawMaterialProducerTax = rawMaterialProducerTax;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
