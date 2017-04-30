package com.encens.khipus.model.employees;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


/**
 * @author
 * @version 3.4
 */
@NamedQueries({
        @NamedQuery(name = "FiscalProfessorPayroll.sumLiquidByPaymentType", query = "SELECT SUM(fiscalProfessorPayroll.liquid) FROM FiscalProfessorPayroll fiscalProfessorPayroll WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "FiscalProfessorPayroll.sumAndDivideLiquidByGestionPayrollListAndPaymentType", query = "SELECT SUM(fiscalProfessorPayroll.liquid/fiscalProfessorPayroll.generatedPayroll.gestionPayroll.exchangeRate.rate) FROM FiscalProfessorPayroll fiscalProfessorPayroll WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.gestionPayroll in (:gestionPayrollList) AND " +
                "fiscalProfessorPayroll.generatedPayroll.generatedPayrollType =:generatedPayrollType AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "FiscalProfessorPayroll.countByPaymentType", query = "SELECT COUNT(fiscalProfessorPayroll) FROM FiscalProfessorPayroll fiscalProfessorPayroll WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "FiscalProfessorPayroll.sumLiquidByPaymentTypeAndCurrency", query = "SELECT SUM(fiscalProfessorPayroll.liquid) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN fiscalProfessorPayroll.employee.bankAccountList bankAccount WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "FiscalProfessorPayroll.countByPaymentTypeAndCurrency", query = "SELECT COUNT(fiscalProfessorPayroll) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN fiscalProfessorPayroll.employee.bankAccountList bankAccount WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "FiscalProfessorPayroll.sumLiquidByCostCenterAndPaymentType", query = "SELECT SUM(fiscalProfessorPayroll.liquid) FROM FiscalProfessorPayroll fiscalProfessorPayroll WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.costCenterCode=:costCenterCode AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "FiscalProfessorPayroll.countByCostCenterAndPaymentType", query = "SELECT COUNT(fiscalProfessorPayroll) FROM FiscalProfessorPayroll fiscalProfessorPayroll WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.costCenterCode=:costCenterCode AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "FiscalProfessorPayroll.sumLiquidByCostCenterAndPaymentTypeAndCurrency", query = "SELECT SUM(fiscalProfessorPayroll.liquid) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN fiscalProfessorPayroll.employee.bankAccountList bankAccount WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.costCenterCode=:costCenterCode AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "FiscalProfessorPayroll.countByCostCenterAndPaymentTypeAndCurrency", query = "SELECT COUNT(fiscalProfessorPayroll) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN fiscalProfessorPayroll.employee.bankAccountList bankAccount WHERE " +
                "fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.costCenterCode=:costCenterCode AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "FiscalProfessorPayroll.findByGeneratedPayroll", query = "select cp from FiscalProfessorPayroll cp where cp.generatedPayroll=:generatedPayroll"),
        @NamedQuery(name = "FiscalProfessorPayroll.countPayrollWithNegativeAmount", query = "select count(cp) from FiscalProfessorPayroll cp where cp.generatedPayroll=:generatedPayroll and cp.liquid<0"),
        @NamedQuery(name = "FiscalProfessorPayroll.findOfficialGeneratedPayroll", query = "SELECT generatedPayroll FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN fiscalProfessorPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN gestionPayroll.jobCategory jobCategory WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " gestionPayroll.month=:month AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "FiscalProfessorPayroll.findByPayrollGenerationParameters", query = "SELECT fiscalProfessorPayroll FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN FETCH fiscalProfessorPayroll.employee employee" +
                " LEFT JOIN FETCH fiscalProfessorPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN FETCH generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN FETCH gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN FETCH gestionPayroll.jobCategory jobCategory " +
                " LEFT JOIN FETCH jobCategory.sector sector WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "FiscalProfessorPayroll.employeeWithoutBankAccount", query = "SELECT count(fiscalProfessorPayroll) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " LEFT JOIN fiscalProfessorPayroll.employee employee" +
                " LEFT JOIN fiscalProfessorPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN fiscalProfessorPayroll.employee.bankAccountList bankAccount WHERE " +
                "fiscalProfessorPayroll.generatedPayroll =:generatedPayroll AND " +
                "fiscalProfessorPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount is null"),
        @NamedQuery(name = "FiscalProfessorPayroll.countByAccountingRecord",
                query = "SELECT count(fiscalProfessorPayroll) " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " AND fiscalProfessorPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND fiscalProfessorPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "FiscalProfessorPayroll.countByAccountingRecordOrInactivePayment",
                query = "SELECT count(fiscalProfessorPayroll) " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE (fiscalProfessorPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " OR fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_FALSE) " +
                        " AND fiscalProfessorPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND fiscalProfessorPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "FiscalProfessorPayroll.updateActivePaymentToSelectItems",
                query = "UPDATE FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " SET fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " WHERE fiscalProfessorPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " AND fiscalProfessorPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND fiscalProfessorPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "FiscalProfessorPayroll.updateInactivePaymentToUnselectItems",
                query = "UPDATE FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " SET fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " WHERE fiscalProfessorPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " AND fiscalProfessorPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND fiscalProfessorPayroll.id NOT IN (:selectIdList)"),
        @NamedQuery(name = "FiscalProfessorPayroll.findSelectIdList",
                query = "SELECT fiscalProfessorPayroll.id " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_TRUE " +
                        " AND fiscalProfessorPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "FiscalProfessorPayroll.findSelectItemList",
                query = "SELECT fiscalProfessorPayroll " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND fiscalProfessorPayroll.hasActivePayment=:BOOLEAN_TRUE " +
                        " AND fiscalProfessorPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "FiscalProfessorPayroll.findEmployeesByIdList",
                query = "SELECT employee " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " left join fiscalProfessorPayroll.employee employee" +
                        " WHERE fiscalProfessorPayroll.id IN (:idList)"),
        @NamedQuery(name = "FiscalProfessorPayroll.findEmployeeIdList",
                query = "SELECT employee.id " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " left join fiscalProfessorPayroll.employee employee" +
                        " WHERE fiscalProfessorPayroll.id IN (:idList)"),
        @NamedQuery(name = "FiscalProfessorPayroll.findEmployeeIdListByGeneratedPayroll",
                query = " SELECT fiscalProfessorPayroll.employee.id FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.generatedPayroll=:generatedPayroll "),
        @NamedQuery(name = "FiscalProfessorPayroll.findEmployeeIdListByGeneratedPayrollInEmployeeIdList",
                query = " SELECT fiscalProfessorPayroll.employee.id FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.generatedPayroll=:generatedPayroll " +
                        " AND fiscalProfessorPayroll.employee.id in(:employeeIdList) "),
        @NamedQuery(name = "FiscalProfessorPayroll.findByGeneratedPayrollAndEmployeeIdList",
                query = " SELECT employee.id, fiscalProfessorPayroll FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.generatedPayroll=:generatedPayroll " +
                        " AND fiscalProfessorPayroll.employee.id in(:employeeIdList) "),
        @NamedQuery(name = "FiscalProfessorPayroll.findByGeneratedPayrollAndEmployee",
                query = "SELECT fiscalProfessorPayroll " +
                        " FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                        " WHERE fiscalProfessorPayroll.generatedPayroll=:generatedPayroll and fiscalProfessorPayroll.employee=:employee"),
        @NamedQuery(name = "FiscalProfessorPayroll.findByGeneratedPayrollList",
                query = "select fiscalProfessorPayroll from FiscalProfessorPayroll fiscalProfessorPayroll where fiscalProfessorPayroll.generatedPayroll in(:generatedPayrolls)"),

        //report summary queries                                                                                                                                
        @NamedQuery(name = "FiscalProfessorPayroll.countByCostCenter", query = "SELECT COUNT(fiscalProfessorPayroll) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " WHERE fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.costCenterCode=:costCenterCode"),
        @NamedQuery(name = "FiscalProfessorPayroll.sumTotalIncomeByCostCenter", query = "SELECT SUM(fiscalProfessorPayroll.totalIncome) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " WHERE fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND fiscalProfessorPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "FiscalProfessorPayroll.sumTotalDiscountByCostCenter", query = "SELECT SUM(fiscalProfessorPayroll.totalDiscount) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " WHERE fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND fiscalProfessorPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "FiscalProfessorPayroll.sumLiquidByCostCenter", query = "SELECT SUM(fiscalProfessorPayroll.liquid) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " WHERE fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "fiscalProfessorPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "FiscalProfessorPayroll.sumLiquidByGeneratedPayrollId", query = "SELECT SUM(fiscalProfessorPayroll.liquid) FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " WHERE fiscalProfessorPayroll.generatedPayroll.id =:generatedPayrollId "),
        @NamedQuery(name = "FiscalProfessorPayroll.loadByGeneratedPayrollList",
                query = "select element from FiscalProfessorPayroll element" +
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
                        " order by businessUnit.executorUnitCode,costCenter.id")
})
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "FiscalProfessorPayroll.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "PLANILLADOCENTELABORAL")

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "PLANILLADOCENTELABORAL")
public class FiscalProfessorPayroll implements GenericPayroll, FiscalInternalGeneralPayroll {

    @Id
    @Column(name = "IDPLANILLADOCENTELABORAL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FiscalProfessorPayroll.tableGenerator")
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

    @Column(name = "diastrabajados", nullable = true, precision = 13, scale = 2)
    private BigDecimal workedDays;

    @Column(name = "sueldobasico", nullable = false, precision = 13, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "basicoganado", nullable = false, precision = 13, scale = 2)
    private BigDecimal basicIncome;

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

    @Column(name = "totalpatronal", nullable = true, precision = 13, scale = 2)
    private BigDecimal patronalTotal;

    @Column(name = "provivienda", nullable = true, precision = 13, scale = 2)
    private BigDecimal proHome;

    @Column(name = "seguro", nullable = true, precision = 13, scale = 2)
    private BigDecimal insurance;

    @Column(name = "atrasos", nullable = true, precision = 13, scale = 2)
    private BigDecimal tardiness;

    @Column(name = "diferencia", nullable = false, precision = 13, scale = 2)
    private BigDecimal difference;

    @Column(name = "retencioniva", nullable = false, precision = 13, scale = 2)
    private BigDecimal ivaRetention;

    @Column(name = "win", nullable = false, precision = 13, scale = 2)
    private BigDecimal winDiscount;

    @Column(name = "anticipo", nullable = false, precision = 13, scale = 2)
    private BigDecimal advanceDiscount;

    @Column(name = "prestamo", nullable = false, precision = 13, scale = 2)
    private BigDecimal loanDiscount;

    @Column(name = "rciva", nullable = true, precision = 13, scale = 2)
    private BigDecimal rciva;

    @Column(name = "otrosdescuentos", nullable = false, precision = 13, scale = 2)
    private BigDecimal otherDiscounts;

    @Column(name = "descuentossinretencion", nullable = true, precision = 13, scale = 2)
    private BigDecimal discountsOutOfRetention;

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

    @Column(name = "registrocontable", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean hasAccountingRecord = Boolean.FALSE;

    @Column(name = "pagoactivo", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean hasActivePayment = Boolean.FALSE;

    @Column(name = "activogenplanfis")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean activeForTaxPayrollGeneration = Boolean.FALSE;

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

    @Column(name = "tipodepago", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "numerocuenta", length = 150)
    @Length(max = 150)
    private String accountNumber;

    @Column(name = "codigocliente", length = 150)
    @Length(max = 150)
    private String clientCod;

    @ManyToOne
    @JoinColumn(name = "IDMONEDACUENTA", referencedColumnName = "IDMONEDA")
    private Currency currency;

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

    public BigDecimal getWorkedDays() {
        return workedDays;
    }

    public PayrollGenerationType getPayrollGenerationType() {
        return null != getJobCategory() ? getJobCategory().getPayrollGenerationType() : null;
    }

    public void setWorkedDays(BigDecimal workedDays) {
        this.workedDays = workedDays;
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

    public BigDecimal getAfp() {
        return afp;
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

    public BigDecimal getWinDiscount() {
        return winDiscount;
    }

    public void setWinDiscount(BigDecimal winDiscount) {
        this.winDiscount = winDiscount;
    }

    public BigDecimal getAdvanceDiscount() {
        return advanceDiscount;
    }

    public void setAdvanceDiscount(BigDecimal advanceDiscount) {
        this.advanceDiscount = advanceDiscount;
    }

    public BigDecimal getLoanDiscount() {
        return loanDiscount;
    }

    public void setLoanDiscount(BigDecimal loanDiscount) {
        this.loanDiscount = loanDiscount;
    }

    public BigDecimal getRciva() {
        return rciva;
    }

    public void setRciva(BigDecimal rciva) {
        this.rciva = rciva;
    }

    public BigDecimal getOtherDiscounts() {
        return otherDiscounts;
    }

    public void setOtherDiscounts(BigDecimal otherDiscounts) {
        this.otherDiscounts = otherDiscounts;
    }

    public BigDecimal getDiscountsOutOfRetention() {
        return discountsOutOfRetention;
    }

    public void setDiscountsOutOfRetention(BigDecimal discountsOutOfRetention) {
        this.discountsOutOfRetention = discountsOutOfRetention;
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

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getClientCod() {
        return clientCod;
    }

    public void setClientCod(String clientCod) {
        this.clientCod = clientCod;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}