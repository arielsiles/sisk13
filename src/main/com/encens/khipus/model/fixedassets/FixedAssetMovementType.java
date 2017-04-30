package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * Entity for FixedAssetMovementType
 *
 * @author
 * @version 2.25
 */
@NamedQueries(
        {
                @NamedQuery(name = "FixedAssetMovementType.findAll", query = "select o from FixedAssetMovementType o order by o.movementCode asc")
        }
)

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "af_tipomovs", schema = Constants.FINANCES_SCHEMA)
public class FixedAssetMovementType implements BaseModel {

    @EmbeddedId
    private FixedAssetMovementTypePk id = new FixedAssetMovementTypePk();

    /*already defined in pk, so properties in null*/
    @Column(name = "cod_mov", insertable = false, updatable = false)
    private String movementCode;

    /*already defined in pk, so properties in null*/
    @Column(name = "no_cia", insertable = false, updatable = false)
    private String companyNumber;

    @Column(name = "descri", length = 100)
    @Length(max = 100)
    private String description;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedAssetMovementTypeState fixedAssetMovementTypeState;

    @Column(name = "tipo_mov", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public FixedAssetMovementTypePk getId() {
        return id;
    }

    public void setId(FixedAssetMovementTypePk id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FixedAssetMovementTypeEnum getFixedAssetMovementTypeEnum() {
        return fixedAssetMovementTypeEnum;
    }

    public void setFixedAssetMovementTypeEnum(FixedAssetMovementTypeEnum fixedAssetMovementTypeEnum) {
        this.fixedAssetMovementTypeEnum = fixedAssetMovementTypeEnum;
    }

    public String getMovementCode() {
        return movementCode;
    }

    public void setMovementCode(String movementCode) {
        this.movementCode = movementCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public FixedAssetMovementTypeState getFixedAssetMovementTypeState() {
        return fixedAssetMovementTypeState;
    }

    public void setFixedAssetMovementTypeState(FixedAssetMovementTypeState fixedAssetMovementTypeState) {
        this.fixedAssetMovementTypeState = fixedAssetMovementTypeState;
    }

    public String getFullName() {
        return getMovementCode() + " - " + getDescription();
    }
}
