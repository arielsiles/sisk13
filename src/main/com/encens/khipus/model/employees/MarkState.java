package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Holds information about mark status of employees
 *
 * @author
 * @version 3.0
 */
@NamedQuery(name = "MarkState.findByMark",
        query = "select markState from MarkState markState " +
                "where markState.mark=:mark ")
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "MarkState.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "estadomarcado")
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "estadomarcado")
public class MarkState implements BaseModel {

    @Id
    @Column(name = "idestadomarcado", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MarkState.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idrhmarcado", referencedColumnName = "IDRHMARCADO", nullable = false)
    @NotNull
    private RHMark mark;

    @Column(name = "codigomarcacion", length = 100, nullable = false)
    @Length(max = 100)
    @NotEmpty
    private String markCode;

    @Column(name = "marfecha", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date marDate;

    @Column(name = "marhora", nullable = false, updatable = false)
    @Temporal(TemporalType.TIME)
    @NotNull
    private Date marTime;

    @Column(name = "identificado", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean identified = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado")
    private Employee employee;

    @OneToMany(mappedBy = "markState", fetch = FetchType.LAZY)
    private List<MarkStateHoraryBandState> markStateHoraryBandStateList = new ArrayList<MarkStateHoraryBandState>(0);

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

    public RHMark getMark() {
        return mark;
    }

    public void setMark(RHMark mark) {
        this.mark = mark;
    }

    public String getMarkCode() {
        return markCode;
    }

    public void setMarkCode(String markCode) {
        this.markCode = markCode;
    }

    public Date getMarDate() {
        return marDate;
    }

    public void setMarDate(Date marDate) {
        this.marDate = marDate;
    }

    public Date getMarTime() {
        return marTime;
    }

    public void setMarTime(Date marTime) {
        this.marTime = marTime;
    }

    public Boolean getIdentified() {
        return identified;
    }

    public void setIdentified(Boolean identified) {
        this.identified = identified;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public List<MarkStateHoraryBandState> getMarkStateHoraryBandStateList() {
        return markStateHoraryBandStateList;
    }

    public void setMarkStateHoraryBandStateList(List<MarkStateHoraryBandState> markStateHoraryBandStateList) {
        this.markStateHoraryBandStateList = markStateHoraryBandStateList;
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