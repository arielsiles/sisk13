package com.encens.khipus.model.production;

import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@NamedQueries(
        {
            @NamedQuery(name = "SalaryMovementGAB.getDiscount",
                        query = " select salaryMovementGAB.valor, typeMovementGAB.typeMovement, typeMovementGAB.name " +
                                " from SalaryMovementGAB salaryMovementGAB " +
                                " join SalaryMovementGAB.typeMovementGAB typeMovementGAB" +
                                " where salaryMovementGAB.date between :startDate and :endDate " +
                                " and salaryMovementGAB.productiveZone = :productiveZone ")
        }
)

@TableGenerator(name = "SalaryMovementGab_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "MOVIMIENTOSALARIOGAB",
        allocationSize = 10)

@Entity
@Table(name = "MOVIMIENTOSALARIOGAB", uniqueConstraints = @UniqueConstraint(columnNames = {"IDMOVIMIENTOSALARIOGAB", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class SalaryMovementGAB implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "IDMOVIMIENTOSALARIOGAB",nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalaryMovementGab_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "IDZONAPRODUCTIVA", nullable = false, updatable = false, insertable = true)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPOMOVIMIENTOGAB", nullable = false, updatable = false, insertable = true)
    private TypeMovementGAB typeMovementGAB;

    @Column(name = "DESCRIPCION",nullable = true)
    private String description;

    @Column(name = "ESTADO",nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionCollectionState state = ProductionCollectionState.PENDING;

    @Column(name = "FECHA", nullable = false, columnDefinition = "DATE")
    private Date date;

    @Column(name = "VALOR",columnDefinition = "DECIMAL(16,2)", nullable = false)
    private double valor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TypeMovementGAB getTypeMovementGAB() {
        return typeMovementGAB;
    }

    public void setTypeMovementGAB(TypeMovementGAB typeMovementGAB) {
        this.typeMovementGAB = typeMovementGAB;
    }

    public ProductionCollectionState getState() {
        return state;
    }

    public void setState(ProductionCollectionState state) {
        this.state = state;
    }
}
