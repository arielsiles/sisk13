package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Encens Team
 *
 * @author
 * @version : HourAvailable, 26-11-2009 07:52:17 PM
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HourAvailable.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "horadisponible",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "horadisponible")
public class HourAvailable implements BaseModel {
    @Id
    @Column(name = "idhoradisponible", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "HourAvailable.tableGenerator")
    private Long id;

    @Column(name = "fechainicio", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date initDate;

    @Column(name = "fechafin", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "horainicio", nullable = true)
    @Temporal(TemporalType.TIME)
    private Date initHour;

    @Column(name = "horafin", nullable = true)
    @Temporal(TemporalType.TIME)
    private Date endHour;

    @ManyToOne
    @JoinColumn(name = "idpostulante")
    private Postulant postulant;

    @Column(name = "dia", nullable = true)
    @Enumerated(EnumType.STRING)
    private AvailableDay availableDay;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
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

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getInitHour() {
        return initHour;
    }

    public void setInitHour(Date initHour) {
        this.initHour = initHour;
    }

    public Date getEndHour() {
        return endHour;
    }

    public void setEndHour(Date endHour) {
        this.endHour = endHour;
    }

    public Postulant getPostulant() {
        return postulant;
    }

    public void setPostulant(Postulant postulant) {
        this.postulant = postulant;
    }

    public AvailableDay getAvailableDay() {
        return availableDay;
    }

    public void setAvailableDay(AvailableDay availableDay) {
        this.availableDay = availableDay;
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
}
