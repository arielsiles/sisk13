package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Regimen de aportes al Sistema Integral de Pensiones.
 * <p/>
 * Define, para un grupo de trabajadores, que componentes del aporte se le cobran. Reemplaza
 * los numeros de carnet que estaban escritos en RetentionAFPCalculator para eximir del aporte
 * a la cuenta individual y/o de la prima de riesgo comun a jubilados y a quienes ya cumplieron
 * la edad de jubilacion.
 * <p/>
 * Un regimen con todas las banderas en true equivale al comportamiento historico del sistema.
 *
 * @author
 * @version 6.0.129
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "SIPContributionRegime.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "regimenaportesip",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "SIPContributionRegime.findDefault",
                query = "select regime from SIPContributionRegime regime" +
                        " where regime.byDefault = :byDefault and regime.active = :active"),
        @NamedQuery(name = "SIPContributionRegime.findActive",
                query = "select regime from SIPContributionRegime regime" +
                        " where regime.active = :active order by regime.name asc"),
        @NamedQuery(name = "SIPContributionRegime.countByName",
                query = "select count(regime) from SIPContributionRegime regime where regime.name = :name"),
        @NamedQuery(name = "SIPContributionRegime.countByNameButThis",
                query = "select count(regime) from SIPContributionRegime regime" +
                        " where regime.name = :name and regime.id <> :id"),
        @NamedQuery(name = "SIPContributionRegime.countContractsByRegime",
                query = "select count(contract) from Contract contract" +
                        " where contract.sipContributionRegime = :sipContributionRegime")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "regimenaportesip")
public class SIPContributionRegime implements BaseModel {

    @Id
    @Column(name = "idregimenaportesip", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SIPContributionRegime.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String name;

    @Column(name = "descripcion", length = 500)
    @Length(max = 500)
    private String description;

    /* --- aportes del asegurado (laborales) --- */

    @Column(name = "aportacuentaindividual", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesIndividualAccount = Boolean.TRUE;

    @Column(name = "aportariesgocomun", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesCommonRisk = Boolean.TRUE;

    @Column(name = "aportasolidario", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesSolidary = Boolean.TRUE;

    @Column(name = "aportacomision", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesComission = Boolean.TRUE;

    @Column(name = "aportanacsolidario", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesNationalSolidary = Boolean.TRUE;

    /* --- aportes a cargo del empleador --- */

    @Column(name = "aportapatronal", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesPatronal = Boolean.TRUE;

    @Column(name = "aportacns", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean contributesCns = Boolean.TRUE;

    /* --- estado --- */

    @Column(name = "pordefecto", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean byDefault = Boolean.FALSE;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active = Boolean.TRUE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getContributesIndividualAccount() {
        return contributesIndividualAccount;
    }

    public void setContributesIndividualAccount(Boolean contributesIndividualAccount) {
        this.contributesIndividualAccount = contributesIndividualAccount;
    }

    public Boolean getContributesCommonRisk() {
        return contributesCommonRisk;
    }

    public void setContributesCommonRisk(Boolean contributesCommonRisk) {
        this.contributesCommonRisk = contributesCommonRisk;
    }

    public Boolean getContributesSolidary() {
        return contributesSolidary;
    }

    public void setContributesSolidary(Boolean contributesSolidary) {
        this.contributesSolidary = contributesSolidary;
    }

    public Boolean getContributesComission() {
        return contributesComission;
    }

    public void setContributesComission(Boolean contributesComission) {
        this.contributesComission = contributesComission;
    }

    public Boolean getContributesNationalSolidary() {
        return contributesNationalSolidary;
    }

    public void setContributesNationalSolidary(Boolean contributesNationalSolidary) {
        this.contributesNationalSolidary = contributesNationalSolidary;
    }

    public Boolean getContributesPatronal() {
        return contributesPatronal;
    }

    public void setContributesPatronal(Boolean contributesPatronal) {
        this.contributesPatronal = contributesPatronal;
    }

    public Boolean getContributesCns() {
        return contributesCns;
    }

    public void setContributesCns(Boolean contributesCns) {
        this.contributesCns = contributesCns;
    }

    public Boolean getByDefault() {
        return byDefault;
    }

    public void setByDefault(Boolean byDefault) {
        this.byDefault = byDefault;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SIPContributionRegime{id=" + id + ", name='" + name + "'}";
    }
}
