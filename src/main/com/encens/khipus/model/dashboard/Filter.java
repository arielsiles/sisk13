package com.encens.khipus.model.dashboard;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a filter of type interval for a widget
 *
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Filter.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "filtro",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FILTROCOMPPNL",
        uniqueConstraints = @UniqueConstraint(columnNames = {"IDFILTROCOMPPNL", "INDICE", "IDCOMPANIA"}))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TIPO", discriminatorType = DiscriminatorType.STRING, length = 20)
@org.hibernate.annotations.Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
public abstract class Filter implements Serializable, BaseModel {

    @Id
    @Column(name = "IDFILTROCOMPPNL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Filter.tableGenerator")
    private Long id;

    @Length(max = 255)
    @Column(name = "NOMBRE", length = 255, nullable = false)
    @NotNull
    private String name;

    @Length(max = 255)
    @Column(name = "DESCRIPCION", length = 255, nullable = false)
    @NotNull
    private String description;

    @Column(name = "INDICE", nullable = false)
    @NotNull
    private Integer index;

    @Column(name = "COLOR", nullable = false)
    @NotNull
    private Integer color;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

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

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
