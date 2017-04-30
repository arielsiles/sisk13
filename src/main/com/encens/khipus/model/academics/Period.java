package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: Period.java  19-ago-2010 15:09:08$
 */

@NamedQueries(
        {
                @NamedQuery(name = "Period.findPeriods", query = "select distinct p.periodId from Period p order by p.periodId")
        }
)

@Entity
@Table(name = "PERIODOS", schema = Constants.ACADEMIC_SCHEMA)
public class Period {

    @Id
    @Column(name = "PERIODO", nullable = false, updatable = false)
    private Integer periodId;

    @Column(name = "GESTION", nullable = false, updatable = false, insertable = false)
    private Integer gestion;

    @Column(name = "NOMBRE", nullable = false, updatable = false, insertable = false)
    private String name;

    public Integer getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
