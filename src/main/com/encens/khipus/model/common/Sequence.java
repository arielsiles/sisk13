package com.encens.khipus.model.common;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Sequence
 *
 * @author
 * @version 2.0
 */

@NamedQueries({
        @NamedQuery(name = "Sequence.findByName",
                query = "select sequence from Sequence sequence where sequence.name=:sequenceName")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "Sequence.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "gensecuencia",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "gensecuencia")
public class Sequence {
    @Id
    @Column(name = "idgensecuencia", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Sequence.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 250, nullable = false)
    @NotNull
    @Length(max = 250)
    private String name;

    @Column(name = "valor", nullable = false)
    @NotNull
    private long value = 1;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Sequence() {
    }

    public Sequence(String name) {
        this.name = name;
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

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
