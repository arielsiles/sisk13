package com.encens.khipus.model.employees;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for MANAGERS PAYROLL
 *
 * @author
 */

@NamedQueries({
        @NamedQuery(name = "ManagersPayroll.sumLiquidByPaymentType", query = "SELECT SUM(managersPayroll.liquid) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.sumAndDivideLiquidByGestionPayrollListAndPaymentType", query = "SELECT SUM(managersPayroll.liquid/managersPayroll.generatedPayroll.gestionPayroll.exchangeRate.rate) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.gestionPayroll in (:gestionPayrollList) AND " +
                "managersPayroll.generatedPayroll.generatedPayrollType =:generatedPayrollType AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.countByPaymentType", query = "SELECT COUNT(managersPayroll) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.sumLiquidByPaymentTypeAndCurrency", query = "SELECT SUM(managersPayroll.liquid) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ManagersPayroll.countByPaymentTypeAndCurrency", query = "SELECT COUNT(managersPayroll) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ManagersPayroll.sumIncomeByPaymentType", query = "SELECT SUM(managersPayroll.totalIncome) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.sumIncomeByPaymentTypeAndCurrency", query = "SELECT SUM(managersPayroll.totalIncome) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ManagersPayroll.sumLiquidByCostCenterAndPaymentType", query = "SELECT SUM(managersPayroll.liquid) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.costCenterCode=:costCenterCode AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.countByCostCenterAndPaymentType", query = "SELECT COUNT(managersPayroll) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.costCenterCode=:costCenterCode AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.sumLiquidByCostCenterAndPaymentTypeAndCurrency", query = "SELECT SUM(managersPayroll.liquid) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.costCenterCode=:costCenterCode AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ManagersPayroll.countByCostCenterAndPaymentTypeAndCurrency", query = "SELECT COUNT(managersPayroll) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.costCenterCode=:costCenterCode AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ManagersPayroll.sumIncomeByCostCenterAndPaymentType", query = "SELECT SUM(managersPayroll.totalIncome) FROM ManagersPayroll managersPayroll WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.costCenterCode=:costCenterCode AND " +
                "managersPayroll.employee.paymentType =:paymentType"),
        @NamedQuery(name = "ManagersPayroll.sumIncomeByCostCenterAndPaymentTypeAndCurrency", query = "SELECT SUM(managersPayroll.totalIncome) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll.id =:generatedPayrollId AND " +
                "managersPayroll.costCenterCode=:costCenterCode AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount.defaultAccount =:defaultAccount AND " +
                "bankAccount.currency.id =:currencyId"),
        @NamedQuery(name = "ManagersPayroll.findByGeneratedPayroll", query = "select mp from ManagersPayroll mp" +
                " left join fetch mp.businessUnit businessUnit" +
                " left join fetch businessUnit.organization organization" +
                " left join fetch mp.charge charge" +
                " left join fetch mp.costCenter costCenter" +
                " left join fetch mp.employee employee" +
                " left join fetch mp.jobCategory jobCategory" +
                " where mp.generatedPayroll=:generatedPayroll"),
        @NamedQuery(name = "ManagersPayroll.countPayrollWithNegativeAmount", query = "select count(mp) from ManagersPayroll mp where mp.generatedPayroll=:generatedPayroll and mp.liquid<0"),
        @NamedQuery(name = "ManagersPayroll.findOfficialGeneratedPayroll", query = "SELECT generatedPayroll FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN gestionPayroll.jobCategory jobCategory WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " gestionPayroll.month=:month AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "ManagersPayroll.findByPayrollGenerationParameters", query = "SELECT managersPayroll FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN FETCH managersPayroll.employee employee" +
                " LEFT JOIN FETCH managersPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN FETCH generatedPayroll.gestionPayroll gestionPayroll" +
                " LEFT JOIN FETCH gestionPayroll.businessUnit businessUnit " +
                " LEFT JOIN FETCH gestionPayroll.jobCategory jobCategory " +
                " LEFT JOIN FETCH jobCategory.sector sector WHERE " +
                " businessUnit=:businessUnit AND " +
                " jobCategory=:jobCategory AND " +
                " gestionPayroll.gestion=:gestion AND " +
                " gestionPayroll.month=:month AND " +
                " generatedPayroll.generatedPayrollType=:generatedPayrollType"),
        @NamedQuery(name = "ManagersPayroll.employeeWithoutBankAccount", query = "SELECT count(managersPayroll) FROM ManagersPayroll managersPayroll " +
                " LEFT JOIN managersPayroll.employee employee" +
                " LEFT JOIN managersPayroll.generatedPayroll generatedPayroll" +
                " LEFT JOIN managersPayroll.employee.bankAccountList bankAccount WHERE " +
                "managersPayroll.generatedPayroll =:generatedPayroll AND " +
                "managersPayroll.employee.paymentType =:paymentType AND " +
                "bankAccount is null"),
        @NamedQuery(name = "ManagersPayroll.countByAccountingRecord",
                query = "SELECT count(managersPayroll) " +
                        " FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " AND managersPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND managersPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "ManagersPayroll.countByAccountingRecordOrInactivePayment",
                query = "SELECT count(managersPayroll) " +
                        " FROM ManagersPayroll managersPayroll " +
                        " WHERE (managersPayroll.hasAccountingRecord=:BOOLEAN_TRUE" +
                        " OR managersPayroll.hasActivePayment=:BOOLEAN_FALSE) " +
                        " AND managersPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND managersPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "ManagersPayroll.updateActivePaymentToSelectItems",
                query = "UPDATE ManagersPayroll managersPayroll " +
                        " SET managersPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " WHERE managersPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND managersPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " AND managersPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND managersPayroll.id IN (:selectIdList)"),
        @NamedQuery(name = "ManagersPayroll.updateInactivePaymentToUnselectItems",
                query = "UPDATE ManagersPayroll managersPayroll " +
                        " SET managersPayroll.hasActivePayment=:BOOLEAN_FALSE" +
                        " WHERE managersPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND managersPayroll.hasActivePayment=:BOOLEAN_TRUE" +
                        " AND managersPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)" +
                        " AND managersPayroll.id NOT IN (:selectIdList)"),
        @NamedQuery(name = "ManagersPayroll.findSelectIdList",
                query = "SELECT managersPayroll.id " +
                        " FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND managersPayroll.hasActivePayment=:BOOLEAN_TRUE " +
                        " AND managersPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "ManagersPayroll.findSelectItemList",
                query = "SELECT managersPayroll " +
                        " FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.hasAccountingRecord=:BOOLEAN_FALSE" +
                        " AND managersPayroll.hasActivePayment=:BOOLEAN_TRUE " +
                        " AND managersPayroll.generatedPayrollId IN (SELECT generatedPayroll.id FROM GeneratedPayroll generatedPayroll WHERE generatedPayroll=:generatedPayroll AND generatedPayroll.generatedPayrollType=:generatedPayrollType)"),
        @NamedQuery(name = "ManagersPayroll.findEmployeesByIdList",
                query = "SELECT employee " +
                        " FROM ManagersPayroll managersPayroll " +
                        " left join managersPayroll.employee employee" +
                        " WHERE managersPayroll.id IN (:idList)"),
        @NamedQuery(name = "ManagersPayroll.findEmployeeIdList",
                query = "SELECT employee.id " +
                        " FROM ManagersPayroll managersPayroll " +
                        " left join managersPayroll.employee employee" +
                        " WHERE managersPayroll.id IN (:idList)"),
        @NamedQuery(name = "ManagersPayroll.findEmployeeIdListByGeneratedPayroll",
                query = " SELECT managersPayroll.employee.id FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.generatedPayroll=:generatedPayroll " +
                        " AND EXISTS (select contract from Contract contract " +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and contract.employee=managersPayroll.employee and" +
                        " ((contract.initDate <=:endDate and contract.endDate is null ) or (contract.initDate<=:endDate and contract.endDate>=:initDate)))"),
        @NamedQuery(name = "ManagersPayroll.findEmployeeIdListByGeneratedPayrollInEmployeeIdList",
                query = " SELECT managersPayroll.employee.id FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.generatedPayroll=:generatedPayroll " +
                        " AND managersPayroll.employee.id in(:employeeIdList) " +
                        " AND EXISTS (select contract from Contract contract " +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and contract.employee=managersPayroll.employee and" +
                        " ((contract.initDate <=:endDate and contract.endDate is null ) or (contract.initDate<=:endDate and contract.endDate>=:initDate)))"),
        @NamedQuery(name = "ManagersPayroll.findByGeneratedPayrollAndEmployeeIdList",
                query = " SELECT employee.id, managersPayroll FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.generatedPayroll=:generatedPayroll " +
                        " AND managersPayroll.employee.id in(:employeeIdList) "),
        @NamedQuery(name = "ManagersPayroll.findByGeneratedPayrollAndEmployee",
                query = "SELECT managersPayroll " +
                        " FROM ManagersPayroll managersPayroll " +
                        " WHERE managersPayroll.generatedPayroll=:generatedPayroll and managersPayroll.employee=:employee"),
        @NamedQuery(name = "ManagersPayroll.findByGeneratedPayrollList",
                query = "select managersPayroll from ManagersPayroll managersPayroll where managersPayroll.generatedPayroll in(:generatedPayrolls)"),
        @NamedQuery(name = "ManagersPayroll.loadByGeneratedPayrollList",
                query = "select element from ManagersPayroll element" +
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
        @NamedQuery(name = "ManagersPayroll.countByCostCenter", query = "SELECT COUNT(managersPayroll) FROM ManagersPayroll managersPayroll " +
                " WHERE managersPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND managersPayroll.costCenterCode=:costCenterCode"),
        @NamedQuery(name = "ManagersPayroll.countByGeneratedPayrollId", query = "SELECT COUNT(managersPayroll) FROM ManagersPayroll managersPayroll " +
                " WHERE managersPayroll.generatedPayroll.id =:generatedPayrollId "),
        @NamedQuery(name = "ManagersPayroll.sumTotalIncomeByCostCenter", query = "SELECT SUM(managersPayroll.totalIncome) FROM ManagersPayroll managersPayroll " +
                " WHERE managersPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND managersPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "ManagersPayroll.sumTotalDiscountByCostCenter", query = "SELECT SUM(managersPayroll.totalDiscount) FROM ManagersPayroll managersPayroll " +
                " WHERE managersPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND managersPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "ManagersPayroll.sumLiquidByCostCenter", query = "SELECT SUM(managersPayroll.liquid) FROM ManagersPayroll managersPayroll " +
                " WHERE managersPayroll.generatedPayroll.id =:generatedPayrollId " +
                " AND managersPayroll.costCenterCode=:costCenterCode "),
        @NamedQuery(name = "ManagersPayroll.sumLiquidByGeneratedPayrollId", query = "SELECT SUM(managersPayroll.liquid) FROM ManagersPayroll managersPayroll " +
                " WHERE managersPayroll.generatedPayroll.id =:generatedPayrollId ")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ManagersPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillaadministrativos",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "planillaadministrativos")
public class ManagersPayroll implements GenericPayroll, FiscalInternalGeneralPayroll {

    @Id
    @Column(name = "idplanillaadministrativos", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ManagersPayroll.tableGenerator")
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

    @Column(name = "sueldo", nullable = false, precision = 13, scale = 2)
    private BigDecimal salary;

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public BigDecimal getDiscountsOutOfRetention() {
        return discountsOutOfRetention;
    }

    public void setDiscountsOutOfRetention(BigDecimal discountsOutOfRetention) {
        this.discountsOutOfRetention = discountsOutOfRetention;
    }

    public BigDecimal getRciva() {
        return rciva;
    }

    public void setRciva(BigDecimal rciva) {
        this.rciva = rciva;
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

    public BigDecimal getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(BigDecimal workedDays) {
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

    public Integer getBandAbsenceMinutes() {
        return bandAbsenceMinutes;
    }

    public void setBandAbsenceMinutes(Integer bandAbsenceMinutes) {
        this.bandAbsenceMinutes = bandAbsenceMinutes;
    }

    public BigDecimal getAbsenceMinutesDiscount() {
        return absenceMinutesDiscount;
    }

    public void setAbsenceMinutesDiscount(BigDecimal absenceMinutesDiscount) {
        this.absenceMinutesDiscount = absenceMinutesDiscount;
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

    public Integer getNumberBandAbsenceMinutes() {
        return numberBandAbsenceMinutes;
    }

    public void setNumberBandAbsenceMinutes(Integer numberBandAbsenceMinutes) {
        this.numberBandAbsenceMinutes = numberBandAbsenceMinutes;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public BigDecimal getBasicIncome() {
        return basicIncome;
    }

    public void setBasicIncome(BigDecimal basicIncome) {
        this.basicIncome = basicIncome;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public PayrollGenerationType getPayrollGenerationType() {
        return null != getJobCategory() ? getJobCategory().getPayrollGenerationType() : null;
    }

    @Override
    public String toString() {
        return "ManagersPayroll{" +
                "id=" + id +
                ", employee='" + employee + '\'' +
                ", area='" + area + '\'' +
                ", unit='" + unit + '\'' +
                ", job='" + job + '\'' +
                ", contractInitDate=" + contractInitDate +
                ", contractEndDate=" + contractEndDate +
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
                ", difference=" + difference +
                ", ivaRetention=" + ivaRetention +
                ", winDiscount=" + winDiscount +
                ", advanceDiscount=" + advanceDiscount +
                ", loanDiscount=" + loanDiscount +
                ", rciva=" + rciva +
                ", otherDiscounts=" + otherDiscounts +
                ", discountsOutOfRetention=" + discountsOutOfRetention +
                ", tardinessMinutes=" + tardinessMinutes +
                ", tardinessMinutesDiscount=" + tardinessMinutesDiscount +
                ", bandAbsenceMinutes=" + bandAbsenceMinutes +
                ", numberBandAbsenceMinutes=" + numberBandAbsenceMinutes +
                ", absenceMinutesDiscount=" + absenceMinutesDiscount +
                ", totalDiscount=" + totalDiscount +
                ", liquid=" + liquid +
                ", contractMode='" + contractMode + '\'' +
                ", kindOfEmployee='" + kindOfEmployee + '\'' +
                ", category='" + category + '\'' +
                ", otherIncomes=" + otherIncomes +
                ", incomeOutOfIva=" + incomeOutOfIva +
                ", version=" + version +
                '}';
    }
}