package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Un grupo de trabajo: el conjunto de personas que rota junto.
 * <p/>
 * El cronograma se planifica por grupo y no por persona, que es como lo hacen hoy: una fila por
 * grupo y una columna por dia. Planificar por persona multiplicaria el trabajo por la cantidad
 * de gente sin agregar nada, porque dentro de un grupo todos hacen el mismo turno.
 * <p/>
 * La persona pertenece a un grupo y se mueve entre grupos segun la planificacion, asi que la
 * pertenencia lleva vigencia: ver {@link WorkGroupMembership}.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "WorkGroup.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "grupotrabajo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "WorkGroup.findActive",
                query = "select o from WorkGroup o where o.active = :active order by o.name asc"),
        @NamedQuery(name = "WorkGroup.countByName",
                query = "select count(o) from WorkGroup o where o.name = :name"),
        @NamedQuery(name = "WorkGroup.countByNameButThis",
                query = "select count(o) from WorkGroup o where o.name = :name and o.id <> :id")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "grupotrabajo")
public class WorkGroup implements BaseModel {

    @Id
    @Column(name = "idgrupotrabajo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WorkGroup.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String name;

    /* Para qué area es el grupo: produccion, mantenimiento. Es texto libre porque cada empresa
       nombra sus areas distinto y no vale la pena un catalogo mas para agrupar dos filas. */
    @Column(name = "area", length = 100)
    @Length(max = 100)
    private String area;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active = Boolean.TRUE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "WorkGroup{id=" + id + ", name='" + name + "'}";
    }
}
