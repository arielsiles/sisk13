package com.encens.khipus.model.production;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 26/09/13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
import com.encens.khipus.model.admin.Company;
import org.apache.poi.hssf.record.formula.functions.True;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(name = "TypeMovementProducer_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "",
        allocationSize = 10)
@Entity
@Table(name = "TIPOMOVIMIENTOPRODUCTOR", uniqueConstraints = @UniqueConstraint(columnNames = {"IDTIPOMOVIMIENTOPRODUCTOR", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class TypeMovementProducer implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "IDTIPOMOVIMIENTOPRODUCTOR",nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TypeMovementProducer_Generator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @Column(name = "NOMBRE", nullable = false)
    private String name;

    @Column(name = "MONEDA", nullable = true)
    private String money;

    @Column(name = "TIPOMOVIMIENTO", nullable = false)
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
}
