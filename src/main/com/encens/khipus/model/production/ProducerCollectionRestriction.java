package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * Restriccion de acopio por productor (cupo diario + precios de excedente).
 *
 * Regla: para los productores con restriccion vigente, la leche hasta el cupo
 * se paga al precio normal de la planilla; el excedente (lo que pasa del cupo)
 * se paga al precio de excedente (hábil o domingo). El excedente se deriva en la
 * generacion de planillas (Modelo A: el acopio guarda el total real).
 */
@TableGenerator(name = "ProducerCollectionRestriction_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "restriccion_acopio_productor",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "restriccion_acopio_productor")
public class ProducerCollectionRestriction implements BaseModel {

    @Id
    @Column(name = "idrestriccion_acopio_productor", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProducerCollectionRestriction_Generator")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idproductormateriaprima", nullable = false)
    private RawMaterialProducer rawMaterialProducer;

    @Column(name = "cupolitrosdia", columnDefinition = "DECIMAL(16,2)", nullable = false)
    private Double maxLitersPerDay;

    @Column(name = "precioexcedentehabil", columnDefinition = "DECIMAL(9,2)", nullable = false)
    private Double excessPriceWeekday;

    @Column(name = "precioexcedentedomingo", columnDefinition = "DECIMAL(9,2)", nullable = false)
    private Double excessPriceSunday;

    @Column(name = "fechaini", columnDefinition = "DATE", nullable = false)
    private Date startDate;

    @Column(name = "fechafin", columnDefinition = "DATE", nullable = false)
    private Date endDate;

    @Column(name = "estado", columnDefinition = "VARCHAR(10)", nullable = false)
    private String state;

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

    public Double getMaxLitersPerDay() {
        return maxLitersPerDay;
    }

    public void setMaxLitersPerDay(Double maxLitersPerDay) {
        this.maxLitersPerDay = maxLitersPerDay;
    }

    public Double getExcessPriceWeekday() {
        return excessPriceWeekday;
    }

    public void setExcessPriceWeekday(Double excessPriceWeekday) {
        this.excessPriceWeekday = excessPriceWeekday;
    }

    public Double getExcessPriceSunday() {
        return excessPriceSunday;
    }

    public void setExcessPriceSunday(Double excessPriceSunday) {
        this.excessPriceSunday = excessPriceSunday;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
