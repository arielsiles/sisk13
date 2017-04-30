package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * @author
 * @version 3.2.9
 */
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_tipodocs", catalog = Constants.FINANCES_SCHEMA)
public class PayableDocumentType implements BaseModel {
    @EmbeddedId
    private PayableDocumentTypePk id = new PayableDocumentTypePk();

    @Column(name = "NO_CIA", length = 2, insertable = false, updatable = false)
    private String companyNumber;

    @Column(name = "TIPO_DOC", length = 3, insertable = false, updatable = false)
    private String documentType;

    @Column(name = "DESCRI", length = 100, updatable = false)
    @Length(max = 100)
    private String description;

    @Column(name = "TIPO_MOV", length = 1)
    @Enumerated(EnumType.STRING)
    private FinanceMovementType movementType;

    /*Todo this columna must be mapped with FinancesModule and it must follow that statement FOREIGN KEY ("NO_CIA", "MODULO") REFERENCES "WISEDEV"."SF_MODULOS" ("NO_CIA", "MODULO") */
    @Column(name = "MODULO", length = 6, updatable = false)
    @Length(max = 6)
    private String module;

    @Column(name = "ESTADO", length = 3, updatable = false)
    @Enumerated(EnumType.STRING)
    private PayableDocumentTypeState state;

    @Column(name = "CLASE_DOC", length = 3, updatable = false)
    @Enumerated(EnumType.STRING)
    private PayableDocumentClass documentClass;

    @Column(name = "REGISTRO_REQUERIDO", updatable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_VALUE
            ),
            @Parameter(
                    name = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_PARAMETER,
                    value = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_VALUE
            )
    })
    private Boolean registerRequired;


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

    public PayableDocumentTypePk getId() {
        return id;
    }

    public void setId(PayableDocumentTypePk id) {
        this.id = id;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public PayableDocumentTypeState getState() {
        return state;
    }

    public void setState(PayableDocumentTypeState state) {
        this.state = state;
    }

    public PayableDocumentClass getDocumentClass() {
        return documentClass;
    }

    public void setDocumentClass(PayableDocumentClass documentClass) {
        this.documentClass = documentClass;
    }

    public Boolean getRegisterRequired() {
        return registerRequired;
    }

    public void setRegisterRequired(Boolean registerRequired) {
        this.registerRequired = registerRequired;
    }

    public String getFullName() {
        return FormatUtils.toCodeName(getDocumentType(), getDescription());
    }
}
