package com.encens.khipus.model.finances;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.contacts.Salutation;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

import static com.encens.khipus.model.usertype.StringBooleanUserType.*;

/**
 * CompanyConfiguration
 *
 * @author
 * @version 2.22
 */
@NamedQueries({
        @NamedQuery(name = "CompanyConfiguration.findByCompany", query = "select c from CompanyConfiguration c")
})
@Entity
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "configuracion", schema = Constants.FINANCES_SCHEMA)
public class CompanyConfiguration {
    @Id
    @Column(name = "NO_CIA", nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "CTADIFTIPCAM", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String balanceExchangeRateAccountCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTADIFTIPCAM", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount balanceExchangeRateAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "IUE_RET", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount iueRetention;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "IT_RET", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount itRetention;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTACOSTPT", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaCostPT;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAALMPT", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPT;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTACOSTPV", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaCostPV;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAALMPV", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPV;

    @Column(name = "CTAANTPROVME", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String advancePaymentForeignCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAANTPROVME", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount advancePaymentForeignCurrencyAccount;

    @Column(name = "CTAANTPROVMN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String advancePaymentNationalCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAANTPROVMN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount advancePaymentNationalCurrencyAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "CTADEPTRAME", referencedColumnName = "CUENTA", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount depositInTransitForeignCurrencyAccount;

    @Column(name = "CTADEPTRAME", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String depositInTransitForeignCurrencyAccountCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "CTADEPTRAMN", referencedColumnName = "CUENTA", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount depositInTransitNationalCurrencyAccount;

    @Column(name = "CTADEPTRAMN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String depositInTransitNationalCurrencyAccountCode;

    @Column(name = "CTAALMME", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseForeignCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAALMME", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseForeignCurrencyAccount;

    @Column(name = "CTAALMMN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAALMMN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyAccount;

    @Column(name = "CTATRANSALMME", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseForeignCurrencyTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTATRANSALMME", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseForeignCurrencyTransientAccount;

    @Column(name = "CTATRANSALMMN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTATRANSALMMN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount;

    @Column(name = "CTATRANSALM1MN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyTransientAccount1Code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTATRANSALM1MN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount1;

    @Column(name = "CTATRANSALM2MN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyTransientAccount2Code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTATRANSALM2MN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount2;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAAITB", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount adjustmentForInflationAccount;

    @Column(name = "CTAAITB", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String adjustmentForInflationAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) foreign currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAIVACREFIME", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyVATFiscalCreditAccount;

    @Column(name = "CTAIVACREFIME", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String foreignCurrencyVATFiscalCreditAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) national currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAIVACREFIMN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyVATFiscalCreditAccount;

    @Column(name = "CTAIVACREFIMN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String nationalCurrencyVATFiscalCreditAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) national currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAIVACREFITRMN", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyVATFiscalCreditTransientAccount;

    @Column(name = "CTAIVACREFITRMN", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String nationalCurrencyVATFiscalCreditTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAPROVOBU", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount provisionByTangibleFixedAssetObsolescenceAccount;

    @Column(name = "CTAPROVOBU", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String provisionByTangibleFixedAssetObsolescenceAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CTAAFET", referencedColumnName = "CUENTA", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedAssetInTransitAccount;

    @Column(name = "CTAAFET", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String fixedAssetInTransitAccountCode;

    @Column(name = "NO_USR_SIS", length = 4, nullable = false)
    @Length(max = 4)
    @NotNull
    private String defaultSystemUserNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_USR_TESO", referencedColumnName = "NO_USR", nullable = false)
    @NotNull
    private FinanceUser defaultTreasuryUser;

    @Column(name = "NO_USR_RPAGOS", length = 4, nullable = false)
    @NotNull
    private String defaultPurchaseOrderRemakePaymentUserNumber;

    @Column(name = "ANIO_GEN_RPAGOS", length = 4, nullable = false)
    @NotNull
    private Integer defaultPurchaseOrderRemakeYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_USR_CONTA", referencedColumnName = "NO_USR", nullable = false)
    @NotNull
    private FinanceUser defaultAccountancyUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_USR_PRODUCCION", referencedColumnName = "NO_USR", nullable = false)
    @NotNull
    private FinanceUser defaultAccountancyUserProduction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_USR_PAGAR", referencedColumnName = "NO_USR", nullable = false)
    @NotNull
    private FinanceUser defaultPayableFinanceUser;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "PAGOCTABCOMN", referencedColumnName = "CTA_BCO", nullable = false, updatable = false, insertable = false)
    })
    private FinancesBankAccount nationalBankAccountForPayment;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "PAGOCTABCOME", referencedColumnName = "CTA_BCO", nullable = false, updatable = false, insertable = false)
    })
    private FinancesBankAccount foreignBankAccountForPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCARGODOC", nullable = false)
    private Charge defaultProfessorsCharge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDDEFTIPODOC", nullable = false)
    private DocumentType defaultDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDDEFSALMUJ", nullable = false)
    private Salutation defaultSalutationForWoman;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDDEFSALHOM", nullable = false)
    private Salutation defaultSalutationForMan;

    @Column(name = "URLREDIREVALPROG", nullable = false)
    private String scheduleEvaluationRedirectURL;

    @Column(name = "URLREDIREVALPROGEST", nullable = false)
    private String studentScheduleEvaluationRedirectURL;

    @Column(name = "URLREDIREVALPROGDOC", nullable = false)
    private String teacherScheduleEvaluationRedirectURL;

    @Column(name = "URLREDIREVALPROGJC", nullable = false)
    private String careerManagerScheduleEvaluationRedirectURL;

    @Column(name = "URLREDIREVALPROGAE", nullable = false)
    private String autoEvaluationScheduleEvaluationRedirectURL;

    @Column(name = "OCCODIFACTIVA", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean purchaseOrderCodificationEnabled;

    @Column(name = "RETENCIONPRESTAMOANTI", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean retentionForLoanAndAdvance;

    @Column(name = "PRECIOUNITARIOLECHE",columnDefinition = "DECIMAL(5,2)",nullable = true)
    private Double unitPriceMilk;

    @Column(name = "IT",columnDefinition = "DECIMAL(3,2)",nullable = true)
    private Double it;

    @Column(name = "IUE",columnDefinition = "DECIMAL(3,2)",nullable = true)
    private Double iue;

    @Column(name = "AUTOMODIFCONTRATO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean contractModificationAuthorization;

    @Column(name = "CODMODIFCONTRATO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private boolean contractModificationCode;

    @Column(name = "ACTIVOAUTDOC_TESO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private boolean treasuryDocumentsAuthorizationEnabled;

    @Column(name = "ACTIVOAUTDOC_CXP", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private boolean payablesDocumentsAuthorizationEnabled;

    @Column(name = "AGUI_BASICO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private boolean basicBasedChristmasPayroll;

    @Column(name = "HRSDIALABORAL", precision = 10, scale = 2, nullable = false)
    @NotNull
    private BigDecimal hrsWorkingDay;

    @Column(name = "TIPO_DOC_CAJA")
    private String cashBoxDocumentTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "TIPO_DOC_CAJA", referencedColumnName = "TIPO_DOC", nullable = false, insertable = false, updatable = false)
    })
    private PayableDocumentType cashBoxDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCATEGORIAPUESTODLH", referencedColumnName = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategoryDLH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCATEGORIAPUESTODTH", referencedColumnName = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategoryDTH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPOSUELDODLH", referencedColumnName = "idtiposueldo", nullable = false)
    private KindOfSalary kindOfSalaryDLH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPOSUELDODTH", referencedColumnName = "idtiposueldo", nullable = false)
    private KindOfSalary kindOfSalaryDTH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter exchangeRateBalanceCostCenter;

    @Column(name = "COD_CC", length = 8)
    @Length(max = 8)
    private String costCenterCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", unique = true, nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getBalanceExchangeRateAccountCode() {
        return balanceExchangeRateAccountCode;
    }

    public void setBalanceExchangeRateAccountCode(String balanceExchangeRateAccountCode) {
        this.balanceExchangeRateAccountCode = balanceExchangeRateAccountCode;
    }

    public CashAccount getBalanceExchangeRateAccount() {
        return balanceExchangeRateAccount;
    }

    public void setBalanceExchangeRateAccount(CashAccount balanceExchangeRateAccount) {
        this.balanceExchangeRateAccount = balanceExchangeRateAccount;
        setBalanceExchangeRateAccountCode(this.balanceExchangeRateAccount != null ? this.balanceExchangeRateAccount.getAccountCode() : null);
    }

    public String getAdvancePaymentForeignCurrencyAccountCode() {
        return advancePaymentForeignCurrencyAccountCode;
    }

    public void setAdvancePaymentForeignCurrencyAccountCode(String advancePaymentForeignCurrencyAccountCode) {
        this.advancePaymentForeignCurrencyAccountCode = advancePaymentForeignCurrencyAccountCode;
    }

    public CashAccount getAdvancePaymentForeignCurrencyAccount() {
        return advancePaymentForeignCurrencyAccount;
    }

    public void setAdvancePaymentForeignCurrencyAccount(CashAccount advancePaymentForeignCurrencyAccount) {
        this.advancePaymentForeignCurrencyAccount = advancePaymentForeignCurrencyAccount;
        setAdvancePaymentForeignCurrencyAccountCode(advancePaymentForeignCurrencyAccount != null ? advancePaymentForeignCurrencyAccount.getAccountCode() : null);
    }

    public String getAdvancePaymentNationalCurrencyAccountCode() {
        return advancePaymentNationalCurrencyAccountCode;
    }

    public void setAdvancePaymentNationalCurrencyAccountCode(String advancePaymentNationalCurrencyAccountCode) {
        this.advancePaymentNationalCurrencyAccountCode = advancePaymentNationalCurrencyAccountCode;
    }

    public CashAccount getAdvancePaymentNationalCurrencyAccount() {
        return advancePaymentNationalCurrencyAccount;
    }

    public void setAdvancePaymentNationalCurrencyAccount(CashAccount advancePaymentNationalCurrencyAccount) {
        this.advancePaymentNationalCurrencyAccount = advancePaymentNationalCurrencyAccount;
        setAdvancePaymentNationalCurrencyAccountCode(advancePaymentNationalCurrencyAccount != null ? advancePaymentNationalCurrencyAccount.getAccountCode() : null);
    }

    public String getWarehouseForeignCurrencyAccountCode() {
        return warehouseForeignCurrencyAccountCode;
    }

    public void setWarehouseForeignCurrencyAccountCode(String warehouseForeignCurrencyAccountCode) {
        this.warehouseForeignCurrencyAccountCode = warehouseForeignCurrencyAccountCode;
    }

    public CashAccount getWarehouseForeignCurrencyAccount() {
        return warehouseForeignCurrencyAccount;
    }

    public void setWarehouseForeignCurrencyAccount(CashAccount warehouseForeignCurrencyAccount) {
        this.warehouseForeignCurrencyAccount = warehouseForeignCurrencyAccount;
        setWarehouseForeignCurrencyAccountCode(warehouseForeignCurrencyAccount != null ? warehouseForeignCurrencyAccount.getAccountCode() : null);
    }

    public String getWarehouseNationalCurrencyAccountCode() {
        return warehouseNationalCurrencyAccountCode;
    }

    public void setWarehouseNationalCurrencyAccountCode(String warehouseNationalCurrencyAccountCode) {
        this.warehouseNationalCurrencyAccountCode = warehouseNationalCurrencyAccountCode;
    }

    public CashAccount getWarehouseNationalCurrencyAccount() {
        return warehouseNationalCurrencyAccount;
    }

    public void setWarehouseNationalCurrencyAccount(CashAccount warehouseNationalCurrencyAccount) {
        this.warehouseNationalCurrencyAccount = warehouseNationalCurrencyAccount;
        setWarehouseNationalCurrencyAccountCode(warehouseNationalCurrencyAccount != null ? warehouseNationalCurrencyAccount.getAccountCode() : null);
    }

    public String getWarehouseForeignCurrencyTransientAccountCode() {
        return warehouseForeignCurrencyTransientAccountCode;
    }

    public void setWarehouseForeignCurrencyTransientAccountCode(String warehouseForeignCurrencyTransientAccountCode) {
        this.warehouseForeignCurrencyTransientAccountCode = warehouseForeignCurrencyTransientAccountCode;
    }

    public CashAccount getWarehouseForeignCurrencyTransientAccount() {
        return warehouseForeignCurrencyTransientAccount;
    }

    public void setWarehouseForeignCurrencyTransientAccount(CashAccount warehouseForeignCurrencyTransientAccount) {
        this.warehouseForeignCurrencyTransientAccount = warehouseForeignCurrencyTransientAccount;
        setWarehouseForeignCurrencyTransientAccountCode(warehouseForeignCurrencyTransientAccount != null ? warehouseForeignCurrencyTransientAccount.getAccountCode() : null);
    }

    public String getWarehouseNationalCurrencyTransientAccountCode() {
        return warehouseNationalCurrencyTransientAccountCode;
    }

    public void setWarehouseNationalCurrencyTransientAccountCode(String warehouseNationalCurrencyTransientAccountCode) {
        this.warehouseNationalCurrencyTransientAccountCode = warehouseNationalCurrencyTransientAccountCode;
    }

    public CashAccount getWarehouseNationalCurrencyTransientAccount() {
        return warehouseNationalCurrencyTransientAccount;
    }

    public void setWarehouseNationalCurrencyTransientAccount(CashAccount warehouseNationalCurrencyTransientAccount) {
        this.warehouseNationalCurrencyTransientAccount = warehouseNationalCurrencyTransientAccount;
        setWarehouseNationalCurrencyTransientAccountCode(warehouseNationalCurrencyTransientAccount != null ? warehouseNationalCurrencyTransientAccount.getAccountCode() : null);
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

    public CashAccount getAdjustmentForInflationAccount() {
        return adjustmentForInflationAccount;
    }

    public void setAdjustmentForInflationAccount(CashAccount adjustmentForInflationAccount) {
        this.adjustmentForInflationAccount = adjustmentForInflationAccount;
        setAdjustmentForInflationAccountCode(this.adjustmentForInflationAccount != null ?
                this.adjustmentForInflationAccount.getAccountCode() : null);
    }

    public String getAdjustmentForInflationAccountCode() {
        return adjustmentForInflationAccountCode;
    }

    public void setAdjustmentForInflationAccountCode(String adjustmentForInflationAccountCode) {
        this.adjustmentForInflationAccountCode = adjustmentForInflationAccountCode;
    }

    public CashAccount getProvisionByTangibleFixedAssetObsolescenceAccount() {
        return provisionByTangibleFixedAssetObsolescenceAccount;
    }

    public void setProvisionByTangibleFixedAssetObsolescenceAccount(CashAccount provisionByTangibleFixedAssetObsolescenceAccount) {
        this.provisionByTangibleFixedAssetObsolescenceAccount = provisionByTangibleFixedAssetObsolescenceAccount;
        setProvisionByTangibleFixedAssetObsolescenceAccountCode(this.provisionByTangibleFixedAssetObsolescenceAccount != null ?
                this.provisionByTangibleFixedAssetObsolescenceAccount.getAccountCode() : null);

    }

    public String getProvisionByTangibleFixedAssetObsolescenceAccountCode() {
        return provisionByTangibleFixedAssetObsolescenceAccountCode;
    }

    public void setProvisionByTangibleFixedAssetObsolescenceAccountCode(String provisionByTangibleFixedAssetObsolescenceAccountCode) {
        this.provisionByTangibleFixedAssetObsolescenceAccountCode = provisionByTangibleFixedAssetObsolescenceAccountCode;
    }

    public String getDefaultSystemUserNumber() {
        return defaultSystemUserNumber;
    }

    public void setDefaultSystemUserNumber(String defaultSystemUserNumber) {
        this.defaultSystemUserNumber = defaultSystemUserNumber;
    }

    public FinanceUser getDefaultTreasuryUser() {
        return defaultTreasuryUser;
    }

    public void setDefaultTreasuryUser(FinanceUser defaultTreasuryUser) {
        this.defaultTreasuryUser = defaultTreasuryUser;
    }

    public String getDefaultPurchaseOrderRemakePaymentUserNumber() {
        return defaultPurchaseOrderRemakePaymentUserNumber;
    }

    public void setDefaultPurchaseOrderRemakePaymentUserNumber(String defaultPurchaseOrderRemakePaymentUserNumber) {
        this.defaultPurchaseOrderRemakePaymentUserNumber = defaultPurchaseOrderRemakePaymentUserNumber;
    }

    public Integer getDefaultPurchaseOrderRemakeYear() {
        return defaultPurchaseOrderRemakeYear;
    }

    public void setDefaultPurchaseOrderRemakeYear(Integer defaultPurchaseOrderRemakeYear) {
        this.defaultPurchaseOrderRemakeYear = defaultPurchaseOrderRemakeYear;
    }

    public FinanceUser getDefaultAccountancyUser() {
        return defaultAccountancyUser;
    }

    public void setDefaultAccountancyUser(FinanceUser defaultAccountancyUser) {
        this.defaultAccountancyUser = defaultAccountancyUser;
    }

    public FinanceUser getDefaultPayableFinanceUser() {
        return defaultPayableFinanceUser;
    }

    public void setDefaultPayableFinanceUser(FinanceUser defaultPayableFinanceUser) {
        this.defaultPayableFinanceUser = defaultPayableFinanceUser;
    }

    public FinancesBankAccount getNationalBankAccountForPayment() {
        return nationalBankAccountForPayment;
    }

    public void setNationalBankAccountForPayment(FinancesBankAccount nationalBankAccountForPayment) {
        this.nationalBankAccountForPayment = nationalBankAccountForPayment;
    }

    public FinancesBankAccount getForeignBankAccountForPayment() {
        return foreignBankAccountForPayment;
    }

    public void setForeignBankAccountForPayment(FinancesBankAccount foreignBankAccountForPayment) {
        this.foreignBankAccountForPayment = foreignBankAccountForPayment;
    }

    public Charge getDefaultProfessorsCharge() {
        return defaultProfessorsCharge;
    }

    public void setDefaultProfessorsCharge(Charge defaultProfessorsCharge) {
        this.defaultProfessorsCharge = defaultProfessorsCharge;
    }

    public CashAccount getForeignCurrencyVATFiscalCreditAccount() {
        return foreignCurrencyVATFiscalCreditAccount;
    }

    public void setForeignCurrencyVATFiscalCreditAccount(CashAccount foreignCurrencyVATFiscalCreditAccount) {
        this.foreignCurrencyVATFiscalCreditAccount = foreignCurrencyVATFiscalCreditAccount;
        setForeignCurrencyVATFiscalCreditAccountCode(this.foreignCurrencyVATFiscalCreditAccount != null ?
                this.foreignCurrencyVATFiscalCreditAccount.getAccountCode() : null);
    }

    public String getForeignCurrencyVATFiscalCreditAccountCode() {
        return foreignCurrencyVATFiscalCreditAccountCode;
    }

    public void setForeignCurrencyVATFiscalCreditAccountCode(String foreignCurrencyVATFiscalCreditAccountCode) {
        this.foreignCurrencyVATFiscalCreditAccountCode = foreignCurrencyVATFiscalCreditAccountCode;
    }

    public CashAccount getNationalCurrencyVATFiscalCreditAccount() {
        return nationalCurrencyVATFiscalCreditAccount;
    }

    public void setNationalCurrencyVATFiscalCreditAccount(CashAccount nationalCurrencyVATFiscalCreditAccount) {
        this.nationalCurrencyVATFiscalCreditAccount = nationalCurrencyVATFiscalCreditAccount;
        setNationalCurrencyVATFiscalCreditAccountCode(this.nationalCurrencyVATFiscalCreditAccount != null ?
                this.nationalCurrencyVATFiscalCreditAccount.getAccountCode() : null);
    }

    public String getNationalCurrencyVATFiscalCreditAccountCode() {
        return nationalCurrencyVATFiscalCreditAccountCode;
    }

    public void setNationalCurrencyVATFiscalCreditAccountCode(String nationalCurrencyVATFiscalCreditAccountCode) {
        this.nationalCurrencyVATFiscalCreditAccountCode = nationalCurrencyVATFiscalCreditAccountCode;
    }

    public CashAccount getNationalCurrencyVATFiscalCreditTransientAccount() {
        return nationalCurrencyVATFiscalCreditTransientAccount;
    }

    public void setNationalCurrencyVATFiscalCreditTransientAccount(CashAccount nationalCurrencyVATFiscalCreditTransientAccount) {
        this.nationalCurrencyVATFiscalCreditTransientAccount = nationalCurrencyVATFiscalCreditTransientAccount;
        setNationalCurrencyVATFiscalCreditTransientAccountCode(this.nationalCurrencyVATFiscalCreditTransientAccount != null ?
                this.nationalCurrencyVATFiscalCreditTransientAccount.getAccountCode() : null);
    }

    public String getNationalCurrencyVATFiscalCreditTransientAccountCode() {
        return nationalCurrencyVATFiscalCreditTransientAccountCode;
    }

    public void setNationalCurrencyVATFiscalCreditTransientAccountCode(String nationalCurrencyVATFiscalCreditTransientAccountCode) {
        this.nationalCurrencyVATFiscalCreditTransientAccountCode = nationalCurrencyVATFiscalCreditTransientAccountCode;
    }

    public CashAccount getFixedAssetInTransitAccount() {
        return fixedAssetInTransitAccount;
    }

    public void setFixedAssetInTransitAccount(CashAccount fixedAssetInTransitAccount) {
        this.fixedAssetInTransitAccount = fixedAssetInTransitAccount;
    }

    public String getFixedAssetInTransitAccountCode() {
        return fixedAssetInTransitAccountCode;
    }

    public void setFixedAssetInTransitAccountCode(String fixedAssetInTransitAccountCode) {
        this.fixedAssetInTransitAccountCode = fixedAssetInTransitAccountCode;
    }

    public DocumentType getDefaultDocumentType() {
        return defaultDocumentType;
    }

    public void setDefaultDocumentType(DocumentType defaultDocumentType) {
        this.defaultDocumentType = defaultDocumentType;
    }

    public Salutation getDefaultSalutationForWoman() {
        return defaultSalutationForWoman;
    }

    public void setDefaultSalutationForWoman(Salutation defaultSalutationForWoman) {
        this.defaultSalutationForWoman = defaultSalutationForWoman;
    }

    public Salutation getDefaultSalutationForMan() {
        return defaultSalutationForMan;
    }

    public void setDefaultSalutationForMan(Salutation defaultSalutationForMan) {
        this.defaultSalutationForMan = defaultSalutationForMan;
    }

    public String getScheduleEvaluationRedirectURL() {
        return scheduleEvaluationRedirectURL;
    }

    public void setScheduleEvaluationRedirectURL(String scheduleEvaluationRedirectURL) {
        this.scheduleEvaluationRedirectURL = scheduleEvaluationRedirectURL;
    }

    public String getStudentScheduleEvaluationRedirectURL() {
        return studentScheduleEvaluationRedirectURL;
    }

    public void setStudentScheduleEvaluationRedirectURL(String studentScheduleEvaluationRedirectURL) {
        this.studentScheduleEvaluationRedirectURL = studentScheduleEvaluationRedirectURL;
    }

    public String getTeacherScheduleEvaluationRedirectURL() {
        return teacherScheduleEvaluationRedirectURL;
    }

    public void setTeacherScheduleEvaluationRedirectURL(String teacherScheduleEvaluationRedirectURL) {
        this.teacherScheduleEvaluationRedirectURL = teacherScheduleEvaluationRedirectURL;
    }

    public String getCareerManagerScheduleEvaluationRedirectURL() {
        return careerManagerScheduleEvaluationRedirectURL;
    }

    public void setCareerManagerScheduleEvaluationRedirectURL(String careerManagerScheduleEvaluationRedirectURL) {
        this.careerManagerScheduleEvaluationRedirectURL = careerManagerScheduleEvaluationRedirectURL;
    }

    public String getAutoEvaluationScheduleEvaluationRedirectURL() {
        return autoEvaluationScheduleEvaluationRedirectURL;
    }

    public void setAutoEvaluationScheduleEvaluationRedirectURL(String autoEvaluationScheduleEvaluationRedirectURL) {
        this.autoEvaluationScheduleEvaluationRedirectURL = autoEvaluationScheduleEvaluationRedirectURL;
    }

    public boolean isPurchaseOrderCodificationEnabled() {
        return purchaseOrderCodificationEnabled;
    }

    public void setPurchaseOrderCodificationEnabled(boolean purchaseOrderCodificationEnabled) {
        this.purchaseOrderCodificationEnabled = purchaseOrderCodificationEnabled;
    }

    public boolean isRetentionForLoanAndAdvance() {
        return retentionForLoanAndAdvance;
    }

    public void setRetentionForLoanAndAdvance(boolean retentionForLoanAndAdvance) {
        this.retentionForLoanAndAdvance = retentionForLoanAndAdvance;
    }

    public boolean getTreasuryDocumentsAuthorizationEnabled() {
        return treasuryDocumentsAuthorizationEnabled;
    }

    public void setTreasuryDocumentsAuthorizationEnabled(boolean treasuryDocumentsAuthorizationEnabled) {
        this.treasuryDocumentsAuthorizationEnabled = treasuryDocumentsAuthorizationEnabled;
    }

    public boolean getPayablesDocumentsAuthorizationEnabled() {
        return payablesDocumentsAuthorizationEnabled;
    }

    public void setPayablesDocumentsAuthorizationEnabled(boolean payablesDocumentsAuthorizationEnabled) {
        this.payablesDocumentsAuthorizationEnabled = payablesDocumentsAuthorizationEnabled;
    }

    public boolean getContractModificationAuthorization() {
        return contractModificationAuthorization;
    }

    public void setContractModificationAuthorization(boolean contractModificationAuthorization) {
        this.contractModificationAuthorization = contractModificationAuthorization;
    }

    public boolean getContractModificationCode() {
        return contractModificationCode;
    }

    public void setContractModificationCode(boolean contractModificationCode) {
        this.contractModificationCode = contractModificationCode;
    }

    public boolean getBasicBasedChristmasPayroll() {
        return basicBasedChristmasPayroll;
    }

    public void setBasicBasedChristmasPayroll(boolean basicBasedChristmasPayroll) {
        this.basicBasedChristmasPayroll = basicBasedChristmasPayroll;
    }

    public BigDecimal getHrsWorkingDay() {
        return hrsWorkingDay;
    }

    public void setHrsWorkingDay(BigDecimal hrsWorkingDay) {
        this.hrsWorkingDay = hrsWorkingDay;
    }

    public String getCashBoxDocumentTypeCode() {
        return cashBoxDocumentTypeCode;
    }

    public void setCashBoxDocumentTypeCode(String cashBoxDocumentTypeCode) {
        this.cashBoxDocumentTypeCode = cashBoxDocumentTypeCode;
    }

    public PayableDocumentType getCashBoxDocumentType() {
        return cashBoxDocumentType;
    }

    public void setCashBoxDocumentType(PayableDocumentType cashBoxDocumentType) {
        this.cashBoxDocumentType = cashBoxDocumentType;
        setCashBoxDocumentTypeCode(cashBoxDocumentType != null ? cashBoxDocumentType.getDocumentType() : null);
    }

    public JobCategory getJobCategoryDLH() {
        return jobCategoryDLH;
    }

    public void setJobCategoryDLH(JobCategory jobCategoryDLH) {
        this.jobCategoryDLH = jobCategoryDLH;
    }

    public JobCategory getJobCategoryDTH() {
        return jobCategoryDTH;
    }

    public void setJobCategoryDTH(JobCategory jobCategoryDTH) {
        this.jobCategoryDTH = jobCategoryDTH;
    }

    public KindOfSalary getKindOfSalaryDLH() {
        return kindOfSalaryDLH;
    }

    public void setKindOfSalaryDLH(KindOfSalary kindOfSalaryDLH) {
        this.kindOfSalaryDLH = kindOfSalaryDLH;
    }

    public KindOfSalary getKindOfSalaryDTH() {
        return kindOfSalaryDTH;
    }

    public void setKindOfSalaryDTH(KindOfSalary kindOfSalaryDTH) {
        this.kindOfSalaryDTH = kindOfSalaryDTH;
    }

    public CostCenter getExchangeRateBalanceCostCenter() {
        return exchangeRateBalanceCostCenter;
    }

    public void setExchangeRateBalanceCostCenter(CostCenter exchangeRateBalanceCostCenter) {
        this.exchangeRateBalanceCostCenter = exchangeRateBalanceCostCenter;
        setCostCenterCode(exchangeRateBalanceCostCenter != null ? exchangeRateBalanceCostCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public CashAccount getDepositInTransitForeignCurrencyAccount() {
        return depositInTransitForeignCurrencyAccount;
    }

    public void setDepositInTransitForeignCurrencyAccount(CashAccount depositInTransitForeignCurrencyAccount) {
        this.depositInTransitForeignCurrencyAccount = depositInTransitForeignCurrencyAccount;
        if (null != depositInTransitForeignCurrencyAccount) {
            depositInTransitForeignCurrencyAccountCode = depositInTransitForeignCurrencyAccount.getAccountCode();
        }
    }

    public CashAccount getDepositInTransitNationalCurrencyAccount() {
        return depositInTransitNationalCurrencyAccount;
    }

    public void setDepositInTransitNationalCurrencyAccount(CashAccount depositInTransitNationalCurrencyAccount) {
        this.depositInTransitNationalCurrencyAccount = depositInTransitNationalCurrencyAccount;
        if (null != depositInTransitNationalCurrencyAccount) {
            depositInTransitNationalCurrencyAccountCode = depositInTransitNationalCurrencyAccount.getAccountCode();
        }
    }

    public String getDepositInTransitForeignCurrencyAccountCode() {
        return depositInTransitForeignCurrencyAccountCode;
    }

    public void setDepositInTransitForeignCurrencyAccountCode(String depositInTransitForeignCurrencyAccountCode) {
        this.depositInTransitForeignCurrencyAccountCode = depositInTransitForeignCurrencyAccountCode;
    }

    public String getDepositInTransitNationalCurrencyAccountCode() {
        return depositInTransitNationalCurrencyAccountCode;
    }

    public void setDepositInTransitNationalCurrencyAccountCode(String depositInTransitNationalCurrencyAccountCode) {
        this.depositInTransitNationalCurrencyAccountCode = depositInTransitNationalCurrencyAccountCode;
    }

    public String getWarehouseNationalCurrencyTransientAccount1Code() {
        return warehouseNationalCurrencyTransientAccount1Code;
    }

    public void setWarehouseNationalCurrencyTransientAccount1Code(String warehouseNationalCurrencyTransientAccount1Code) {
        this.warehouseNationalCurrencyTransientAccount1Code = warehouseNationalCurrencyTransientAccount1Code;
    }

    public CashAccount getWarehouseNationalCurrencyTransientAccount1() {
        return warehouseNationalCurrencyTransientAccount1;
    }

    public void setWarehouseNationalCurrencyTransientAccount1(CashAccount warehouseNationalCurrencyTransientAccount1) {
        this.warehouseNationalCurrencyTransientAccount1 = warehouseNationalCurrencyTransientAccount1;
    }

    @Override
    public String toString() {
        return "CompanyConfiguration{" +
                "companyNumber='" + companyNumber + '\'' +
                ", balanceExchangeRateAccountCode='" + balanceExchangeRateAccountCode + '\'' +
                ", balanceExchangeRateAccount=" + balanceExchangeRateAccount +
                ", version=" + version +
                '}';
    }

    public String getWarehouseNationalCurrencyTransientAccount2Code() {
        return warehouseNationalCurrencyTransientAccount2Code;
    }

    public void setWarehouseNationalCurrencyTransientAccount2Code(String warehouseNationalCurrencyTransientAccount2Code) {
        this.warehouseNationalCurrencyTransientAccount2Code = warehouseNationalCurrencyTransientAccount2Code;
    }

    public CashAccount getWarehouseNationalCurrencyTransientAccount2() {
        return warehouseNationalCurrencyTransientAccount2;
    }

    public void setWarehouseNationalCurrencyTransientAccount2(CashAccount warehouseNationalCurrencyTransientAccount2) {
        this.warehouseNationalCurrencyTransientAccount2 = warehouseNationalCurrencyTransientAccount2;
    }

    public FinanceUser getDefaultAccountancyUserProduction() {
        return defaultAccountancyUserProduction;
    }

    public void setDefaultAccountancyUserProduction(FinanceUser defaultAccountancyUserProduction) {
        this.defaultAccountancyUserProduction = defaultAccountancyUserProduction;
    }

    public double getUnitPriceMilk() {
        return unitPriceMilk;
    }

    public void setUnitPriceMilk(double unitPriceMilk) {
        this.unitPriceMilk = unitPriceMilk;
    }

    public double getIt() {
        return it;
    }

    public void setIt(double it) {
        this.it = it;
    }

    public double getIue() {
        return iue;
    }

    public void setIue(double iue) {
        this.iue = iue;
    }

    public CashAccount getItRetention() {
        return itRetention;
    }

    public void setItRetention(CashAccount itRetention) {
        this.itRetention = itRetention;
    }

    public CashAccount getIueRetention() {
        return iueRetention;
    }

    public void setIueRetention(CashAccount iueRetention) {
        this.iueRetention = iueRetention;
    }

    public CashAccount getCtaCostPT() {
        return ctaCostPT;
    }

    public void setCtaCostPT(CashAccount ctaCostPT) {
        this.ctaCostPT = ctaCostPT;
    }

    public CashAccount getCtaAlmPT() {
        return ctaAlmPT;
    }

    public void setCtaAlmPT(CashAccount ctaAlmPT) {
        this.ctaAlmPT = ctaAlmPT;
    }

    public CashAccount getCtaCostPV() {
        return ctaCostPV;
    }

    public void setCtaCostPV(CashAccount ctaCostPV) {
        this.ctaCostPV = ctaCostPV;
    }

    public CashAccount getCtaAlmPV() {
        return ctaAlmPV;
    }

    public void setCtaAlmPV(CashAccount ctaAlmPV) {
        this.ctaAlmPV = ctaAlmPV;
    }
}