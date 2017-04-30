package com.encens.khipus.model.academics;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author
 * @version 2.12
 */
@Entity
@Table(name = "unidades", schema = Constants.ACADEMIC_SCHEMA)
public class AcademicFaculty implements BaseModel {
    @Id
    @Column(name = "UNIDAD", nullable = false)
    private Integer id;

    @Column(name = "SIGLA", length = 10, nullable = false)
    private String acronym;

    @Column(name = "DESCRIPCION", length = 60, nullable = false)
    private String description;

    @Column(name = "ACTIVA", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "UNIDAD_ACAD_ADM", nullable = false)
    private Integer executorUnitId;

    public Object getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getExecutorUnitId() {
        return executorUnitId;
    }

    public void setExecutorUnitId(Integer executorUnitId) {
        this.executorUnitId = executorUnitId;
    }
}
