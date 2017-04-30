package com.encens.khipus.model.employees;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for GENERAL PAYROLL
 *
 * @author
 */


@NamedQueries({
        @NamedQuery(name = "GeneralPayroll.sumLiquidByPaymentType", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.sumLiquidByGestionPayrollListAndPaymentType", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.gestionPayroll in(:gestionPayrollList) AND " +
                "generalPayroll.generatedPayroll.generatedPayrollType =:generatedPayrollType AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.countByPaymentType", query = "SELECT COUNT(generalPayroll) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.sumLiquidByPaymentTypeAndCurrency", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "GeneralPayroll.countByPaymentTypeAndCurrency", query = "SELECT COUNT(generalPayroll) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "GeneralPayroll.sumIncomeByPaymentType", query = "SELECT SUM(generalPayroll.totalIncome) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.sumIncomeByPaymentTypeAndCurrency", query = "SELECT SUM(generalPayroll.totalIncome) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "GeneralPayroll.sumLiquidByCostCenterAndPaymentType", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.costCenterCode =:costCenterCode AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.countByCostCenterAndPaymentType", query = "SELECT COUNT(generalPayroll) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.costCenterCode =:costCenterCode AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.sumLiquidByCostCenterAndPaymentTypeAndCurrency", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.costCenterCode =:costCenterCode AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "GeneralPayroll.countByCostCenterAndPaymentTypeAndCurrency", query = "SELECT COUNT(generalPayroll) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.costCenterCode =:costCenterCode AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "GeneralPayroll.sumIncomeByCostCenterAndPaymentType", query = "SELECT SUM(generalPayroll.totalIncome) FROM GeneralPayroll generalPayroll WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.costCenterCode =:costCenterCode AND " +
                "generalPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "GeneralPayroll.sumIncomeByCostCenterAndPaymentTypeAndCurrency", query = "SELECT SUM(generalPayroll.totalIncome) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "generalPayroll.costCenterCode =:costCenterCode AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "GeneralPayroll.findByGeneratedPayroll", query = "select gp from GeneralPayroll gp" +
                " left join fetch gp.businessUnit businessUnit" +
                " left join fetch businessUnit.organization organization" +
                " left join fetch gp.charge charge" +
                " left join fetch gp.costCenter costCenter" +
                " left join fetch gp.employee employee" +
                " left join fetch gp.jobCategory jobCategory" +
                " where gp.generatedPayroll=:generatedPayroll"),
        @NamedQuery(name = "GeneralPayroll.countPayrollWithNegativeAmount", query = "select count(gp) from GeneralPayroll gp where gp.generatedPayroll=:generatedPayroll and gp.liquid<0"),
        @NamedQuery(name = "GeneralPayroll.findOfficialGeneratedPayroll", query = "SELECT generatedPayroll FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN gestionPayroll.jobCategory jobCategory WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " gestionPayroll.month=:month AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "GeneralPayroll.findByPayrollGenerationParameters", query = "SELECT generalPayroll FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN FETCH generalPayroll.employee employee" +
                " LEFT JOIN FETCH generalPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN FETCH generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN FETCH gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN FETCH gestionPayroll.jobCategory jobCategory " +
                " LEFT JOIN FETCH jobCategory.sector sector WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " gestionPayroll.month=:month AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "GeneralPayroll.employeeWithoutBankAccount", query = "SELECT count(generalPayroll) FROM GeneralPayroll generalPayroll " +
                " LEFT JOIN generalPayroll.employee employee" +
                " LEFT JOIN generalPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN generalPayroll.employee.bankAccountList bankAccount WHERE " +
                "generalPayroll.generatedPayroll =:generatedPayroll AND " +
                "generalPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount is null"),
        @NamedQuery(name = "GeneralPayroll.countByAccountingRecord",
                query = "SELECT count(generalPayroll) " +
                        " FROM GeneralPayroll generalPayroll " +
                        " WHERE generalPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " AND generalPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND generalPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "GeneralPayroll.countByAccountingRecordOrInactivePayment",
                query = "SELECT count(generalPayroll) " +
                        " FROM GeneralPayroll generalPayroll " +
                        " WHERE (generalPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " OR generalPayroll.hasActivePayment=:BOOLEAN_FALSE) " +
                        " AND generalPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND generalPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "GeneralPayroll.updateActivePaymentToSelectItems",
                query = "UPDATE GeneralPayroll generalPayroll " +
                        " SET generalPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " WHERE generalPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND generalPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " AND generalPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND generalPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "GeneralPayroll.updateInactivePaymentToUnselectItems",
                query = "UPDATE GeneralPayroll generalPayroll " +
                        " SET generalPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " WHERE generalPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND generalPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " AND generalPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND generalPayroll.id NOT IN (:selectIdList)"),
        @NamedQuery(name = "GeneralPayroll.findSelectIdList",
                query = "SELECT generalPayroll.id " +
                        " FROM GeneralPayroll generalPayroll " +
                        " WHERE generalPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND generalPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " AND generalPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "GeneralPayroll.findSelectItemList",
                query = "SELECT generalPayroll " +
                        " FROM GeneralPayroll generalPayroll " +
                        " WHERE generalPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND generalPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " AND generalPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "GeneralPayroll.findEmployeesByIdList",
                query = "SELECT employee " +
                        " FROM GeneralPayroll generalPayroll " +
                        " left join generalPayroll.employee employee" +
                        " WHERE generalPayroll.id IN (:idList)"),
        @NamedQuery(name = "GeneralPayroll.findEmployeeIdList",
                query = "SELECT employee.id " +
                        " FROM GeneralPayroll generalPayroll " +
                        " left join generalPayroll.employee employee" +
                        " WHERE generalPayroll.id IN (:idList)"),
        @NamedQuery(name = "GeneralPayroll.findByGeneratedPayrollAndEmployee",
                query = "SELECT generalPayroll " +
                        " FROM GeneralPayroll generalPayroll " +
                        " WHERE generalPayroll.generatedPayroll=:generatedPayroll and generalPayroll.employee=:employee"),
        @NamedQuery(name = "GeneralPayroll.loadByGeneratedPayrollList",
                query = "select element from GeneralPayroll element" +
                        " left join fetch element.charge charge" +
                        " left join fetch element.employee employee" +
                        " left join fetch element.costCenter costCenter" +
                        " left join fetch element.businessUnit businessUnit" +
                        " left join fetch element.jobCategory jobCategory" +
                        " left join fetch businessUnit.organization organization" +
                        " left join fetch element.generatedPayroll generatedPayroll" +
                        " left join fetch generatedPayroll.gestionPayroll gestionPayroll" +
                        " left join fetch gestionPayroll.exchangeRate exchangeRate " +
                        " where element.id in(:idList)" +
                        " order by businessUnit.executorUnitCode,costCenter.id"),
        //summary queries
        @NamedQuery(name = "GeneralPayroll.countByCostCenter", query = "SELECT COUNT(generalPayroll) FROM GeneralPayroll generalPayroll " +
                " WHERE generalPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND generalPayroll.costCenterCode=:costCenterCode"),
        @NamedQuery(name = "GeneralPayroll.countByGeneratedPayrollId", query = "SELECT COUNT(generalPayroll) FROM GeneralPayroll generalPayroll " +
                " WHERE generalPayroll.generatedPayroll.id =:generatedPayrollId "),
        @NamedQuery(name = "GeneralPayroll.sumTotalIncomeByCostCenter", query = "SELECT SUM(generalPayroll.totalIncome) FROM GeneralPayroll generalPayroll " +
                " WHERE generalPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND generalPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "GeneralPayroll.sumTotalDiscountByCostCenter", query = "SELECT SUM(generalPayroll.totalDiscount) FROM GeneralPayroll generalPayroll " +
                " WHERE generalPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND generalPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "GeneralPayroll.sumLiquidByCostCenter", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll " +
                " WHERE generalPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND generalPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "GeneralPayroll.sumLiquidByGeneratedPayrollId", query = "SELECT SUM(generalPayroll.liquid) FROM GeneralPayroll generalPayroll " +
                " WHERE generalPayroll.generatedPayroll.id =:generatedPayrollId ")

})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "GeneralPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillageneral",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "planillageneral")
public class GeneralPayroll implements GenericPayroll {

    @Id
    @Column(name = "idplanillageneral", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GeneralPayroll.tableGenerator")
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

    @Column(name = "diastrabajados", nullable = true)
    private Integer workedDays;

    @Column(name = "sueldo", nullable = false, precision = 13, scale = 2)
    private BigDecimal salary;

    @Column(name = "totalganado", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "afps", nullable = true, precision = 13, scale = 2)
    private BigDecimal afp;

    @Column(name = "saldoiva", nullable = true, precision = 13, scale = 2)
    private BigDecimal ivaResidue;

    @Column(name = "saldoivaanterior", nullable = true, precision = 13, scale = 2)
    private BigDecimal lastIvaResidue;

    @Column(name = "totallaboral", nullable = true, precision = 13, scale = 2)
    private BigDecimal laboralTotal;

    @Column(name = "totalpatrimonial", nullable = true, precision = 13, scale = 2)
    private BigDecimal patronalTotal;

    @Column(name = "provivienda", nullable = true, precision = 13, scale = 2)
    private BigDecimal proHome;

    @Column(name = "seguro", nullable = true, precision = 13, scale = 2)
    private BigDecimal insurance;

    @Column(name = "atrasos", nullable = false, precision = 13, scale = 2)
    private BigDecimal tardiness;

    @Column(name = "minausencia", nullable = true)
    private Integer absenceminut;

    @Column(name = "descuentoausencia", nullable = true, precision = 13, scale = 2)
    private BigDecimal absencediscount;


    @Column(name = "totalperiodosganado", nullable = true, precision = 13, scale = 2)
    private BigDecimal totalperiodearned;

    @Column(name = "totalperiodostrabajado", nullable = true, precision = 13, scale = 2)
    private BigDecimal totalperiodworked;

    @Column(name = "diferencia", nullable = false, precision = 13, scale = 2)
    private BigDecimal difference;

    @Column(name = "retencioniva", nullable = false, precision = 13, scale = 2)
    private BigDecimal ivaRetention;

    @Column(name = "otrosingresos", nullable = true, precision = 13, scale = 2)
    private BigDecimal otherIncomes;

    @Column(name = "ingresofueraiva", nullable = true, precision = 13, scale = 2)
    private BigDecimal incomeOutOfIva;

    @Column(name = "liquidopagable", nullable = false, precision = 13, scale = 2)
    private BigDecimal liquid;

    @Column(name = "otrosdescuentos", nullable = false, precision = 13, scale = 2)
    private BigDecimal otherDiscounts;

    @Column(name = "descuentossinretencion", nullable = true, precision = 13, scale = 2)
    private BigDecimal discountsOutOfRetention;

    @Column(name = "totaldescuentos", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "minutosausencia", nullable = true)
    private Integer absenceMinutes;

    @Column(name = "descuentoporminutosausencia", nullable = false, precision = 13, scale = 2)
    private BigDecimal absenceMinutesDiscount;

    @Column(name = "descuentototalporausencias", nullable = false, precision = 13, scale = 2)
    private BigDecimal absenceTotalDiscount;

    @Column(name = "modalidadcontratacion", nullable = true)
    private String contractMode;

    @Column(name = "tipoempleado", nullable = true)
    private String kindOfEmployee;
    /*0 without control,    1 whith control,    2 horary change(without control)*/
    @Column(name = "tipocontrol")
    private Integer controlType;

    @Column(name = "registrocontable", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean hasAccountingRecord = Boolean.FALSE;

    @Column(name = "pagoactivo", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean hasActivePayment = Boolean.FALSE;

    @Column(name = "activogenplanfis")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean activeForTaxPayrollGeneration = Boolean.FALSE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

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

    public Integer getControlType() {
        return controlType;
    }

    public void setControlType(Integer controlType) {
        this.controlType = controlType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }


    public Integer getAbsenceminut() {
        return absenceminut;
    }

    public void setAbsenceminut(Integer absenceminut) {
        this.absenceminut = absenceminut;
    }

    public BigDecimal getAbsencediscount() {
        return absencediscount;
    }

    public void setAbsencediscount(BigDecimal absencediscount) {
        this.absencediscount = absencediscount;
    }

    public BigDecimal getTotalperiodearned() {
        return totalperiodearned;
    }

    public void setTotalperiodearned(BigDecimal totalperiodearned) {
        this.totalperiodearned = totalperiodearned;
    }

    public BigDecimal getTotalperiodworked() {
        return totalperiodworked;
    }

    public void setTotalperiodworked(BigDecimal totalperiodworked) {
        this.totalperiodworked = totalperiodworked;
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

    public Integer getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(Integer workedDays) {
        this.workedDays = workedDays;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }


    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getAfp() {
        return afp;
    }

    public BigDecimal getRciva() {
        return null;
    }

    public BigDecimal getWinDiscount() {
        return null;
    }

    public void setAfp(BigDecimal afp) {
        this.afp = afp;
    }

    public BigDecimal getIvaResidue() {
        return ivaResidue;
    }

    public void setIvaResidue(BigDecimal ivaResidue) {
        this.ivaResidue = ivaResidue;
    }

    public BigDecimal getLastIvaResidue() {
        return lastIvaResidue;
    }

    public void setLastIvaResidue(BigDecimal lastIvaResidue) {
        this.lastIvaResidue = lastIvaResidue;
    }

    public BigDecimal getLaboralTotal() {
        return laboralTotal;
    }

    public void setLaboralTotal(BigDecimal laboralTotal) {
        this.laboralTotal = laboralTotal;
    }

    public BigDecimal getPatronalTotal() {
        return patronalTotal;
    }

    public void setPatronalTotal(BigDecimal patronalTotal) {
        this.patronalTotal = patronalTotal;
    }

    public BigDecimal getProHome() {
        return proHome;
    }

    public void setProHome(BigDecimal proHome) {
        this.proHome = proHome;
    }

    public BigDecimal getInsurance() {
        return insurance;
    }

    public void setInsurance(BigDecimal insurance) {
        this.insurance = insurance;
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

    public BigDecimal getIvaRetention() {
        return ivaRetention;
    }

    public void setIvaRetention(BigDecimal ivaRetention) {
        this.ivaRetention = ivaRetention;
    }

    public BigDecimal getLiquid() {
        return liquid;
    }

    public void setLiquid(BigDecimal liquid) {
        this.liquid = liquid;
    }

    public BigDecimal getOtherDiscounts() {
        return otherDiscounts;
    }

    public void setOtherDiscounts(BigDecimal otherDiscounts) {
        this.otherDiscounts = otherDiscounts;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Integer getAbsenceMinutes() {
        return absenceMinutes;
    }

    public void setAbsenceMinutes(Integer absenceMinutes) {
        this.absenceMinutes = absenceMinutes;
    }

    public BigDecimal getAbsenceMinutesDiscount() {
        return absenceMinutesDiscount;
    }

    public void setAbsenceMinutesDiscount(BigDecimal absenceMinutesDiscount) {
        this.absenceMinutesDiscount = absenceMinutesDiscount;
    }

    public BigDecimal getAbsenceTotalDiscount() {
        return absenceTotalDiscount;
    }

    public void setAbsenceTotalDiscount(BigDecimal absenceTotalDiscount) {
        this.absenceTotalDiscount = absenceTotalDiscount;
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

    public BigDecimal getIncomeOutOfIva() {
        return incomeOutOfIva;
    }

    public void setIncomeOutOfIva(BigDecimal incomeOutOfIva) {
        this.incomeOutOfIva = incomeOutOfIva;
    }

    public Boolean getHasAccountingRecord() {
        return hasAccountingRecord;
    }

    public void setHasAccountingRecord(Boolean hasAccountingRecord) {
        this.hasAccountingRecord = hasAccountingRecord;
    }

    public Boolean getHasActivePayment() {
        return hasActivePayment;
    }

    public void setHasActivePayment(Boolean hasActivePayment) {
        this.hasActivePayment = hasActivePayment;
    }

    public Boolean getActiveForTaxPayrollGeneration() {
        return activeForTaxPayrollGeneration;
    }

    public void setActiveForTaxPayrollGeneration(Boolean activeForTaxPayrollGeneration) {
        this.activeForTaxPayrollGeneration = activeForTaxPayrollGeneration;
    }

    public BigDecimal getOtherIncomes() {
        return otherIncomes;
    }

    public void setOtherIncomes(BigDecimal otherIncomes) {
        this.otherIncomes = otherIncomes;
    }

    public BigDecimal getDiscountsOutOfRetention() {
        return discountsOutOfRetention;
    }

    public void setDiscountsOutOfRetention(BigDecimal discountsOutOfRetention) {
        this.discountsOutOfRetention = discountsOutOfRetention;
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
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
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

    public BigDecimal getTardinessMinutesDiscount() {
        return null;
    }

    public BigDecimal getLoanDiscount() {
        return null;
    }

    public BigDecimal getAdvanceDiscount() {
        return null;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    @Override
    public String toString() {
        return "GeneralPayroll{" +
                "id=" + id +
                ", employee='" + employee + '\'' +
                ", area='" + area + '\'' +
                ", unit='" + unit + '\'' +
                ", job='" + job + '\'' +
                ", workedDays=" + workedDays +
                ", salary=" + salary +
                ", totalIncome=" + totalIncome +
                ", afp=" + afp +
                ", ivaResidue=" + ivaResidue +
                ", lastIvaResidue=" + lastIvaResidue +
                ", laboralTotal=" + laboralTotal +
                ", patronalTotal=" + patronalTotal +
                ", proHome=" + proHome +
                ", insurance=" + insurance +
                ", tardiness=" + tardiness +
                ", absenceminut=" + absenceminut +
                ", absencediscount=" + absencediscount +
                ", totalperiodearned=" + totalperiodearned +
                ", totalperiodworked=" + totalperiodworked +
                ", difference=" + difference +
                ", ivaRetention=" + ivaRetention +
                ", incomeOutOfIva=" + incomeOutOfIva +
                ", liquid=" + liquid +
                ", otherDiscounts=" + otherDiscounts +
                ", totalDiscount=" + totalDiscount +
                ", absenceMinutes=" + absenceMinutes +
                ", absenceMinutesDiscount=" + absenceMinutesDiscount +
                ", absenceTotalDiscount=" + absenceTotalDiscount +
                ", contractMode='" + contractMode + '\'' +
                ", kindOfEmployee='" + kindOfEmployee + '\'' +
                ", controlType=" + controlType +
                ", version=" + version +
                '}';
    }
}