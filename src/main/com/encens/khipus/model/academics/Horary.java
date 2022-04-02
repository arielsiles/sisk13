package com.encens.khipus.model.academics;

import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Entity for Asignature
 *
 * @author Ariel Siles
 */

@NamedQueries(
        {
                @NamedQuery(name = "Horary.findById", query = "select h from Horary h " +
                        " where h.horaryId=:horaryId and h.gestion =:gestion and h.period =:period")
        }
)

@Entity
@Table(name = "horarios", schema = Constants.ACADEMIC_SCHEMA)
public class Horary {

    @EmbeddedId
    private HoraryPk id = new HoraryPk();

    @Column(name = "horario", nullable = false, updatable = false, insertable = false)
    private Long horaryId;

    @Column(name = "gestion", nullable = false, updatable = false, insertable = false)
    private Integer gestion;

    @Column(name = "periodo", nullable = false, updatable = false, insertable = false)
    private Integer period;

    @Column(name = "plan_estudio", nullable = false, updatable = false, insertable = false)
    private String studyPlan;

    @ManyToOne
    @JoinColumn(name = "plan_estudio", nullable = false)
    private Carrer carrer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_estudio", referencedColumnName = "planestudio", nullable = false, updatable = false, insertable = false)
    private OrganizationalUnit organizationalUnit;

    public HoraryPk getId() {
        return id;
    }

    public void setId(HoraryPk id) {
        this.id = id;
    }

    public Long getHoraryId() {
        return horaryId;
    }

    public void setHoraryId(Long horaryId) {
        this.horaryId = horaryId;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Carrer getCarrer() {
        return carrer;
    }

    public void setCarrer(Carrer carrer) {
        this.carrer = carrer;
    }

    public String getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }
}