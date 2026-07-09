package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * Configuracion global de precios de acopio de leche, con vigencia.
 *
 * Define los precios estandar de la quincena (hábil / domingo) y de excedente
 * (hábil / domingo). Es la fuente por defecto de precios en la generacion de
 * planillas. Un productor puede negociar su propio precio de EXCEDENTE en
 * {@link ProducerCollectionRestriction}: si ese override es &gt; 0 se usa, de lo
 * contrario se usa el precio global de esta config.
 *
 * Configuracion propia (no reutiliza CompanyConfiguration) para poder versionar
 * los precios en el tiempo (cambian constantemente).
 */
@TableGenerator(name = "MilkPriceConfig_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "precio_acopio_leche",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "precio_acopio_leche")
public class MilkPriceConfig implements BaseModel {

    @Id
    @Column(name = "idprecio_acopio_leche", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MilkPriceConfig_Generator")
    private Long id;

    @Column(name = "preciohabil", columnDefinition = "DECIMAL(9,2)", nullable = false)
    private double priceWeekday = 5.0;

    @Column(name = "preciodomingo", columnDefinition = "DECIMAL(9,2)", nullable = false)
    private double priceSunday = 4.5;

    @Column(name = "precioexcedentehabil", columnDefinition = "DECIMAL(9,2)", nullable = false)
    private double excessPriceWeekday = 4.0;

    @Column(name = "precioexcedentedomingo", columnDefinition = "DECIMAL(9,2)", nullable = false)
    private double excessPriceSunday = 4.0;

    @Column(name = "fechaini", columnDefinition = "DATE", nullable = false)
    private Date startDate;

    @Column(name = "fechafin", columnDefinition = "DATE", nullable = false)
    private Date endDate;

    @Column(name = "estado", columnDefinition = "VARCHAR(10)", nullable = false)
    private String state = "ENABLE";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPriceWeekday() {
        return priceWeekday;
    }

    public void setPriceWeekday(double priceWeekday) {
        this.priceWeekday = priceWeekday;
    }

    public double getPriceSunday() {
        return priceSunday;
    }

    public void setPriceSunday(double priceSunday) {
        this.priceSunday = priceSunday;
    }

    public double getExcessPriceWeekday() {
        return excessPriceWeekday;
    }

    public void setExcessPriceWeekday(double excessPriceWeekday) {
        this.excessPriceWeekday = excessPriceWeekday;
    }

    public double getExcessPriceSunday() {
        return excessPriceSunday;
    }

    public void setExcessPriceSunday(double excessPriceSunday) {
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
