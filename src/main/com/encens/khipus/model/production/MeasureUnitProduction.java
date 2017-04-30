package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
 */
@TableGenerator(name = "MeasureUnitProduction_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "UNIDADMEDIDA",
        allocationSize = 10)

@Entity
@Table(name = "UNIDADMEDIDAPRODUCCION", uniqueConstraints = @UniqueConstraint(columnNames = {"NOMBRE", "IDCOMPANIA"}))
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class MeasureUnitProduction implements BaseModel {

    @Id
    @Column(name = "IDUNIDADMEDIDAPRODUCCION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MeasureUnitProduction_Generator")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String name;

    @Column(name = "DESCRIPCION", nullable = true, length = 500)
    private String description;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
