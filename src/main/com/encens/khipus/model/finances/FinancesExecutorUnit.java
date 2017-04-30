package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * Encens S.R.L.
 * FinancesExecutorUnit
 *
 * @author
 * @version $Id: FinancesExecutorUnit.java  29-nov-2010 16:02:35$
 */
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cg_unidades", schema = Constants.FINANCES_SCHEMA)
public class FinancesExecutorUnit implements BaseModel {

    @EmbeddedId
    private FinancesExecutorUnitPk id = new FinancesExecutorUnitPk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_UEJ", nullable = false, updatable = false, insertable = false)
    @Length(max = 6)
    private String executorUnitCode;

    @Column(name = "DESCRI", length = 100)
    private String description;

    @Column(name = "ESTADO", length = 3)
    private String state;

    public FinancesExecutorUnitPk getId() {
        return id;
    }

    public void setId(FinancesExecutorUnitPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getExecutorUnitCode() {
        return executorUnitCode;
    }

    public void setExecutorUnitCode(String executorUnitCode) {
        this.executorUnitCode = executorUnitCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}