package com.encens.khipus.model.contacts;

import javax.persistence.*;
import java.io.Serializable;

/**
 * TODO: Currently this primary key class is not used in the Entity entity because it's not
 * TODO: possible to generate values within a compound key class.
 * TODO: http://forum.hibernate.org/viewtopic.php?p=2393962 and https://jira.jboss.org/jira/browse/EJBTHREE-508
 *
 * @author
 * @version $Id: EntityPK.java 2008-8-28 11:17:08 $
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ENTITY_GENERATOR",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "ENTIDAD",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Embeddable
public class EntityPK implements Serializable {
    @Column(name = "ID_ENTIDAD", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ENTITY_GENERATOR")
    private Long id;

    @Column(name = "NO_IDENTIFICACION", nullable = false, length = 100)
    private String number;

    public EntityPK() {
    }

    public EntityPK(String number) {
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityPK entityPK = (EntityPK) o;

        if (id != null ? !id.equals(entityPK.id) : entityPK.id != null) {
            return false;
        }
        if (number != null ? !number.equals(entityPK.number) : entityPK.number != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }
}
