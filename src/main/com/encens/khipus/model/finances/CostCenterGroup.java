package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for CostCenter
 *
 * @author
 * @version 1.2.1
 */

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cg_grucc", schema = Constants.FINANCES_SCHEMA)
public class CostCenterGroup implements BaseModel {
    @EmbeddedId
    CostCenterGroupPk id = new CostCenterGroupPk();

    @Column(name = "NO_CIA", updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "GRU_CC", updatable = false, insertable = false)
    private String code;

    @Column(name = "DESCRI", nullable = false, length = 100)
    @Length(max = 100)
    @NotNull
    private String description;

    public CostCenterGroupPk getId() {
        return id;
    }

    public void setId(CostCenterGroupPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return getCode() + " - " + getDescription();
    }

    @Override
    public String toString() {
        return "CostCenterGroup{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}