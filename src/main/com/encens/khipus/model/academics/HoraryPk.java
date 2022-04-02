package com.encens.khipus.model.academics;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author
 * @version 3.4
 */

@Embeddable
public class HoraryPk implements Serializable {

    @Column(name = "horario", nullable = false, updatable = false, insertable = false)
    private Long horaryId;

    @Column(name = "gestion", nullable = false, updatable = false, insertable = false)
    private Integer gestion;

    @Column(name = "periodo", nullable = false, updatable = false, insertable = false)
    private Integer period;

    public HoraryPk() {
    }

    public HoraryPk(Long horaryId, Integer gestion, Integer period) {
        this.horaryId = horaryId;
        this.gestion = gestion;
        this.period = period;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HoraryPk horaryPk = (HoraryPk) o;

        if (gestion != null ? !gestion.equals(horaryPk.gestion) : horaryPk.gestion != null) {
            return false;
        }
        if (horaryId != null ? !horaryId.equals(horaryPk.horaryId) : horaryPk.horaryId != null) {
            return false;
        }
        if (period != null ? !period.equals(horaryPk.period) : horaryPk.period != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = horaryId != null ? horaryId.hashCode() : 0;
        result = 31 * result + (gestion != null ? gestion.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        return result;
    }
}
