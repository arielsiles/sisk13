package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version 3.4
 */
@NamedQueries({
        @NamedQuery(name = "Vacation.countOverlap",
                query = "select count(vacation) from Vacation vacation" +
                        " where vacation.vacationGestion.vacationPlanning.id =:vacationPlanningId " +
                        " and vacation.id<>:vacationId and vacation.state in (:stateList) and " +
                        " ((vacation.initDate<=:initDate and vacation.endDate>=:initDate) " +
                        " or (vacation.initDate<=:endDate and vacation.endDate>=:endDate)" +
                        " or (vacation.initDate>=:initDate and vacation.endDate<=:endDate)) "),
        @NamedQuery(name = "Vacation.sumTotalDaysByVacationGestion",
                query = "select sum(vacation.totalDays) from Vacation vacation" +
                        " where vacation.vacationGestion.id=:vacationGestionId and vacation.state=:state")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Vacation.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "VACACION",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "VACACION", schema = Constants.KHIPUS_SCHEMA)
public class Vacation implements BaseModel {

    @Id
    @Column(name = "IDVACACION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Vacation.tableGenerator")
    private Long id;

    @Column(name = "DESCRIPCION", nullable = false, length = 200)
    @NotEmpty
    @Length(max = 200)
    private String description;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private VacationState state;

    @Column(name = "FECHAINICIO", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date initDate;

    @Column(name = "FECHAFIN", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date endDate;

    @Column(name = "TOTALDIAS", nullable = false)
    @NotNull
    private Integer totalDays;

    @Column(name = "DIASLIBRES", nullable = false)
    @NotNull
    private Integer daysOff;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDGESTIONVACACION", nullable = false)
    @NotNull
    private VacationGestion vacationGestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUSUARIOCREADOR", updatable = false)
    @NotNull
    private User creatorUser;

    @Column(name = "FECHACREACION", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUSUARIOEDITOR")
    private User updaterUser;

    @Column(name = "FECHAMODIFICACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "USARPARAGENPLAN", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean useForPayrollGeneration;

    @OneToOne(mappedBy = "vacation", fetch = FetchType.LAZY)
    private SpecialDate specialDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @PrePersist
    private void defineCreateValues() {
        setCreatorUser((User) Component.getInstance("currentUser"));
        setCreationDate(new Date());
    }

    @PreUpdate
    private void defineUpdateValues() {
        setUpdaterUser((User) Component.getInstance("currentUser"));
        setUpdateDate(new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VacationState getState() {
        return state;
    }

    public void setState(VacationState state) {
        this.state = state;
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

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public Integer getDaysOff() {
        return daysOff;
    }

    public void setDaysOff(Integer daysOff) {
        this.daysOff = daysOff;
    }

    public VacationGestion getVacationGestion() {
        return vacationGestion;
    }

    public void setVacationGestion(VacationGestion vacationGestion) {
        this.vacationGestion = vacationGestion;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getUpdaterUser() {
        return updaterUser;
    }

    public void setUpdaterUser(User updaterUser) {
        this.updaterUser = updaterUser;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Boolean getUseForPayrollGeneration() {
        return useForPayrollGeneration;
    }

    public void setUseForPayrollGeneration(Boolean useForPayrollGeneration) {
        this.useForPayrollGeneration = useForPayrollGeneration;
    }

    public SpecialDate getSpecialDate() {
        return specialDate;
    }

    public void setSpecialDate(SpecialDate specialDate) {
        this.specialDate = specialDate;
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
