package com.encens.khipus.model.academics;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * CurriculaPk
 *
 * @author
 * @version 2.24
 */
@Embeddable
public class CurriculaPk implements Serializable {

    @Column(name = "plan_estudio", nullable = false, insertable = false, updatable = false)
    private String curricula;

    @Column(name = "asignatura", nullable = false, updatable = false, insertable = true)
    private String asignature;

    @Column(name = "gestion", nullable = false, insertable = false, updatable = false)
    private Integer gestion;

    @Column(name = "periodo", nullable = false, insertable = false, updatable = false)
    private Integer period;

    @Column(name = "sistema", nullable = false, insertable = false, updatable = false)
    private Integer systemNumber;


    public String getCurricula() {
        return curricula;
    }

    public void setCurricula(String curricula) {
        this.curricula = curricula;
    }

    public String getAsignature() {
        return asignature;
    }

    public void setAsignature(String asignature) {
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

    public Integer getSystemNumber() {
        return systemNumber;
    }

    public void setSystemNumber(Integer systemNumber) {
        this.systemNumber = systemNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CurriculaPk that = (CurriculaPk) o;

        if (asignature != null ? !asignature.equals(that.asignature) : that.asignature != null) {
            return false;
        }
        if (curricula != null ? !curricula.equals(that.curricula) : that.curricula != null) {
            return false;
        }
        if (gestion != null ? !gestion.equals(that.gestion) : that.gestion != null) {
            return false;
        }
        if (period != null ? !period.equals(that.period) : that.period != null) {
            return false;
        }
        if (systemNumber != null ? !systemNumber.equals(that.systemNumber) : that.systemNumber != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = curricula != null ? curricula.hashCode() : 0;
        result = 31 * result + (asignature != null ? asignature.hashCode() : 0);
        result = 31 * result + (gestion != null ? gestion.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        result = 31 * result + (systemNumber != null ? systemNumber.hashCode() : 0);
        return result;
    }
}
