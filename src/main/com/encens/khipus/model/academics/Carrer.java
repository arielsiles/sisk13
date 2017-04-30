package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Entity for Asignature
 *
 * @author Ariel Siles
 */

@NamedQueries(
        {
                //@NamedQuery(name = "Asignature.findAsignature", query = "select a from Carrer a where a.asignature=:asignature")
        }
)

@Entity
@Table(name = "PLANES_ESTUDIOS", schema = Constants.ACADEMIC_SCHEMA)
public class Carrer {

    @Id
    @Column(name = "PLAN_ESTUDIO", nullable = false, updatable = false)
    private String studyPlan;

    @Column(name = "DESC_PLAN", nullable = false, updatable = false, insertable = false)
    private String name;

    @Column(name = "UNIDAD", nullable = false)
    private Integer facultyId;


    @Column(name = "UNIDAD_ACAD_ADM", nullable = false)
    private Integer executorUnitId;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Integer facultyId) {
        this.facultyId = facultyId;
    }

    public Integer getExecutorUnitId() {
        return executorUnitId;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}