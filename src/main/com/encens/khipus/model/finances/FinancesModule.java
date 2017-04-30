package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version 2.0
 */
@Entity
@Table(name = "sf_modulos", schema = Constants.FINANCES_SCHEMA)
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
public class FinancesModule implements BaseModel {
    @EmbeddedId
    private FinancesModulePK id = new FinancesModulePK();

    @Column(name = "DESCRI", nullable = true, length = 100)
    @Length(max = 100)
    private String description;

    @Column(name = "MONEDA", nullable = true, length = 6)
    @Length(max = 6)
    private String currency;

    @Column(name = "ESTADO", nullable = true)
    @Enumerated(EnumType.STRING)
    private FinancesModuleState state;

    @Column(name = "MES_PROCE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "TIPO_DOC_EXT", nullable = true, length = 3)
    @Length(max = 3)
    private String documentTypeExtension;


    public FinancesModulePK getId() {
        return id;
    }

    public void setId(FinancesModulePK id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public FinancesModuleState getState() {
        return state;
    }

    public void setState(FinancesModuleState state) {
        this.state = state;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDocumentTypeExtension() {
        return documentTypeExtension;
    }

    public void setDocumentTypeExtension(String documentTypeExtension) {
        this.documentTypeExtension = documentTypeExtension;
    }
}
