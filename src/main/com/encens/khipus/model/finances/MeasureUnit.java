package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * MeasureUnitProduction
 *
 * @author
 * @version 2.0
 */
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "unidadmedida", schema = Constants.FINANCES_SCHEMA, uniqueConstraints = {@UniqueConstraint(columnNames = {"NO_CIA", "NOMBRE"})})
public class MeasureUnit implements BaseModel {

    @EmbeddedId
    private MeasureUnitPk id = new MeasureUnitPk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    private String companyNumber;
    @Column(name = "COD_MED", nullable = false, updatable = false, insertable = false)
    private String measureUnitCode;
    @Column(name = "NOMBRE", length = 150, nullable = false)
    @NotNull
    @Length(max = 150)
    private String name;
    @Column(name = "DESCRIPCION", length = 500)
    @Length(max = 500)
    private String description;
    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public MeasureUnitPk getId() {
        return id;
    }

    public void setId(MeasureUnitPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getMeasureUnitCode() {
        return measureUnitCode;
    }

    public void setMeasureUnitCode(String measureUnitCode) {
        this.measureUnitCode = measureUnitCode;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFullName() {
        return getMeasureUnitCode() + " - " + getName();
    }

}
