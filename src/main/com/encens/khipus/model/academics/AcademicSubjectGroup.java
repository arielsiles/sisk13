package com.encens.khipus.model.academics;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import javax.persistence.*;


/**
 * @author
 * @version 3.2.8
 */
@NamedQueries({
        @NamedQuery(name = "AcademicSubjectGroup.findById",
                query = "select distinct academicSubjectGroup " +
                        "from  AcademicSubjectGroup academicSubjectGroup " +
                        "where academicSubjectGroup.id.curricula=:curricula " +
                        "and academicSubjectGroup.id.asignature =:asignature " +
                        "and academicSubjectGroup.id.gestion =:gestion " +
                        "and academicSubjectGroup.id.groupType =:groupType " +
                        "and academicSubjectGroup.id.subjectGroup =:subjectGroup ")
})

@Entity
@Table(schema = Constants.ACADEMIC_SCHEMA, name = "GRUPOS_ASIGNATURAS")
public class AcademicSubjectGroup implements BaseModel {

    @EmbeddedId
    private AcademicSubjectGroupPK id;

    @Column(name = "PERIODO", insertable = false, updatable = false)
    private Integer period;

    @Column(name = "GRUPO_ASIGNATURA", length = 15, insertable = false, updatable = false)
    @Length(max = 15)
    @NotEmpty
    private String subjectGroup;

    @Column(name = "TIPO_GRUPO", length = 5, insertable = false, updatable = false)
    @Length(max = 5)
    @NotEmpty
    private String groupType;

    public String getSubjectGroup() {
        return subjectGroup;
    }

    public void setSubjectGroup(String subjectGroup) {
        this.subjectGroup = subjectGroup;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public AcademicSubjectGroupPK getId() {
        return id;
    }

    public void setId(AcademicSubjectGroupPK id) {
        this.id = id;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }
}