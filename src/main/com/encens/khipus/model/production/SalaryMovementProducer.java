package com.encens.khipus.model.production;

import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@NamedQueries(
        {
            @NamedQuery(name = "SalaryMovementProducer.getDiscount",
                        query = " select salaryMovementProducer.valor, typeMovementProducer.typeMovement, typeMovementProducer.name " +
                                " from SalaryMovementProducer salaryMovementProducer " +
                                " join SalaryMovementProducer.typeMovementProducer typeMovementProducer" +
                                " where salaryMovementProducer.date between :startDate and :endDate " +
                                " and salaryMovementProducer.rawMaterialProducer = :rawMaterialProducer " +
                                " and salaryMovementProducer.rawMaterialProducer.productiveZone = :productiveZone")
        }
)

@TableGenerator(name = "SalaryMovementProducer_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "movimientosalarioproductor",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "movimientosalarioproductor", uniqueConstraints = @UniqueConstraint(columnNames = {"idmovimientosalarioproductor", "idcompania"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class SalaryMovementProducer implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "idmovimientosalarioproductor",nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalaryMovementProducer_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idproductormateriaprima", nullable = false, updatable = false, insertable = true)
    private RawMaterialProducer rawMaterialProducer;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipomovimientoproductor", nullable = false, updatable = false, insertable = true)
    private TypeMovementProducer typeMovementProducer;

    @Column(name = "descripcion",nullable = true)
    private String description;

    @Column(name = "fecha", nullable = false, columnDefinition = "DATE")
    private Date date;

    @Column(name = "valor",columnDefinition = "DECIMAL(16,2)", nullable = false)
    private double valor;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionCollectionState state = ProductionCollectionState.PENDING;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = true, updatable = false, insertable = true)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

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

    public TypeMovementProducer getTypeMovementProducer() {
        return typeMovementProducer;
    }

    public void setTypeMovementProducer(TypeMovementProducer typeMovementProducer) {
        this.typeMovementProducer = typeMovementProducer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public ProductionCollectionState getState() {
        return state;
    }

    public void setState(ProductionCollectionState state) {
        this.state = state;
    }
}
