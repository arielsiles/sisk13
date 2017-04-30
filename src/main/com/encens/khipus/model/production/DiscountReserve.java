package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(name = "DiscountReserve_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "DESCUENTORESERVA",
        allocationSize = 10)

@Entity
@Table(name = "DESCUENTORESERVA")
public class DiscountReserve implements BaseModel {

    @Id
    @Column(name = "IDDESCUENTORESERVA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DiscountReserve_Generator")
    private Long id;

    @Column(name = "MONTO", columnDefinition = "DECIMAL(16,2)", nullable = false)
    private Double amount;

    @Column(name = "FECHAINI",columnDefinition = "DATE",nullable = false)
    private Date startDate;

    @Column(name = "FECHAFIN",columnDefinition = "DATE",nullable = false)
    private Date endDate;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDDESCUENTOPRODUCTOR", nullable = false, updatable = true, insertable = true)
    private DiscountProducer discountProducer;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "IDPRODUCTORMATERIAPRIMA", nullable = false, updatable = false, insertable = true)
    private RawMaterialProducer materialProducer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public DiscountProducer getDiscountProducer() {
        return discountProducer;
    }

    public void setDiscountProducer(DiscountProducer discountProducer) {
        this.discountProducer = discountProducer;
    }

    public RawMaterialProducer getMaterialProducer() {
        return materialProducer;
    }

    public void setMaterialProducer(RawMaterialProducer materialProducer) {
        this.materialProducer = materialProducer;
    }
}
