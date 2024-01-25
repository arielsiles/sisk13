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
import java.util.Date;

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
    @Column(name = "no_cia", nullable = false, updatable = false)
    private String companyNumber;

    @Column(name = "titulo")
    private String title;

    @Column(name = "subtitulo")
    private String subTitle;

    @Column(name = "compania")
    private String companyName;

    @Column(name = "sistema")
    private String systemName;

    @Column(name = "lugar")
    private String locationName;

    @Column(name = "leyenda_uno")
    private String oneLegend;

    @Column(name = "leyenda_online")
    private String onlineLegend;

    @Column(name = "leyenda_offline")
    private String offlineLegend;

    @Column(name = "annul_date")
    private Date invoiceAnnulDate;

    @Column(name = "url_measure_units")
    private String measureUnitsURL;

    @Column(name = "url_products_services")
    private String productsAndServicesURL;

    @Column(name = "url_activities")
    private String activitiesURL;

    @Column(name = "url_nit_verification")
    private String nitVerificationURL;

    @Column(name = "url_validate_offline_bill_packages")
    private String validateOfflineBillPackagesURL;

    @Column(name = "url_process_offline_bill_packages")
    private String processOfflineBillPackagesURL;

    @Column(name = "url_prepare_offline_bill_packages")
    private String prepareOfflineBillPackagesURL;

    @Column(name = "url_set_online_mode")
    private String onlineModeURL;

    @Column(name = "url_set_offline_mode")
    private String offlineModeURL;

    @Column(name = "url_significant_event")
    private String significantEventURL;

    @Column(name = "url_online_offline_mode")
    private String checkBillingModeURL;

    @Column(name = "url_createbill")
    private String createbillURL;

    @Column(name = "url_cancelbill")
    private String cancelbillURL;

    @Column(name = "url_qr")
    private String qrcodeURL;

    @Column(name = "url_ping")
    private String connectionTestURL;

    @Column(name = "ctadiftipcam", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String balanceExchangeRateAccountCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctadiftipcam", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount balanceExchangeRateAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "iue_ret", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount iueRetention;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "it_ret", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount itRetention;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctacostpt", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaCostPT;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmpt", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPT;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmptag", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPTAG;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctacostpv", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaCostPV;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmpv", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPV;

    @Column(name = "ctaantprovme", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String advancePaymentForeignCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaantprovme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount advancePaymentForeignCurrencyAccount;

    @Column(name = "ctaantprovmn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String advancePaymentNationalCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaantprovmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount advancePaymentNationalCurrencyAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "ctadeptrame", referencedColumnName = "cuenta", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount depositInTransitForeignCurrencyAccount;

    @Column(name = "ctadeptrame", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String depositInTransitForeignCurrencyAccountCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "ctadeptramn", referencedColumnName = "cuenta", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount depositInTransitNationalCurrencyAccount;

    @Column(name = "ctadeptramn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String depositInTransitNationalCurrencyAccountCode;

    @Column(name = "ctaalmme", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseForeignCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseForeignCurrencyAccount;

    @Column(name = "ctaalmmn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyAccount;

    @Column(name = "ctatransalmme", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseForeignCurrencyTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalmme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseForeignCurrencyTransientAccount;

    @Column(name = "ctatransalmmn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalmmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount;

    @Column(name = "ctatransalm1mn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyTransientAccount1Code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalm1mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount1;

    @Column(name = "ctatransalm2mn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String warehouseNationalCurrencyTransientAccount2Code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalm2mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount2;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaaitb", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount adjustmentForInflationAccount;

    @Column(name = "ctaaitb", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String adjustmentForInflationAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) foreign currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaivacrefime", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyVATFiscalCreditAccount;

    @Column(name = "ctaivacrefime", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String foreignCurrencyVATFiscalCreditAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) national currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaivacrefimn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyVATFiscalCreditAccount;

    @Column(name = "ctaivacrefimn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String nationalCurrencyVATFiscalCreditAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) national currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaivacrefitrmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyVATFiscalCreditTransientAccount;

    @Column(name = "ctaivacrefitrmn", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String nationalCurrencyVATFiscalCreditTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaprovobu", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount provisionByTangibleFixedAssetObsolescenceAccount;

    @Column(name = "ctaprovobu", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String provisionByTangibleFixedAssetObsolescenceAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaafet", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedAssetInTransitAccount;

    @Column(name = "ctaafet", length = 20, nullable = false)
    @Length(max = 20)
    @NotNull
    private String fixedAssetInTransitAccountCode;

    @Column(name = "no_usr_sis", length = 4, nullable = false)
    @Length(max = 4)
    @NotNull
    private String defaultSystemUserNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_usr_teso", referencedColumnName = "NO_USR", nullable = false)
    @NotNull
    private FinanceUser defaultTreasuryUser;

    @Column(name = "no_usr_rpagos", length = 4, nullable = false)
    @NotNull
    private String defaultPurchaseOrderRemakePaymentUserNumber;

    @Column(name = "anio_gen_rpagos", length = 4, nullable = false)
    @NotNull
    private Integer defaultPurchaseOrderRemakeYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_usr_conta", referencedColumnName = "no_usr", nullable = false)
    @NotNull
    private FinanceUser defaultAccountancyUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_usr_produccion", referencedColumnName = "no_usr", nullable = false)
    @NotNull
    private FinanceUser defaultAccountancyUserProduction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_usr_pagar", referencedColumnName = "no_usr", nullable = false)
    @NotNull
    private FinanceUser defaultPayableFinanceUser;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "pagoctabcomn", referencedColumnName = "cta_bco", nullable = false, updatable = false, insertable = false)
    })
    private FinancesBankAccount nationalBankAccountForPayment;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "pagoctabcome", referencedColumnName = "cta_bco", nullable = false, updatable = false, insertable = false)
    })
    private FinancesBankAccount foreignBankAccountForPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCARgODOC", nullable = false)
    private Charge defaultProfessorsCharge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddeftipodoc", nullable = false)
    private DocumentType defaultDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddefsalmuj", nullable = false)
    private Salutation defaultSalutationForWoman;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddefsalhom", nullable = false)
    private Salutation defaultSalutationForMan;

    @Column(name = "urlredirevalprog", nullable = false)
    private String scheduleEvaluationRedirectURL;

    @Column(name = "urlredirevalprogest", nullable = false)
    private String studentScheduleEvaluationRedirectURL;

    @Column(name = "urlredirevalprogdoc", nullable = false)
    private String teacherScheduleEvaluationRedirectURL;

    @Column(name = "urlredirevalprogjc", nullable = false)
    private String careerManagerScheduleEvaluationRedirectURL;

    @Column(name = "urlredirevalprogae", nullable = false)
    private String autoEvaluationScheduleEvaluationRedirectURL;

    @Column(name = "occodifactiva", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean purchaseOrderCodificationEnabled;

    @Column(name = "retencionprestamoanti", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean retentionForLoanAndAdvance;

    @Column(name = "preciounitarioleche",columnDefinition = "DECIMAL(5,2)",nullable = true)
    private Double unitPriceMilk;

    @Column(name = "it",columnDefinition = "DECIMAL(3,2)",nullable = true)
    private Double it;

    @Column(name = "iue",columnDefinition = "DECIMAL(3,2)",nullable = true)
    private Double iue;

    @Column(name = "iva_tax", nullable = true)
    private BigDecimal ivaTaxValue;

    @Column(name = "net_val", nullable = true)
    private BigDecimal netValue;

    @Column(name = "it_tax", nullable = true)
    private BigDecimal itTaxValue;

    @Column(name = "automodifcontrato", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean contractModificationAuthorization;

    @Column(name = "codmodifcontrato", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private boolean contractModificationCode;

    @Column(name = "activoautdoc_teso", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private boolean treasuryDocumentsAuthorizationEnabled;

    @Column(name = "activoautdoc_cxp", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME, parameters = {
            @Parameter(name = TRUE_PARAMETER, value = TRUE_VALUE),
            @Parameter(name = FALSE_PARAMETER, value = FALSE_VALUE)
    })
    private boolean payablesDocumentsAuthorizationEnabled;

    @Column(name = "agui_basico", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private boolean basicBasedChristmasPayroll;

    @Column(name = "hrsdialaboral", precision = 10, scale = 2, nullable = false)
    @NotNull
    private BigDecimal hrsWorkingDay;

    @Column(name = "tipo_doc_caja")
    private String cashBoxDocumentTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "tipo_doc_caja", referencedColumnName = "tipo_doc", nullable = false, insertable = false, updatable = false)
    })
    private PayableDocumentType cashBoxDocumentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriapuestodlh", referencedColumnName = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategoryDLH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriapuestodth", referencedColumnName = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategoryDTH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtiposueldodlh", referencedColumnName = "idtiposueldo", nullable = false)
    private KindOfSalary kindOfSalaryDLH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtiposueldodth", referencedColumnName = "idtiposueldo", nullable = false)
    private KindOfSalary kindOfSalaryDTH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter exchangeRateBalanceCostCenter;

    @Column(name = "cod_cc", length = 8)
    @Length(max = 8)
    private String costCenterCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctamermabaj", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount lowAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctareproc", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount reworkAccount;


    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ct_cajaahorro", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount SavingsBankAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ct_cajaveter", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount VeterinaryCashAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "CAJAgRAL1MN", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount generalCashAccountNational;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "i_pvig_pf_mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedTermInterestNationalCurrency;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaprovaf", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedAssetProvidersAccount;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctag_it", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount transactionTaxExpense;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctap_debfisiva", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fiscalDebitLiability;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctap_itxpagar", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount transactionTaxPayable;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctai_ventapri", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount primarySaleProduct;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctai_ventasec", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount secondarySaleProduct;

    @Column(name = "distparam")
    private BigDecimal dealerParameter;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctacomision", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount commissionSalesCashAccount;

    @Column(name = "doc_oc_pago")
    private String paymentDocumentOC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cxp_provmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountPayableSupplier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", unique = true, nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
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

    public CashAccount getLowAccount() {
        return lowAccount;
    }

    public void setLowAccount(CashAccount lowAccount) {
        this.lowAccount = lowAccount;
    }

    public CashAccount getReworkAccount() {
        return reworkAccount;
    }

    public void setReworkAccount(CashAccount reworkAccount) {
        this.reworkAccount = reworkAccount;
    }

    public CashAccount getSavingsBankAccount() {
        return SavingsBankAccount;
    }

    public void setSavingsBankAccount(CashAccount savingsBankAccount) {
        SavingsBankAccount = savingsBankAccount;
    }

    public CashAccount getVeterinaryCashAccount() {
        return VeterinaryCashAccount;
    }

    public void setVeterinaryCashAccount(CashAccount veterinaryCashAccount) {
        VeterinaryCashAccount = veterinaryCashAccount;
    }

    public CashAccount getGeneralCashAccountNational() {
        return generalCashAccountNational;
    }

    public void setGeneralCashAccountNational(CashAccount generalCashAccountNational) {
        this.generalCashAccountNational = generalCashAccountNational;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public CashAccount getFixedTermInterestNationalCurrency() {
        return fixedTermInterestNationalCurrency;
    }

    public void setFixedTermInterestNationalCurrency(CashAccount fixedTermInterestNationalCurrency) {
        this.fixedTermInterestNationalCurrency = fixedTermInterestNationalCurrency;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public CashAccount getFixedAssetProvidersAccount() {
        return fixedAssetProvidersAccount;
    }

    public void setFixedAssetProvidersAccount(CashAccount fixedAssetProvidersAccount) {
        this.fixedAssetProvidersAccount = fixedAssetProvidersAccount;
    }

    public CashAccount getFiscalDebitLiability() {
        return fiscalDebitLiability;
    }

    public void setFiscalDebitLiability(CashAccount fiscalDebitLiability) {
        this.fiscalDebitLiability = fiscalDebitLiability;
    }

    public CashAccount getTransactionTaxPayable() {
        return transactionTaxPayable;
    }

    public void setTransactionTaxPayable(CashAccount transactionTaxPayable) {
        this.transactionTaxPayable = transactionTaxPayable;
    }

    public CashAccount getPrimarySaleProduct() {
        return primarySaleProduct;
    }

    public void setPrimarySaleProduct(CashAccount primarySaleProduct) {
        this.primarySaleProduct = primarySaleProduct;
    }

    public CashAccount getSecondarySaleProduct() {
        return secondarySaleProduct;
    }

    public void setSecondarySaleProduct(CashAccount secondarySaleProduct) {
        this.secondarySaleProduct = secondarySaleProduct;
    }

    public CashAccount getTransactionTaxExpense() {
        return transactionTaxExpense;
    }

    public void setTransactionTaxExpense(CashAccount transactionTaxExpense) {
        this.transactionTaxExpense = transactionTaxExpense;
    }

    public BigDecimal getDealerParameter() {
        return dealerParameter;
    }

    public void setDealerParameter(BigDecimal dealerParameter) {
        this.dealerParameter = dealerParameter;
    }

    public CashAccount getCommissionSalesCashAccount() {
        return commissionSalesCashAccount;
    }

    public void setCommissionSalesCashAccount(CashAccount commissionSalesCashAccount) {
        this.commissionSalesCashAccount = commissionSalesCashAccount;
    }

    public BigDecimal getIvaTaxValue() {
        return ivaTaxValue;
    }

    public void setIvaTaxValue(BigDecimal ivaTaxValue) {
        this.ivaTaxValue = ivaTaxValue;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
    }

    public BigDecimal getItTaxValue() {
        return itTaxValue;
    }

    public void setItTaxValue(BigDecimal itTaxValue) {
        this.itTaxValue = itTaxValue;
    }

    public CashAccount getCtaAlmPTAG() {
        return ctaAlmPTAG;
    }

    public void setCtaAlmPTAG(CashAccount ctaAlmPTAG) {
        this.ctaAlmPTAG = ctaAlmPTAG;
    }

    public String getCreatebillURL() {
        return createbillURL;
    }

    public void setCreatebillURL(String createbillURL) {
        this.createbillURL = createbillURL;
    }

    public String getCancelbillURL() {
        return cancelbillURL;
    }

    public void setCancelbillURL(String cancelbillURL) {
        this.cancelbillURL = cancelbillURL;
    }

    public String getQrcodeURL() {
        return qrcodeURL;
    }

    public void setQrcodeURL(String qrcodeURL) {
        this.qrcodeURL = qrcodeURL;
    }

    public String getConnectionTestURL() {
        return connectionTestURL;
    }

    public void setConnectionTestURL(String connectionTestURL) {
        this.connectionTestURL = connectionTestURL;
    }

    public String getCheckBillingModeURL() {
        return checkBillingModeURL;
    }

    public void setCheckBillingModeURL(String checkBillingModeURL) {
        this.checkBillingModeURL = checkBillingModeURL;
    }

    public String getSignificantEventURL() {
        return significantEventURL;
    }

    public void setSignificantEventURL(String significantEventURL) {
        this.significantEventURL = significantEventURL;
    }

    public String getOnlineModeURL() {
        return onlineModeURL;
    }

    public void setOnlineModeURL(String onlineModeURL) {
        this.onlineModeURL = onlineModeURL;
    }

    public String getOfflineModeURL() {
        return offlineModeURL;
    }

    public void setOfflineModeURL(String offlineModeURL) {
        this.offlineModeURL = offlineModeURL;
    }

    public String getValidateOfflineBillPackagesURL() {
        return validateOfflineBillPackagesURL;
    }

    public void setValidateOfflineBillPackagesURL(String validateOfflineBillPackagesURL) {
        this.validateOfflineBillPackagesURL = validateOfflineBillPackagesURL;
    }

    public String getProcessOfflineBillPackagesURL() {
        return processOfflineBillPackagesURL;
    }

    public void setProcessOfflineBillPackagesURL(String processOfflineBillPackagesURL) {
        this.processOfflineBillPackagesURL = processOfflineBillPackagesURL;
    }

    public String getPrepareOfflineBillPackagesURL() {
        return prepareOfflineBillPackagesURL;
    }

    public void setPrepareOfflineBillPackagesURL(String prepareOfflineBillPackagesURL) {
        this.prepareOfflineBillPackagesURL = prepareOfflineBillPackagesURL;
    }

    public String getNitVerificationURL() {
        return nitVerificationURL;
    }

    public void setNitVerificationURL(String nitVerificationURL) {
        this.nitVerificationURL = nitVerificationURL;
    }

    public String getActivitiesURL() {
        return activitiesURL;
    }

    public void setActivitiesURL(String activitiesURL) {
        this.activitiesURL = activitiesURL;
    }

    public String getProductsAndServicesURL() {
        return productsAndServicesURL;
    }

    public void setProductsAndServicesURL(String productsAndServicesURL) {
        this.productsAndServicesURL = productsAndServicesURL;
    }

    public String getMeasureUnitsURL() {
        return measureUnitsURL;
    }

    public void setMeasureUnitsURL(String measureUnitsURL) {
        this.measureUnitsURL = measureUnitsURL;
    }

    public Date getInvoiceAnnulDate() {
        return invoiceAnnulDate;
    }

    public void setInvoiceAnnulDate(Date invoiceAnnulDate) {
        this.invoiceAnnulDate = invoiceAnnulDate;
    }

    public String getOnlineLegend() {
        return onlineLegend;
    }

    public void setOnlineLegend(String onlineLegend) {
        this.onlineLegend = onlineLegend;
    }

    public String getOfflineLegend() {
        return offlineLegend;
    }

    public void setOfflineLegend(String offlineLegend) {
        this.offlineLegend = offlineLegend;
    }

    public String getOneLegend() {
        return oneLegend;
    }

    public void setOneLegend(String oneLegend) {
        this.oneLegend = oneLegend;
    }

    public String getPaymentDocumentOC() {
        return paymentDocumentOC;
    }

    public void setPaymentDocumentOC(String paymentDocumentOC) {
        this.paymentDocumentOC = paymentDocumentOC;
    }

    public CashAccount getAccountPayableSupplier() {
        return accountPayableSupplier;
    }

    public void setAccountPayableSupplier(CashAccount accountPayableSupplier) {
        this.accountPayableSupplier = accountPayableSupplier;
    }
}