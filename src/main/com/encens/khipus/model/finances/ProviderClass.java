package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * @author
 * @version 3.2
 */
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_claseprov", schema = Constants.FINANCES_SCHEMA)
public class ProviderClass implements BaseModel {
    @EmbeddedId
    private ProviderClassPk id = new ProviderClassPk();

    @Column(name = "NO_CIA", updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "CLASE", updatable = false, insertable = false)
    private String code;

    @Column(name = "DESCRI", length = 100, nullable = false)
    @NotNull
    @Length(max = 100)
    private String description;

    @Column(name = "TIPO", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderClassType type;

    public ProviderClassPk getId() {
        return id;
    }

    public void setId(ProviderClassPk id) {
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

    public ProviderClassType getType() {
        return type;
    }

    public void setType(ProviderClassType type) {
        this.type = type;
    }

    public String getFullName() {
        return getCode() + " - " + getDescription();
    }

}
