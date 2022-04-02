package com.encens.khipus.model.production;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 26/09/13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */

import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

@TableGenerator(name = "TypeMovementProducer_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "",
        allocationSize = 10)
@Entity
@Table(name = "tipomovimientoproductor", uniqueConstraints = @UniqueConstraint(columnNames = {"idtipomovimientoproductor", "idcompania"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class TypeMovementProducer implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "idtipomovimientoproductor",nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TypeMovementProducer_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private SalaryMovementProducerTypeEnum salaryMovementProducerTypeEnum;

    @Column(name = "moneda", nullable = true)
    private String money;

    @Column(name = "tipomovimiento", nullable = false)
    private String typeMovement;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getTypeMovement() {
        return typeMovement;
    }

    public void setTypeMovement(String typeMovement) {
        this.typeMovement = typeMovement;
    }

    public SalaryMovementProducerTypeEnum getSalaryMovementProducerTypeEnum() {
        return salaryMovementProducerTypeEnum;
    }

    public void setSalaryMovementProducerTypeEnum(SalaryMovementProducerTypeEnum salaryMovementProducerTypeEnum) {
        this.salaryMovementProducerTypeEnum = salaryMovementProducerTypeEnum;
    }
}
