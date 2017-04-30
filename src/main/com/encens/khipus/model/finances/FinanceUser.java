package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * @author
 * @version 2.1.2
 */

@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "usuarios", schema = Constants.FINANCES_SCHEMA)
public class FinanceUser implements BaseModel {
    @Id
    @Column(name = "NO_USR", nullable = false, length = 4)
    @Length(max = 4)
    private String id;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    @Length(max = 50)
    private String name;

    @Column(name = "ORAUSER", length = 25)
    @Length(max = 25)
    private String oracleUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOracleUser() {
        return oracleUser;
    }

    public void setOracleUser(String oracleUser) {
        this.oracleUser = oracleUser;
    }
}
