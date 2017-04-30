package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;

import javax.persistence.*;
import java.util.Date;

/**
 * Encens S.R.L.
 *
 * @author
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HoraryBandChange.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "cambiobandahorario",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "HoraryBandChange.findAllHoraryBandChange", query = "select o from HoraryBandChange o "),
                @NamedQuery(name = "HoraryBandChange.findByCurrentIdNumber", query = "select distinct o from HoraryBandChange o " +
                        "where o.currentCredential =:currentCiNumber"
                )
        }
)
@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cambiobandahorario")
public class HoraryBandChange implements BaseModel {

    @Id
    @Column(name = "idcambiohorario", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "HoraryBandChange.tableGenerator")
    private Long id;

    @Column(name = "ciactual", nullable = true)
    private Integer currentCredential;

    @Column(name = "paterno", nullable = true)
    private String paternalLastName;

    @Column(name = "materno", nullable = true)
    private String maternalLastName;

    @Column(name = "nombres", nullable = true)
    private String names;

    @Column(name = "hinicioactual", nullable = true)
    @Temporal(TemporalType.TIME)
    private Date currentStartTime;

    @Column(name = "hfinactual", nullable = true)
    @Temporal(TemporalType.TIME)
    private Date currentEndTime;

    @Column(name = "diaactual", nullable = true)
    private String currentDay;

    @Column(name = "cinuevo")
    private Integer newCi;

    @Column(name = "paternonuevo")
    private String newPaternalLastName;

    @Column(name = "maternonuevo")
    private String newMaternalLastName;

    @Column(name = "nombresnuevo")
    private String newNames;

    @Column(name = "hinicionuevo")
    @Temporal(TemporalType.TIME)
    private Date newStartTime;

    @Column(name = "hfinnuevo")
    @Temporal(TemporalType.TIME)
    private Date newEndTime;

    @Column(name = "dianuevo")
    private String newDay;

    @Column(name = "idcarrera")
    private Integer careerId;

    @Column(name = "carrera")
    private String career;

    @Column(name = "asignatura")
    private String subject;

    @Column(name = "grupo")
    private String group;

    @Column(name = "fechacambio")
    @Temporal(TemporalType.DATE)
    private Date changeDate;

    @Column(name = "sede")
    private String location;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCurrentCredential() {
        return currentCredential;
    }

    public void setCurrentCredential(Integer currentCredential) {
        this.currentCredential = currentCredential;
    }

    public String getPaternalLastName() {
        return paternalLastName;
    }

    public void setPaternalLastName(String paternalLastName) {
        this.paternalLastName = paternalLastName;
    }

    public String getMaternalLastName() {
        return maternalLastName;
    }

    public void setMaternalLastName(String maternalLastName) {
        this.maternalLastName = maternalLastName;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public Date getCurrentStartTime() {
        return currentStartTime;
    }

    public void setCurrentStartTime(Date currentStartTime) {
        this.currentStartTime = currentStartTime;
    }

    public Date getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(Date currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public String getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(String currentDay) {
        this.currentDay = currentDay;
    }

    public Integer getNewCi() {
        return newCi;
    }

    public void setNewCi(Integer newCi) {
        this.newCi = newCi;
    }

    public String getNewPaternalLastName() {
        return newPaternalLastName;
    }

    public void setNewPaternalLastName(String newPaternalLastName) {
        this.newPaternalLastName = newPaternalLastName;
    }

    public String getNewMaternalLastName() {
        return newMaternalLastName;
    }

    public void setNewMaternalLastName(String newMaternalLastName) {
        this.newMaternalLastName = newMaternalLastName;
    }

    public String getNewNames() {
        return newNames;
    }

    public void setNewNames(String newNames) {
        this.newNames = newNames;
    }

    public Date getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(Date newStartTime) {
        this.newStartTime = newStartTime;
    }

    public Date getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(Date newEndTime) {
        this.newEndTime = newEndTime;
    }

    public String getNewDay() {
        return newDay;
    }

    public void setNewDay(String newDay) {
        this.newDay = newDay;
    }

    public Integer getCareerId() {
        return careerId;
    }

    public void setCareerId(Integer careerId) {
        this.careerId = careerId;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
