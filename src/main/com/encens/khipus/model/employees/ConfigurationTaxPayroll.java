package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ConfigurationTaxPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "confplanillafiscal",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "ConfigurationTaxPayroll.findByDates",
                query = "select configurationTaxPayroll from ConfigurationTaxPayroll configurationTaxPayroll where configurationTaxPayroll.startDate =:startDate and configurationTaxPayroll.endDate =:endDate and configurationTaxPayroll.businessUnit =:businessUnit"),
        @NamedQuery(name = "ConfigurationTaxPayroll.findByDatesAndMonth",
                query = "select configurationTaxPayroll from ConfigurationTaxPayroll configurationTaxPayroll where configurationTaxPayroll.startDate =:startDate and configurationTaxPayroll.endDate =:endDate and configurationTaxPayroll.month =:month and configurationTaxPayroll.businessUnit =:businessUnit")
})

@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "CONFPLANILLAFISCAL", schema = Constants.KHIPUS_SCHEMA)
public class ConfigurationTaxPayroll implements BaseModel {
    @Id
    @Column(name = "IDCONFPLANILLAFISCAL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ConfigurationTaxPayroll.tableGenerator")
    private Long id;

    @Column(name = "DESCRIPCION", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDUNIDADNEGOCIO", updatable = true, insertable = true)
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFP", updatable = true, insertable = true)
    private AFPRate afpRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFPPROFRISK", updatable = true, insertable = true)
    private AFPRate afpRateProfessionalRisk;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAAFPPROHOUS", updatable = true, insertable = true)
    private AFPRate afpRateProHousing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASACNS", updatable = true, insertable = true)
    private CNSRate cnsRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASAIVA", updatable = true, insertable = true)
    private IVARate ivaRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDTASASMN", updatable = true, insertable = true)
    private SMNRate smnRate;

    @Column(name = "FECHACREACION", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Column(name = "FECHAINICIO", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "FECHAFIN", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDGESTION", nullable = false, updatable = false, insertable = true)
    private Gestion gestion;

    @Column(name = "MES", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Month month;

    @Column(name = "TIPOCAMBIOINICIALUFV", precision = 16, scale = 6, nullable = false)
    private BigDecimal initialUfvExchangeRate = BigDecimal.ZERO;

    @Column(name = "TIPOCAMBIOFINALUFV", precision = 16, scale = 6, nullable = false)
    private BigDecimal finalUfvExchangeRate = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "configuration")
    private List<AdministrativeGestionPayroll> administrativePayrolls = new ArrayList<AdministrativeGestionPayroll>();

    @Version
    @Column(name = "VERSION")
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public AFPRate getAfpRate() {
        return afpRate;
    }

    public void setAfpRate(AFPRate afpRate) {
        this.afpRate = afpRate;
    }

    public AFPRate getAfpRateProfessionalRisk() {
        return afpRateProfessionalRisk;
    }

    public void setAfpRateProfessionalRisk(AFPRate afpRateProfessionalRisk) {
        this.afpRateProfessionalRisk = afpRateProfessionalRisk;
    }

    public AFPRate getAfpRateProHousing() {
        return afpRateProHousing;
    }

    public void setAfpRateProHousing(AFPRate afpRateProHousing) {
        this.afpRateProHousing = afpRateProHousing;
    }

    public CNSRate getCnsRate() {
        return cnsRate;
    }

    public void setCnsRate(CNSRate cnsRate) {
        this.cnsRate = cnsRate;
    }

    public IVARate getIvaRate() {
        return ivaRate;
    }

    public void setIvaRate(IVARate ivaRate) {
        this.ivaRate = ivaRate;
    }

    public SMNRate getSmnRate() {
        return smnRate;
    }

    public void setSmnRate(SMNRate smnRate) {
        this.smnRate = smnRate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getInitialUfvExchangeRate() {
        return initialUfvExchangeRate;
    }

    public void setInitialUfvExchangeRate(BigDecimal initialUfvExchangeRate) {
        this.initialUfvExchangeRate = initialUfvExchangeRate;
    }

    public BigDecimal getFinalUfvExchangeRate() {
        return finalUfvExchangeRate;
    }

    public void setFinalUfvExchangeRate(BigDecimal finalUfvExchangeRate) {
        this.finalUfvExchangeRate = finalUfvExchangeRate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<AdministrativeGestionPayroll> getAdministrativePayrolls() {
        return administrativePayrolls;
    }

    public void setAdministrativePayrolls(List<AdministrativeGestionPayroll> administrativePayrolls) {
        this.administrativePayrolls = administrativePayrolls;
    }

/*
    public List<ExtraHoursWorked> getExtraHoursWorked() {
        return extraHoursWorked;
    }

    public void setExtraHoursWorked(List<ExtraHoursWorked> extraHoursWorked) {
        this.extraHoursWorked = extraHoursWorked;
    }
*/

/*
    public List<GrantedBonus> getGrantedBonus() {
        return GrantedBonus;
    }

    public void setGrantedBonus(List<GrantedBonus> grantedBonus) {
        GrantedBonus = grantedBonus;
    }
*/

/*
    public List<InvoicesForm> getInvoicesForms() {
        return InvoicesForms;
    }

    public void setInvoicesForms(List<InvoicesForm> invoicesForms) {
        InvoicesForms = invoicesForms;
    }
*/

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
