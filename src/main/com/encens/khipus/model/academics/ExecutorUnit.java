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
 * @version 2.6
 */
@Entity
@Table(name = "unidades_acad_adm", schema = Constants.ACADEMIC_SCHEMA)
public class ExecutorUnit implements BaseModel {

    @Id
    @Column(name = "unidad_acad_adm", nullable = false)
    private Integer id;

    @Column(name = "sigla", length = 10, nullable = false)
    private String acronym;

    @Column(name = "descripcion", length = 60, nullable = false)
    private String description;

    @Column(name = "activa", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "universidad", nullable = false)
    private Integer code;

    @Column(name = "tipo_unidad_acad_adm", nullable = true)
    private Integer type;

    @Column(name = "posicion")
    private Integer position;

    public Integer getId() {
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

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
