package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;

import static com.encens.khipus.model.usertype.StringBooleanUserType.*;

/**
 * Curricula
 *
 * @author
 * @version 2.24
 */
@Entity
@Table(name = "curriculas", schema = Constants.ACADEMIC_SCHEMA)
public class Curricula {

    @EmbeddedId
    private CurriculaPk id = new CurriculaPk();

    @Column(name = "plan_estudio", insertable = false, updatable = false)
    private String curricula;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura", nullable = false, updatable = false, insertable = false)
    private Asignature asignature;

    @Column(name = "gestion", insertable = false, updatable = false)
    private Integer gestion;

    @Column(name = "periodo", insertable = false, updatable = false)
    private Integer period;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_asignatura", nullable = false, updatable = false, insertable = false)
    private AsignatureLevel asignatureLevel;

    @Column(name = "activo", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private Boolean active;

    @Column(name = "sistema", insertable = false, updatable = false)
    private Integer systemNumber;

    @Column(name = "escala", insertable = false, updatable = false)
    private String scale;

    @Column(name = "creditos", insertable = false, updatable = false)
    private Integer credit;

    @Column(name = "cuota", insertable = false, updatable = false)
    private Integer quota;

    @Column(name = "costo", insertable = false, updatable = false)
    private BigDecimal costAmount;

    public CurriculaPk getId() {
        return id;
    }

    public void setId(CurriculaPk id) {
        this.id = id;
    }

    public String getCurricula() {
        return curricula;
    }

    public void setCurricula(String curricula) {
        this.curricula = curricula;
    }

    public Asignature getAsignature() {
        return asignature;
    }

    public void setAsignature(Asignature asignature) {
        this.asignature = asignature;
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

    public AsignatureLevel getAsignatureLevel() {
        return asignatureLevel;
    }

    public void setAsignatureLevel(AsignatureLevel asignatureLevel) {
        this.asignatureLevel = asignatureLevel;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSystemNumber() {
        return systemNumber;
    }

    public void setSystemNumber(Integer systemNumber) {
        this.systemNumber = systemNumber;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount) {
        this.costAmount = costAmount;
    }
}
