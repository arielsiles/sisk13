package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Gender;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FiscalPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillafiscal",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "FiscalPayroll.findByPayrollGenerationCycle",
                query = "select element from FiscalPayroll element where element.payrollGenerationCycle =:payrollGenerationCycle order by element.number"),
        @NamedQuery(name = "FiscalPayroll.countByPayrollGenerationCycle",
                query = "select count(*) from FiscalPayroll element where element.payrollGenerationCycle =:payrollGenerationCycle order by element.number")
})

@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "planillafiscal", schema = Constants.KHIPUS_SCHEMA)
public class FiscalPayroll implements BaseModel {

    @Id
    @Column(name = "idplanillafiscal", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FiscalPayroll.tableGenerator")
    private Long id;

    @Column(name = "aniosantiguedad")
    private Integer seniorityYears;

    @Column(name = "bonoantiguedad", precision = 13, scale = 2, nullable = false)
    private BigDecimal seniorityBonus;

    @Column(name = "bonodominical", precision = 13, scale = 2, nullable = false)
    private BigDecimal sundayBonus;

    @Column(name = "bonoproduccion", precision = 13, scale = 2, nullable = false)
    private BigDecimal productionBonus;

    @Column(name = "ci", nullable = false)
    private String personalIdentifier;

    @Column(name = "costohorasextra", precision = 13, scale = 2, nullable = false)
    private BigDecimal extraHourCost;

    @Column(name = "diastrabajados", precision = 13, scale = 2, nullable = false)
    private BigDecimal workedDays;

    @Column(name = "diaspagados", precision = 13, scale = 2, nullable = false)
    private BigDecimal paidDays;

    @Column(name = "fechanacimiento")
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(name = "fechaingreso")
    @Temporal(TemporalType.DATE)
    private Date entranceDate;

    @Column(name = "genero", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "novedad", length = 10)
    @Enumerated(EnumType.STRING)
    private NewnessType newnessType;

    @Column(name = "haberbasico", precision = 13, scale = 2, nullable = false)
    private BigDecimal basicAmount;

    @Column(name = "horasdiapagado", precision = 13, scale = 2, nullable = false)
    private BigDecimal hourDayPayment;

    @Column(name = "horasextra", precision = 13, scale = 2, nullable = false)
    private BigDecimal extraHour;

    @Column(name = "liquidacionretencion", precision = 13, scale = 2, nullable = false)
    private BigDecimal retentionClearance;

    @Column(name = "liquidopagable", precision = 13, scale = 2, nullable = false)
    private BigDecimal liquidPayment;

    @Column(name = "numero", nullable = false)
    private Long number;

    @Column(name = "nombre", nullable = false, length = 250)
    private String name;

    @Column(name = "nacionalidad", length = 250)
    private String nationality;

    @Column(name = "ocupacion", nullable = false, length = 250)
    private String occupation;

    @Column(name = "otrosbonos", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherBonus;

    @Column(name = "otrosingresos", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherIncomes;

    @Column(name = "descuentoporminutosatraso", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal tardinessMinutesDiscount;

    @Column(name = "descuentoporminutosausencia", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal absenceMinutesDiscount;

    @Column(name = "prestamo", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal loanDiscount;

    @Column(name = "anticipo", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal advanceDiscount;

    @Column(name = "win", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal winDiscount;

    @Column(name = "otrosdescuentosmovsal", nullable = false, precision = 13, scale = 2)
    @NotNull
    private BigDecimal otherSalaryMovementDiscount;

    @Column(name = "otrosdescuentos", precision = 13, scale = 2, nullable = false)
    private BigDecimal otherDiscount;

    @Column(name = "retencionafp", precision = 13, scale = 2, nullable = false)
    private BigDecimal retentionAFP;

    @Column(name = "totalganado", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalGrained;

    @Column(name = "totaldescuentos", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalDiscount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idunidadnegocio", updatable = true, insertable = true)
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idciclogeneracionplanilla", nullable = false, updatable = false)
    private PayrollGenerationCycle payrollGenerationCycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idempleado", insertable = true, updatable = false, nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcontratopuesto", insertable = true, updatable = false, nullable = false)
    private JobContract jobContract;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillatributaria")
    private TributaryPayroll tributaryPayroll;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeniorityYears() {
        return seniorityYears;
    }

    public void setSeniorityYears(Integer seniorityYears) {
        this.seniorityYears = seniorityYears;
    }

    public BigDecimal getSeniorityBonus() {
        return seniorityBonus;
    }

    public void setSeniorityBonus(BigDecimal seniorityBonus) {
        this.seniorityBonus = seniorityBonus;
    }

    public BigDecimal getSundayBonus() {
        return sundayBonus;
    }

    public void setSundayBonus(BigDecimal sundayBonus) {
        this.sundayBonus = sundayBonus;
    }

    public BigDecimal getProductionBonus() {
        return productionBonus;
    }

    public void setProductionBonus(BigDecimal productionBonus) {
        this.productionBonus = productionBonus;
    }

    public String getPersonalIdentifier() {
        return personalIdentifier;
    }

    public void setPersonalIdentifier(String personalIdentifier) {
        this.personalIdentifier = personalIdentifier;
    }

    public BigDecimal getExtraHourCost() {
        return extraHourCost;
    }

    public void setExtraHourCost(BigDecimal extraHourCost) {
        this.extraHourCost = extraHourCost;
    }

    public BigDecimal getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(BigDecimal workedDays) {
        this.workedDays = workedDays;
    }

    public BigDecimal getPaidDays() {
        return paidDays;
    }

    public void setPaidDays(BigDecimal paidDays) {
        this.paidDays = paidDays;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getEntranceDate() {
        return entranceDate;
    }

    public void setEntranceDate(Date entranceDate) {
        this.entranceDate = entranceDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public NewnessType getNewnessType() {
        return newnessType;
    }

    public void setNewnessType(NewnessType newnessType) {
        this.newnessType = newnessType;
    }

    public BigDecimal getBasicAmount() {
        return basicAmount;
    }

    public void setBasicAmount(BigDecimal basicAmount) {
        this.basicAmount = basicAmount;
    }

    public BigDecimal getHourDayPayment() {
        return hourDayPayment;
    }

    public void setHourDayPayment(BigDecimal hourDayPayment) {
        this.hourDayPayment = hourDayPayment;
    }

    public BigDecimal getExtraHour() {
        return extraHour;
    }

    public void setExtraHour(BigDecimal extraHour) {
        this.extraHour = extraHour;
    }

    public BigDecimal getRetentionClearance() {
        return retentionClearance;
    }

    public void setRetentionClearance(BigDecimal retentionClearance) {
        this.retentionClearance = retentionClearance;
    }

    public BigDecimal getLiquidPayment() {
        return liquidPayment;
    }

    public void setLiquidPayment(BigDecimal liquidPayment) {
        this.liquidPayment = liquidPayment;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public BigDecimal getOtherBonus() {
        return otherBonus;
    }

    public void setOtherBonus(BigDecimal otherBonus) {
        this.otherBonus = otherBonus;
    }

    public BigDecimal getOtherIncomes() {
        return otherIncomes;
    }

    public void setOtherIncomes(BigDecimal otherIncomes) {
        this.otherIncomes = otherIncomes;
    }

    public BigDecimal getTardinessMinutesDiscount() {
        return tardinessMinutesDiscount;
    }

    public void setTardinessMinutesDiscount(BigDecimal tardinessMinutesDiscount) {
        this.tardinessMinutesDiscount = tardinessMinutesDiscount;
    }

    public BigDecimal getAbsenceMinutesDiscount() {
        return absenceMinutesDiscount;
    }

    public void setAbsenceMinutesDiscount(BigDecimal absenceMinutesDiscount) {
        this.absenceMinutesDiscount = absenceMinutesDiscount;
    }

    public BigDecimal getLoanDiscount() {
        return loanDiscount;
    }

    public void setLoanDiscount(BigDecimal loanDiscount) {
        this.loanDiscount = loanDiscount;
    }

    public BigDecimal getAdvanceDiscount() {
        return advanceDiscount;
    }

    public void setAdvanceDiscount(BigDecimal advanceDiscount) {
        this.advanceDiscount = advanceDiscount;
    }

    public BigDecimal getWinDiscount() {
        return winDiscount;
    }

    public void setWinDiscount(BigDecimal winDiscount) {
        this.winDiscount = winDiscount;
    }

    public BigDecimal getOtherSalaryMovementDiscount() {
        return otherSalaryMovementDiscount;
    }

    public void setOtherSalaryMovementDiscount(BigDecimal otherSalaryMovementDiscount) {
        this.otherSalaryMovementDiscount = otherSalaryMovementDiscount;
    }

    public BigDecimal getOtherDiscount() {
        return otherDiscount;
    }

    public void setOtherDiscount(BigDecimal otherDiscount) {
        this.otherDiscount = otherDiscount;
    }

    public BigDecimal getRetentionAFP() {
        return retentionAFP;
    }

    public void setRetentionAFP(BigDecimal retentionAFP) {
        this.retentionAFP = retentionAFP;
    }

    public BigDecimal getTotalGrained() {
        return totalGrained;
    }

    public void setTotalGrained(BigDecimal totalGrained) {
        this.totalGrained = totalGrained;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public TributaryPayroll getTributaryPayroll() {
        return tributaryPayroll;
    }

    public void setTributaryPayroll(TributaryPayroll tributaryPayroll) {
        this.tributaryPayroll = tributaryPayroll;
    }
}
