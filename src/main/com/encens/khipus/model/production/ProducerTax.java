package com.encens.khipus.model.production;


import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

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
}
