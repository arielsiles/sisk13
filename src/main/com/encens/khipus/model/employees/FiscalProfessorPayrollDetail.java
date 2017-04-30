package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


/**
 * @author
 * @version 3.4
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "FiscalProfessorPayrollDetail.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "DETPLANILLADOCENTELABORAL")

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "DETPLANILLADOCENTELABORAL")
public class FiscalProfessorPayrollDetail implements BaseModel {

    @Id
    @Column(name = "IDDETPLANILLADOCENTELABORAL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FiscalProfessorPayrollDetail.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = false, updatable = false, insertable = true)
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillagenerada", nullable = false, updatable = false, insertable = true)
    private GeneratedPayroll generatedPayroll;

    @Column(name = "idplanillagenerada", nullable = false, updatable = false, insertable = false)
    private Long generatedPayrollId;

    @Column(name = "area", nullable = true)
    private String area;

    @Column(name = "unidad", nullable = true)
    private String unit;

    @Column(name = "puesto", nullable = true)
    private String job;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechainiciocontrato", nullable = true)
    private Date contractInitDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechafincontrato", nullable = true)
    private Date contractEndDate;

    @Column(name = "sueldobasico", nullable = false, precision = 13, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "basicoganado", nullable = false, precision = 13, scale = 2)
    private BigDecimal basicIncome;

    @Column(name = "totalganado", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "atrasos", nullable = true, precision = 13, scale = 2)
    private BigDecimal tardiness;

    @Column(name = "diferencia", nullable = false, precision = 13, scale = 2)
    private BigDecimal difference;

    @Column(name = "minutosatraso", nullable = true)
    private Integer tardinessMinutes;

    @Column(name = "descuentoporminutosatraso", nullable = false, precision = 13, scale = 2)
    private BigDecimal tardinessMinutesDiscount;

    @Column(name = "minutosausenciabandas", nullable = true)
    private Integer bandAbsenceMinutes;

    @Column(name = "numerominutosausenciabandas", nullable = true)
    private Integer numberBandAbsenceMinutes;

    @Column(name = "descuentoporminutosausencia", nullable = false, precision = 13, scale = 2)
    private BigDecimal absenceMinutesDiscount;

    @Column(name = "totaldescuento", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "liquidopagable", nullable = false, precision = 13, scale = 2)
    private BigDecimal liquid;

    @Column(name = "modalidadcontratacion", nullable = true)
    private String contractMode;

    @Column(name = "tipoempleado", nullable = true)
    private String kindOfEmployee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "categoria", nullable = true)
    private String category;

    @Column(name = "otrosingresos", nullable = true, precision = 13, scale = 2)
    private BigDecimal otherIncomes;

    @Column(name = "ingresofueraiva", nullable = true, precision = 13, scale = 2)
    private BigDecimal incomeOutOfIva;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "codigocencos", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "numerocompania", updatable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "codigocencos", length = 6)
    @Length(max = 6)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcargo", nullable = true)
    private Charge charge;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategory;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahorariacontrato", nullable = false)
    @NotNull
    private HoraryBandContract horaryBandContract;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public GeneratedPayroll getGeneratedPayroll() {
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public Long getGeneratedPayrollId() {
        return generatedPayrollId;
    }

    public void setGeneratedPayrollId(Long generatedPayrollId) {
        this.generatedPayrollId = generatedPayrollId;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Date getContractInitDate() {
        return contractInitDate;
    }

    public void setContractInitDate(Date contractInitDate) {
        this.contractInitDate = contractInitDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getBasicIncome() {
        return basicIncome;
    }

    public void setBasicIncome(BigDecimal basicIncome) {
        this.basicIncome = basicIncome;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTardiness() {
        return tardiness;
    }

    public void setTardiness(BigDecimal tardiness) {
        this.tardiness = tardiness;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public Integer getTardinessMinutes() {
        return tardinessMinutes;
    }

    public void setTardinessMinutes(Integer tardinessMinutes) {
        this.tardinessMinutes = tardinessMinutes;
    }

    public BigDecimal getTardinessMinutesDiscount() {
        return tardinessMinutesDiscount;
    }

    public void setTardinessMinutesDiscount(BigDecimal tardinessMinutesDiscount) {
        this.tardinessMinutesDiscount = tardinessMinutesDiscount;
    }

    public Integer getBandAbsenceMinutes() {
        return bandAbsenceMinutes;
    }

    public void setBandAbsenceMinutes(Integer bandAbsenceMinutes) {
        this.bandAbsenceMinutes = bandAbsenceMinutes;
    }

    public Integer getNumberBandAbsenceMinutes() {
        return numberBandAbsenceMinutes;
    }

    public void setNumberBandAbsenceMinutes(Integer numberBandAbsenceMinutes) {
        this.numberBandAbsenceMinutes = numberBandAbsenceMinutes;
    }

    public BigDecimal getAbsenceMinutesDiscount() {
        return absenceMinutesDiscount;
    }

    public void setAbsenceMinutesDiscount(BigDecimal absenceMinutesDiscount) {
        this.absenceMinutesDiscount = absenceMinutesDiscount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getLiquid() {
        return liquid;
    }

    public void setLiquid(BigDecimal liquid) {
        this.liquid = liquid;
    }

    public String getContractMode() {
        return contractMode;
    }

    public void setContractMode(String contractMode) {
        this.contractMode = contractMode;
    }

    public String getKindOfEmployee() {
        return kindOfEmployee;
    }

    public void setKindOfEmployee(String kindOfEmployee) {
        this.kindOfEmployee = kindOfEmployee;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getOtherIncomes() {
        return otherIncomes;
    }

    public void setOtherIncomes(BigDecimal otherIncomes) {
        this.otherIncomes = otherIncomes;
    }

    public BigDecimal getIncomeOutOfIva() {
        return incomeOutOfIva;
    }

    public void setIncomeOutOfIva(BigDecimal incomeOutOfIva) {
        this.incomeOutOfIva = incomeOutOfIva;
    }


    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

}