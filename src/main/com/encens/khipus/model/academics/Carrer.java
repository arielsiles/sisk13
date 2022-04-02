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
@Table(name = "planes_estudios", schema = Constants.ACADEMIC_SCHEMA)
public class Carrer {

    @Id
    @Column(name = "plan_estudio", nullable = false, updatable = false)
    private String studyPlan;

    @Column(name = "desc_plan", nullable = false, updatable = false, insertable = false)
    private String name;

    @Column(name = "unidad", nullable = false)
    private Integer facultyId;


    @Column(name = "unidad_acad_adm", nullable = false)
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