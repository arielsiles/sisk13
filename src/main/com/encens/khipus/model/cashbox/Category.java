package com.encens.khipus.model.cashbox;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author
 * @version 2.17
 */

@Entity
@Table(name = "CATEGORIAS", schema = Constants.CASHBOX_SCHEMA)
public class Category implements BaseModel {
    @Id
    @Column(name = "COD", nullable = false, length = 15)
    @Length(max = 15)
    private String id;

    @Column(name = "DESCRIPCION", nullable = false, length = 50)
    @Length(max = 50)
    private String description;

    @Column(name = "ACTIVO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean active;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
