package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Asignature
 *
 * @author Ariel Siles
 */

@NamedQueries(
        {
                @NamedQuery(name = "Asignature.findAsignature", query = "select a from Asignature a where a.asignature=:asignature")
        }
)

@Entity
@Table(name = "asignaturas", schema = Constants.ACADEMIC_SCHEMA)
public class Asignature {

    @Id
    @Column(name = "asignatura", length = 200, nullable = false, updatable = false)
    private String asignature;

    @Column(name = "nombre", length = 200, nullable = false, updatable = false, insertable = false)
    private String name;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "tipo_asignatura", length = 100, nullable = false, updatable = false, insertable = false)
    private String asignatureType;

    @Column(name = "universidad", nullable = false, updatable = false, insertable = false)
    private Integer university;

    @Column(name = "unidad", nullable = false, updatable = false, insertable = false)
    private Integer unit;

    @Column(name = "unidad_acad_adm", nullable = false, insertable = false, updatable = false)
    private Integer administrativeAcademicUnit;

    @Column(name = "area_de_conocimiento", length = 100, updatable = false, insertable = false)
    private String knowledgeArea;

    @Column(name = "sigla", length = 100, updatable = false, insertable = false)
    private String acronym;

    @Column(name = "desc_corta", length = 100, updatable = false, insertable = false)
    private String shortDescription;

    @Column(name = "carga_horaria", updatable = false, insertable = false)
    private Integer scheduleCharge;

    @Column(name = "cuota", updatable = false, insertable = false)
    private Integer quota;

    @Column(name = "creditos", updatable = false, insertable = false)
    private Integer credit;

    @Column(name = "costo", updatable = false, insertable = false)
    private Integer cost;

    @Column(name = "carga_hteorica", updatable = false, insertable = false)
    private Integer theoreticalCharge;

    @Column(name = "carga_hpractica", updatable = false, insertable = false)
    private Integer practicalCharge;

    public String getAsignature() {
        return asignature;
    }

    public void setAsignature(String asignature) {
        this.asignature = asignature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getAsignatureType() {
        return asignatureType;
    }

    public void setAsignatureType(String asignatureType) {
        this.asignatureType = asignatureType;
    }

    public Integer getUniversity() {
        return university;
    }

    public void setUniversity(Integer university) {
        this.university = university;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public Integer getAdministrativeAcademicUnit() {
        return administrativeAcademicUnit;
    }

    public void setAdministrativeAcademicUnit(Integer administrativeAcademicUnit) {
        this.administrativeAcademicUnit = administrativeAcademicUnit;
    }

    public String getKnowledgeArea() {
        return knowledgeArea;
    }

    public void setKnowledgeArea(String knowledgeArea) {
        this.knowledgeArea = knowledgeArea;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Integer getScheduleCharge() {
        return scheduleCharge;
    }

    public void setScheduleCharge(Integer scheduleCharge) {
        this.scheduleCharge = scheduleCharge;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getTheoreticalCharge() {
        return theoreticalCharge;
    }

    public void setTheoreticalCharge(Integer theoreticalCharge) {
        this.theoreticalCharge = theoreticalCharge;
    }

    public Integer getPracticalCharge() {
        return practicalCharge;
    }

    public void setPracticalCharge(Integer practicalCharge) {
        this.practicalCharge = practicalCharge;
    }
}
