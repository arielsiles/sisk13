package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * FinancesDocumentType
 *
 * @author
 * @version 2.10
 */
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "ck_tipodocs", schema = Constants.FINANCES_SCHEMA)
public class FinancesDocumentType implements BaseModel {

    @EmbeddedId
    private FinancesDocumentTypePk id = new FinancesDocumentTypePk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "TIPO_DOC", nullable = false, updatable = false, insertable = false)
    @Length(max = 3)
    private String documentType;

    @Column(name = "DESCRI", length = 100)
    private String description;

    @Column(name = "TIPO_MOV", length = 1)
    @Enumerated(EnumType.STRING)
    private FinanceMovementType movementType;

    @Column(name = "TIPO_COMPRO", length = 2)
    private String voucherType;

    @Column(name = "DOSIFICADOR", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean dispenser;

    @Column(name = "ESTADO", length = 3)
    private String state;

    public FinancesDocumentTypePk getId() {
        return id;
    }

    public void setId(FinancesDocumentTypePk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FinanceMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(FinanceMovementType movementType) {
        this.movementType = movementType;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public Boolean isDispenser() {
        return dispenser;
    }

    public void setDispenser(Boolean dispenser) {
        this.dispenser = dispenser;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFullName() {
        return getDocumentType() + " - " + getDescription();
    }

    @Override
    public String toString() {
        return "FinancesDocumentType{" +
                "documentType='" + documentType + '\'' +
                ", description='" + description + '\'' +
                ", movementType='" + movementType + '\'' +
                ", voucherType='" + voucherType + '\'' +
                ", dispenser=" + dispenser +
                ", state='" + state + '\'' +
                '}';
    }
}
