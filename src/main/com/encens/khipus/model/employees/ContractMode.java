package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Exchange rate
 *
 * @author: Ariel Siles
 */


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ContractMode.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "modalidadcontrato",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "ContractMode.findContractMode", query = "select o from ContractMode o where o.id=:id")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "modalidadcontrato", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class ContractMode implements BaseModel {

    @Id
    @Column(name = "idmodalidadcontrato", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ContractMode.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 150)
    private String name;

    @Column(name = "descripcion")
    @Lob
    private String description;

    @Column(name = "definition", length = 150)
    private String definition;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "DURACIONDIAS", precision = 10)
    private Integer dayDuration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Integer getDayDuration() {
        return dayDuration;
    }

    public void setDayDuration(Integer dayDuration) {
        this.dayDuration = dayDuration;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}