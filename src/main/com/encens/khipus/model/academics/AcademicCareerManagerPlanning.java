package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * AcademicCareerManagerPlanning
 *
 * @author
 * @version 2.24
 */
@NamedQueries({
        @NamedQuery(name = "AcademicCareerManagerPlanning.findByCodeGestiondAndPeriod",
                query = "select planning from  AcademicCareerManagerPlanning planning " +
                        "where planning.employeeCode =:employeeCode and planning.gestion =:gestion and planning.period =:period" +
                        " order by planning.seatName,planning.facultyName,planning.careerName"),
        @NamedQuery(name = "AcademicCareerManagerPlanning.findByCareerGestiondAndPeriod",
                query = "select planning from  AcademicCareerManagerPlanning planning " +
                        "where planning.careerId=:careerId and  planning.facultyId=:facultyId and  planning.seatId=:seatId " +
                        " and planning.gestion =:gestion and planning.period =:period" +
                        " order by planning.seatName,planning.facultyName,planning.careerName")
})
@Entity
@Table(name = "jefesporcarr", schema = Constants.KHIPUS_SCHEMA)
public class AcademicCareerManagerPlanning extends AcademicGeneralPlanning {
    @Id
    @Column(name = "idjefesporcarr")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
