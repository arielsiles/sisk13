package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Gender;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Jornada semanal de trabajo, por genero.
 * <p/>
 * La norma boliviana fija 48 horas semanales para hombres y 40 para mujeres, pero el numero
 * no se cablea: la norma puede cambiar y otras empresas pueden tener otra jornada. El computo
 * semanal y la planificacion leen de aca.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "WeeklyWorkload.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "jornadasemanal",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "WeeklyWorkload.findByGender",
                query = "select o from WeeklyWorkload o where o.gender = :gender and o.active = :active"),
        @NamedQuery(name = "WeeklyWorkload.findActiveList",
                query = "select o from WeeklyWorkload o where o.active = :active order by o.gender asc"),
        @NamedQuery(name = "WeeklyWorkload.countActiveByGenderButThis",
                query = "select count(o) from WeeklyWorkload o"
                        + " where o.gender = :gender and o.active = :active and o.id <> :id"),
        @NamedQuery(name = "WeeklyWorkload.countActiveByGender",
                query = "select count(o) from WeeklyWorkload o where o.gender = :gender and o.active = :active")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "jornadasemanal")
public class WeeklyWorkload implements BaseModel {

    @Id
    @Column(name = "idjornadasemanal", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WeeklyWorkload.tableGenerator")
    private Long id;

    @Column(name = "genero", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @Column(name = "horassemana", nullable = false, precision = 5, scale = 2)
    @NotNull
    private BigDecimal weeklyHours;

    /* Las horas de la jornada diaria no salen de dividir las semanales entre 5 ni entre 6:
       depende de como la empresa reparte la semana. Se configura aparte. */
    @Column(name = "horasdia", nullable = false, precision = 5, scale = 2)
    @NotNull
    private BigDecimal dailyHours;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active = Boolean.TRUE;

    @Column(name = "descripcion", length = 200)
    private String description;

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BigDecimal getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(BigDecimal weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public BigDecimal getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(BigDecimal dailyHours) {
        this.dailyHours = dailyHours;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "WeeklyWorkload{id=" + id + ", gender=" + gender + ", weeklyHours=" + weeklyHours + "}";
    }
}
