package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * Aplicacion de un descuento (movimiento de salario del productor) en una planilla.
 *
 * Registra cuanto se cobro de un {@link SalaryMovementProducer} en un
 * {@link RawMaterialPayRecord} concreto. Da trazabilidad total (nada se olvida ni
 * se duplica) y permite revertir: al contabilizar se reduce el saldo del movimiento
 * por cada aplicacion; al anular/revertir se restaura sumando de vuelta el monto.
 *
 * Se crea en la GENERACION (provisional). El saldo del movimiento NO se toca hasta
 * CONTABILIZAR.
 */
@TableGenerator(name = "DiscountApplication_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "aplicacion_descuento_productor",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "aplicacion_descuento_productor")
@EntityListeners(CompanyListener.class)
public class DiscountApplication implements BaseModel {

    @Id
    @Column(name = "idaplicacion_descuento_productor", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DiscountApplication_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idmovimientosalarioproductor", nullable = false)
    private SalaryMovementProducer salaryMovementProducer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idregistropagomateriaprima", nullable = false)
    private RawMaterialPayRecord rawMaterialPayRecord;

    @Column(name = "montoaplicado", columnDefinition = "DECIMAL(16,2)", nullable = false)
    private double appliedAmount;

    @Column(name = "fecha", columnDefinition = "DATE", nullable = false)
    private Date date;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SalaryMovementProducer getSalaryMovementProducer() {
        return salaryMovementProducer;
    }

    public void setSalaryMovementProducer(SalaryMovementProducer salaryMovementProducer) {
        this.salaryMovementProducer = salaryMovementProducer;
    }

    public RawMaterialPayRecord getRawMaterialPayRecord() {
        return rawMaterialPayRecord;
    }

    public void setRawMaterialPayRecord(RawMaterialPayRecord rawMaterialPayRecord) {
        this.rawMaterialPayRecord = rawMaterialPayRecord;
    }

    public double getAppliedAmount() {
        return appliedAmount;
    }

    public void setAppliedAmount(double appliedAmount) {
        this.appliedAmount = appliedAmount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
