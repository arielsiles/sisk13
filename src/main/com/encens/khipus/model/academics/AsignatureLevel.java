package com.encens.khipus.model.academics;

import com.encens.khipus.util.Constants;

import javax.persistence.*;

/**
 * AsignatureLevel
 *
 * @author
 * @version 2.24
 */
@NamedQueries({
        @NamedQuery(name = "AsignatureLevel.findByGestionAndPerido",
                query = "select asignatureLevel from Curricula c left join c.asignatureLevel asignatureLevel" +
                        " where c.curricula=:career and c.asignature.asignature=:asignature and c.gestion=:gestion and c.period=:period and c.active=:active" +
                        " order by asignatureLevel.priority desc")
})
@Entity
@Table(name = "niveles_asignatura", schema = Constants.ACADEMIC_SCHEMA)
public class AsignatureLevel {
    @Id
    @Column(name = "nivel_asignatura", length = 5, updatable = false)
    private String id;

    @Column(name = "grado", updatable = false)
    private Integer priority;

    @Column(name = "unidad", length = 15, updatable = false)
    private String name;

    @Column(name = "descrip_niv_asig", length = 40, updatable = false)
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
