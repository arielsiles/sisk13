package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for CostCenter
 *
 * @author
 * @version 1.2.1
 */


@NamedQueries({
        @NamedQuery(name = "CostCenter.findByGeneratedPayollAndEmployee", query =
                "select distinct cr.horaryBandContract.jobContract.job.organizationalUnit.costCenter" +
                        " from ControlReport cr" +
                        " where cr.generatedPayroll=:generatedPayroll and cr.horaryBandContract.jobContract.contract.employee=:employee"),
        @NamedQuery(name = "CostCenter.findMaxByGeneratedPayollAndEmployee", query =
                "select max(cr.horaryBandContract.jobContract.job.organizationalUnit.costCenter.code)" +
                        " from ControlReport cr" +
                        " where cr.generatedPayroll=:generatedPayroll and cr.horaryBandContract.jobContract.contract.employee=:employee"),
        @NamedQuery(name = "CostCenter.findByCode", query = "select cc from CostCenter cc where cc.code=:code")
})
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cg_cencos", schema = Constants.FINANCES_SCHEMA)
public class CostCenter implements BaseModel {

    @EmbeddedId
    CostCenterPk id = new CostCenterPk();

    @Column(name = "NO_CIA", updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "COD_CC", updatable = false, insertable = false)
    private String code;

    @Column(name = "DESCRI", length = 100, nullable = false)
    @NotNull
    @Length(max = 100)
    private String description;

    @Column(name = "ESTADO", nullable = false)
    @Enumerated(EnumType.STRING)
    private CostCenterState state;

    @Column(name = "GRU_CC", length = 6)
    @Length(max = 6)
    private String groupCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "GRU_CC", referencedColumnName = "GRU_CC", updatable = false, insertable = false)
    })
    private CostCenterGroup costCenterGroup;

    @Column(name = "TIPO", nullable = false)
    @Enumerated(EnumType.STRING)
    private CostCenterType type;

    @Column(name = "CONS_EXCL", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean exclusiveConsumption = false;

    @Column(name = "IND_MOV", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasMovement = false;

    public CostCenterPk getId() {
        return id;
    }

    public void setId(CostCenterPk id) {
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

    public CostCenterState getState() {
        return state;
    }

    public void setState(CostCenterState state) {
        this.state = state;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public CostCenterGroup getCostCenterGroup() {
        return costCenterGroup;
    }

    public void setCostCenterGroup(CostCenterGroup costCenterGroup) {
        this.costCenterGroup = costCenterGroup;
        setCompanyNumber(costCenterGroup != null ? costCenterGroup.getCompanyNumber() : null);
        setGroupCode(costCenterGroup != null ? costCenterGroup.getCode() : null);
    }

    public CostCenterType getType() {
        return type;
    }

    public void setType(CostCenterType type) {
        this.type = type;
    }

    public Boolean getExclusiveConsumption() {
        return exclusiveConsumption;
    }

    public void setExclusiveConsumption(Boolean exclusiveConsumption) {
        this.exclusiveConsumption = exclusiveConsumption;
    }

    public Boolean getHasMovement() {
        return hasMovement;
    }

    public void setHasMovement(Boolean hasMovement) {
        this.hasMovement = hasMovement;
    }

    public String getFullName() {
        return getCode() + " - " + getDescription();
    }

    @Override
    public String toString() {
        return "CostCenter{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", state='" + state + '\'' +
                ", groupCode='" + groupCode + '\'' +
                ", type='" + type + '\'' +
                ", exclusiveConsumption=" + exclusiveConsumption +
                '}';
    }
}
