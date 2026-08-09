package com.encens.khipus.model.finances;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.exception.finances.CompanyAccountNotConfiguredException;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.contacts.Salutation;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import javax.persistence.EntityNotFoundException;
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
@EntityListeners({CompanyListener.class, CompanyNumberListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(name = "configuracion", schema = Constants.FINANCES_SCHEMA)
public class CompanyConfiguration {
    /* Cotas de reescalado de los logos. El alto es el limite real de renderizado;
       el ancho es holgado para no deformar logos apaisados. */
    public static final int HEADER_LOGO_MAX_WIDTH = 200;
    public static final int HEADER_LOGO_MAX_HEIGHT = 50;
    public static final int LOGIN_LOGO_MAX_WIDTH = 300;
    public static final int LOGIN_LOGO_MAX_HEIGHT = 300;

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

    @Column(name = "ctadiftipcam", length = 20)
    @Length(max = 20)
    private String balanceExchangeRateAccountCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctadiftipcam", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount balanceExchangeRateAccount;

    @Column(name = "iue_ret", length = 20)
    @Length(max = 20)
    private String iueRetentionCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "iue_ret", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount iueRetention;

    @Column(name = "it_ret", length = 20)
    @Length(max = 20)
    private String itRetentionCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "it_ret", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount itRetention;

    @Column(name = "ctacostpt", length = 20)
    @Length(max = 20)
    private String ctaCostPTCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctacostpt", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaCostPT;

    @Column(name = "ctaalmpt", length = 20)
    @Length(max = 20)
    private String ctaAlmPTCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmpt", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPT;

    @Column(name = "ctaalmptag", length = 20)
    @Length(max = 20)
    private String ctaAlmPTAGCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmptag", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPTAG;

    @Column(name = "ctacostpv", length = 20)
    @Length(max = 20)
    private String ctaCostPVCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctacostpv", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaCostPV;

    @Column(name = "ctaalmpv", length = 20)
    @Length(max = 20)
    private String ctaAlmPVCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmpv", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount ctaAlmPV;

    @Column(name = "ctaMerma", length = 20)
    @Length(max = 20)
    private String wasteAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaMerma", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount wasteAccount;

    @Column(name = "ctaProm", length = 20)
    @Length(max = 20)
    private String averageAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaProm", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount averageAccount;

    @Column(name = "ctaantprovme", length = 20)
    @Length(max = 20)
    private String advancePaymentForeignCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaantprovme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount advancePaymentForeignCurrencyAccount;

    @Column(name = "ctaantprovmn", length = 20)
    @Length(max = 20)
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

    @Column(name = "ctadeptrame", length = 20)
    @Length(max = 20)
    private String depositInTransitForeignCurrencyAccountCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "ctadeptramn", referencedColumnName = "cuenta", nullable = false, insertable = false, updatable = false)
    })
    private CashAccount depositInTransitNationalCurrencyAccount;

    @Column(name = "ctadeptramn", length = 20)
    @Length(max = 20)
    private String depositInTransitNationalCurrencyAccountCode;

    @Column(name = "ctaalmme", length = 20)
    @Length(max = 20)
    private String warehouseForeignCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseForeignCurrencyAccount;

    @Column(name = "ctaalmmn", length = 20)
    @Length(max = 20)
    private String warehouseNationalCurrencyAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaalmmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyAccount;

    @Column(name = "ctatransalmme", length = 20)
    @Length(max = 20)
    private String warehouseForeignCurrencyTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalmme", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseForeignCurrencyTransientAccount;

    @Column(name = "ctatransalmmn", length = 20)
    @Length(max = 20)
    private String warehouseNationalCurrencyTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalmmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount;

    @Column(name = "ctatransalm1mn", length = 20)
    @Length(max = 20)
    private String warehouseNationalCurrencyTransientAccount1Code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctatransalm1mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount warehouseNationalCurrencyTransientAccount1;

    @Column(name = "ctatransalm2mn", length = 20)
    @Length(max = 20)
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

    @Column(name = "ctaaitb", length = 20)
    @Length(max = 20)
    private String adjustmentForInflationAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) foreign currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaivacrefime", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount foreignCurrencyVATFiscalCreditAccount;

    @Column(name = "ctaivacrefime", length = 20)
    @Length(max = 20)
    private String foreignCurrencyVATFiscalCreditAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) national currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaivacrefimn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyVATFiscalCreditAccount;

    @Column(name = "ctaivacrefimn", length = 20)
    @Length(max = 20)
    private String nationalCurrencyVATFiscalCreditAccountCode;

    /* account for iva fiscal credit (VAT=value-added tax) national currency*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaivacrefitrmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount nationalCurrencyVATFiscalCreditTransientAccount;

    @Column(name = "ctaivacrefitrmn", length = 20)
    @Length(max = 20)
    private String nationalCurrencyVATFiscalCreditTransientAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaprovobu", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount provisionByTangibleFixedAssetObsolescenceAccount;

    @Column(name = "ctaprovobu", length = 20)
    @Length(max = 20)
    private String provisionByTangibleFixedAssetObsolescenceAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaafet", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedAssetInTransitAccount;

    @Column(name = "ctaafet", length = 20)
    @Length(max = 20)
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

    @Column(name = "pagoctabcomn", length = 20)
    @Length(max = 20)
    private String nationalBankAccountForPaymentCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "pagoctabcomn", referencedColumnName = "cta_bco", nullable = false, updatable = false, insertable = false)
    })
    private FinancesBankAccount nationalBankAccountForPayment;

    @Column(name = "pagoctabcome", length = 20)
    @Length(max = 20)
    private String foreignBankAccountForPaymentCode;

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

    /**
     * Interruptor (por empresa) del control de stock en la APROBACION del
     * despacho. true = control activo (comportamiento normal). false = permite
     * registrar despachos de meses atras sin validar stock suficiente. Default
     * 1 en BD. Lo consume el flujo de despacho, NO el servicio de vales.
     */
    @Column(name = "desp_controla_inventario", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean dispatchInventoryControl = true;

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

    @Column(name = "email_unisueldo")
    @Email
    @Length(max = 100)
    private String unisueldoEmail;

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

    @Column(name = "ctamermabaj", length = 20)
    @Length(max = 20)
    private String lowAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctamermabaj", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount lowAccount;

    @Column(name = "ctareproc", length = 20)
    @Length(max = 20)
    private String reworkAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctareproc", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount reworkAccount;

    @Column(name = "ct_cajaahorro", length = 20)
    @Length(max = 20)
    private String savingsBankAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ct_cajaahorro", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount SavingsBankAccount;

    @Column(name = "ct_cajaveter", length = 20)
    @Length(max = 20)
    private String veterinaryCashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ct_cajaveter", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount VeterinaryCashAccount;

    /**
     * Caja general. De aca sale el efectivo que se entrega al socio al cerrar un DPF, y
     * tambien el del retiro parcial de una renovacion; antes esas dos cuentas estaban
     * hardcodeadas en AccountAction.
     * <p/>
     * El nombre de columna va en minusculas: en MySQL los nombres de columna no distinguen
     * mayusculas, asi que el "CAJAgRAL1MN" original apuntaba a la misma columna y solo
     * desentonaba con el resto de la tabla.
     */
    @Column(name = "cajagral1mn", length = 20)
    @Length(max = 20)
    private String generalCashAccountNationalCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cajagral1mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount generalCashAccountNational;

    @Column(name = "cajagral1me", length = 20)
    @Length(max = 20)
    private String generalCashAccountForeignCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cajagral1me", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount generalCashAccountForeign;

    @Column(name = "i_pvig_pf_mn", length = 20)
    @Length(max = 20)
    private String fixedTermInterestNationalCurrencyCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "i_pvig_pf_mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedTermInterestNationalCurrency;

    /**
     * Cuentas de gasto de la provision mensual de intereses por pagar sobre DPF.
     * El contrapartida (pasivo "cargos financieros por pagar") NO va aca: sale de
     * CTACF_MN / CTACF_ME del tipo de cuenta, que es lo que ya usa la renovacion de DPF.
     */
    @Column(name = "i_ppag_dpf_mn", length = 20)
    @Length(max = 20)
    private String fixedTermPayableInterestNationalCurrencyCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "i_ppag_dpf_mn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedTermPayableInterestNationalCurrency;

    @Column(name = "i_ppag_dpf_me", length = 20)
    @Length(max = 20)
    private String fixedTermPayableInterestForeignCurrencyCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "i_ppag_dpf_me", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedTermPayableInterestForeignCurrency;

    @Column(name = "ctaprovaf", length = 20)
    @Length(max = 20)
    private String fixedAssetProvidersAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctaprovaf", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fixedAssetProvidersAccount;

    @Column(name = "ctag_it", length = 20)
    @Length(max = 20)
    private String transactionTaxExpenseCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctag_it", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount transactionTaxExpense;

    @Column(name = "ctap_debfisiva", length = 20)
    @Length(max = 20)
    private String fiscalDebitLiabilityCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctap_debfisiva", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount fiscalDebitLiability;

    @Column(name = "ctap_itxpagar", length = 20)
    @Length(max = 20)
    private String transactionTaxPayableCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctap_itxpagar", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount transactionTaxPayable;

    @Column(name = "ctai_ventapri", length = 20)
    @Length(max = 20)
    private String primarySaleProductCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctai_ventapri", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount primarySaleProduct;

    @Column(name = "ctai_ventasec", length = 20)
    @Length(max = 20)
    private String secondarySaleProductCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctai_ventasec", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount secondarySaleProduct;

    @Column(name = "distparam")
    private BigDecimal dealerParameter;

    @Column(name = "ctacomision", length = 20)
    @Length(max = 20)
    private String commissionSalesCashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "ctacomision", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount commissionSalesCashAccount;

    @Column(name = "doc_oc_pago")
    private String paymentDocumentOC;

    @Column(name = "af_fin_oc")
    private String documentFixedAssetOC;

    @Column(name = "cxp_provmn", length = 20)
    @Length(max = 20)
    private String accountPayableSupplierCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cxp_provmn", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountPayableSupplier;

    @Column(name = "cta_pat01", length = 20)
    @Length(max = 20)
    private String accountBalanceSheet1Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cta_pat01", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountBalanceSheet1;

    @Column(name = "cta_pat02", length = 20)
    @Length(max = 20)
    private String accountBalanceSheet2Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cta_pat02", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountBalanceSheet2;

    @Column(name = "cta_pat03", length = 20)
    @Length(max = 20)
    private String accountBalanceSheet3Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cta_pat03", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountBalanceSheet3;

    @Column(name = "cta_pat04", length = 20)
    @Length(max = 20)
    private String accountBalanceSheet4Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cta_pat04", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountBalanceSheet4;

    @Column(name = "cta_pat05", length = 20)
    @Length(max = 20)
    private String accountBalanceSheet5Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cta_pat05", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountBalanceSheet5;

    @Column(name = "cxp_iva", length = 20)
    @Length(max = 20)
    private String accountPayableIVACode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cxp_iva", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountPayableIVA;

    @Column(name = "cxp_regalia", length = 20)
    @Length(max = 20)
    private String accountRegaliaCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cxp_regalia", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountRegalia;

    @Column(name = "cxp_cns", length = 20)
    @Length(max = 20)
    private String accountRetentionCNSCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cxp_cns", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount accountRetentionCNS;

    @Column(name = "ret_cns", nullable = true)
    private BigDecimal retentionCNSValue;

    @Column(name = "res_perdida", length = 20)
    @Length(max = 20)
    private String lossCashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "res_perdida", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount lossCashAccount;

    @Column(name = "res_utilidad", length = 20)
    @Length(max = 20)
    private String profitCashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "res_utilidad", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount profitCashAccount;

    @Column(name = "oc_pagodefault", length = 20)
    @Length(max = 20)
    private String defaultAccountPurchaseOrderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "oc_pagodefault", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount defaultAccountPurchaseOrder;

    /**
     * Logo mostrado en la cabecera, encima del menu principal. Se reescala a
     * {@link #HEADER_LOGO_MAX_WIDTH}x{@link #HEADER_LOGO_MAX_HEIGHT} al guardar.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idlogocabecera", referencedColumnName = "idarchivo")
    private File headerLogo;

    /**
     * Logo mostrado en la pagina de login. Se sirve de forma anonima a traves de
     * companyLogoHolder, por lo que no depende de currentCompany.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idlogologin", referencedColumnName = "idarchivo")
    private File loginLogo;

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
        setNationalBankAccountForPaymentCode(this.nationalBankAccountForPayment != null ? this.nationalBankAccountForPayment.getAccountNumber() : null);
    }

    public String getNationalBankAccountForPaymentCode() {
        return nationalBankAccountForPaymentCode;
    }

    public void setNationalBankAccountForPaymentCode(String nationalBankAccountForPaymentCode) {
        this.nationalBankAccountForPaymentCode = nationalBankAccountForPaymentCode;
    }

    public FinancesBankAccount getForeignBankAccountForPayment() {
        return foreignBankAccountForPayment;
    }

    public void setForeignBankAccountForPayment(FinancesBankAccount foreignBankAccountForPayment) {
        this.foreignBankAccountForPayment = foreignBankAccountForPayment;
        setForeignBankAccountForPaymentCode(this.foreignBankAccountForPayment != null ? this.foreignBankAccountForPayment.getAccountNumber() : null);
    }

    public String getForeignBankAccountForPaymentCode() {
        return foreignBankAccountForPaymentCode;
    }

    public void setForeignBankAccountForPaymentCode(String foreignBankAccountForPaymentCode) {
        this.foreignBankAccountForPaymentCode = foreignBankAccountForPaymentCode;
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
        setFixedAssetInTransitAccountCode(this.fixedAssetInTransitAccount != null ? this.fixedAssetInTransitAccount.getAccountCode() : null);
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

    public boolean isDispatchInventoryControl() {
        return dispatchInventoryControl;
    }

    public void setDispatchInventoryControl(boolean dispatchInventoryControl) {
        this.dispatchInventoryControl = dispatchInventoryControl;
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

    public String getUnisueldoEmail() {
        return unisueldoEmail;
    }

    public void setUnisueldoEmail(String unisueldoEmail) {
        this.unisueldoEmail = unisueldoEmail;
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
        setDepositInTransitForeignCurrencyAccountCode(this.depositInTransitForeignCurrencyAccount != null ? this.depositInTransitForeignCurrencyAccount.getAccountCode() : null);
    }

    public CashAccount getDepositInTransitNationalCurrencyAccount() {
        return depositInTransitNationalCurrencyAccount;
    }

    public void setDepositInTransitNationalCurrencyAccount(CashAccount depositInTransitNationalCurrencyAccount) {
        this.depositInTransitNationalCurrencyAccount = depositInTransitNationalCurrencyAccount;
        setDepositInTransitNationalCurrencyAccountCode(this.depositInTransitNationalCurrencyAccount != null ? this.depositInTransitNationalCurrencyAccount.getAccountCode() : null);
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
        setWarehouseNationalCurrencyTransientAccount1Code(this.warehouseNationalCurrencyTransientAccount1 != null ? this.warehouseNationalCurrencyTransientAccount1.getAccountCode() : null);
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
        setWarehouseNationalCurrencyTransientAccount2Code(this.warehouseNationalCurrencyTransientAccount2 != null ? this.warehouseNationalCurrencyTransientAccount2.getAccountCode() : null);
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
        setItRetentionCode(this.itRetention != null ? this.itRetention.getAccountCode() : null);
    }

    public String getItRetentionCode() {
        return itRetentionCode;
    }

    public void setItRetentionCode(String itRetentionCode) {
        this.itRetentionCode = itRetentionCode;
    }

    public CashAccount getIueRetention() {
        return iueRetention;
    }

    public void setIueRetention(CashAccount iueRetention) {
        this.iueRetention = iueRetention;
        setIueRetentionCode(this.iueRetention != null ? this.iueRetention.getAccountCode() : null);
    }

    public String getIueRetentionCode() {
        return iueRetentionCode;
    }

    public void setIueRetentionCode(String iueRetentionCode) {
        this.iueRetentionCode = iueRetentionCode;
    }

    public CashAccount getCtaCostPT() {
        return ctaCostPT;
    }

    public void setCtaCostPT(CashAccount ctaCostPT) {
        this.ctaCostPT = ctaCostPT;
        setCtaCostPTCode(this.ctaCostPT != null ? this.ctaCostPT.getAccountCode() : null);
    }

    public String getCtaCostPTCode() {
        return ctaCostPTCode;
    }

    public void setCtaCostPTCode(String ctaCostPTCode) {
        this.ctaCostPTCode = ctaCostPTCode;
    }

    public CashAccount getCtaAlmPT() {
        return ctaAlmPT;
    }

    public void setCtaAlmPT(CashAccount ctaAlmPT) {
        this.ctaAlmPT = ctaAlmPT;
        setCtaAlmPTCode(this.ctaAlmPT != null ? this.ctaAlmPT.getAccountCode() : null);
    }

    public String getCtaAlmPTCode() {
        return ctaAlmPTCode;
    }

    public void setCtaAlmPTCode(String ctaAlmPTCode) {
        this.ctaAlmPTCode = ctaAlmPTCode;
    }

    public CashAccount getCtaCostPV() {
        return ctaCostPV;
    }

    public void setCtaCostPV(CashAccount ctaCostPV) {
        this.ctaCostPV = ctaCostPV;
        setCtaCostPVCode(this.ctaCostPV != null ? this.ctaCostPV.getAccountCode() : null);
    }

    public String getCtaCostPVCode() {
        return ctaCostPVCode;
    }

    public void setCtaCostPVCode(String ctaCostPVCode) {
        this.ctaCostPVCode = ctaCostPVCode;
    }

    public CashAccount getCtaAlmPV() {
        return ctaAlmPV;
    }

    public void setCtaAlmPV(CashAccount ctaAlmPV) {
        this.ctaAlmPV = ctaAlmPV;
        setCtaAlmPVCode(this.ctaAlmPV != null ? this.ctaAlmPV.getAccountCode() : null);
    }

    public String getCtaAlmPVCode() {
        return ctaAlmPVCode;
    }

    public void setCtaAlmPVCode(String ctaAlmPVCode) {
        this.ctaAlmPVCode = ctaAlmPVCode;
    }

    public CashAccount getLowAccount() {
        return lowAccount;
    }

    public void setLowAccount(CashAccount lowAccount) {
        this.lowAccount = lowAccount;
        setLowAccountCode(this.lowAccount != null ? this.lowAccount.getAccountCode() : null);
    }

    public String getLowAccountCode() {
        return lowAccountCode;
    }

    public void setLowAccountCode(String lowAccountCode) {
        this.lowAccountCode = lowAccountCode;
    }

    public CashAccount getReworkAccount() {
        return reworkAccount;
    }

    public void setReworkAccount(CashAccount reworkAccount) {
        this.reworkAccount = reworkAccount;
        setReworkAccountCode(this.reworkAccount != null ? this.reworkAccount.getAccountCode() : null);
    }

    public String getReworkAccountCode() {
        return reworkAccountCode;
    }

    public void setReworkAccountCode(String reworkAccountCode) {
        this.reworkAccountCode = reworkAccountCode;
    }

    public CashAccount getSavingsBankAccount() {
        return SavingsBankAccount;
    }

    public void setSavingsBankAccount(CashAccount savingsBankAccount) {
        SavingsBankAccount = savingsBankAccount;
        setSavingsBankAccountCode(SavingsBankAccount != null ? SavingsBankAccount.getAccountCode() : null);
    }

    public String getSavingsBankAccountCode() {
        return savingsBankAccountCode;
    }

    public void setSavingsBankAccountCode(String savingsBankAccountCode) {
        this.savingsBankAccountCode = savingsBankAccountCode;
    }

    public CashAccount getVeterinaryCashAccount() {
        return VeterinaryCashAccount;
    }

    public void setVeterinaryCashAccount(CashAccount veterinaryCashAccount) {
        VeterinaryCashAccount = veterinaryCashAccount;
        setVeterinaryCashAccountCode(VeterinaryCashAccount != null ? VeterinaryCashAccount.getAccountCode() : null);
    }

    public String getVeterinaryCashAccountCode() {
        return veterinaryCashAccountCode;
    }

    public void setVeterinaryCashAccountCode(String veterinaryCashAccountCode) {
        this.veterinaryCashAccountCode = veterinaryCashAccountCode;
    }

    public CashAccount getGeneralCashAccountNational() {
        return generalCashAccountNational;
    }

    public void setGeneralCashAccountNational(CashAccount generalCashAccountNational) {
        this.generalCashAccountNational = generalCashAccountNational;
        setGeneralCashAccountNationalCode(this.generalCashAccountNational != null ? this.generalCashAccountNational.getAccountCode() : null);
    }

    public String getGeneralCashAccountNationalCode() {
        return generalCashAccountNationalCode;
    }

    public void setGeneralCashAccountNationalCode(String generalCashAccountNationalCode) {
        this.generalCashAccountNationalCode = generalCashAccountNationalCode;
    }

    public CashAccount getGeneralCashAccountForeign() {
        return generalCashAccountForeign;
    }

    public void setGeneralCashAccountForeign(CashAccount generalCashAccountForeign) {
        this.generalCashAccountForeign = generalCashAccountForeign;
        setGeneralCashAccountForeignCode(this.generalCashAccountForeign != null ? this.generalCashAccountForeign.getAccountCode() : null);
    }

    public String getGeneralCashAccountForeignCode() {
        return generalCashAccountForeignCode;
    }

    public void setGeneralCashAccountForeignCode(String generalCashAccountForeignCode) {
        this.generalCashAccountForeignCode = generalCashAccountForeignCode;
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
        setFixedTermInterestNationalCurrencyCode(this.fixedTermInterestNationalCurrency != null ? this.fixedTermInterestNationalCurrency.getAccountCode() : null);
    }

    public CashAccount getFixedTermPayableInterestNationalCurrency() {
        return fixedTermPayableInterestNationalCurrency;
    }

    public void setFixedTermPayableInterestNationalCurrency(CashAccount fixedTermPayableInterestNationalCurrency) {
        this.fixedTermPayableInterestNationalCurrency = fixedTermPayableInterestNationalCurrency;
        setFixedTermPayableInterestNationalCurrencyCode(this.fixedTermPayableInterestNationalCurrency != null ? this.fixedTermPayableInterestNationalCurrency.getAccountCode() : null);
    }

    public String getFixedTermPayableInterestNationalCurrencyCode() {
        return fixedTermPayableInterestNationalCurrencyCode;
    }

    public void setFixedTermPayableInterestNationalCurrencyCode(String fixedTermPayableInterestNationalCurrencyCode) {
        this.fixedTermPayableInterestNationalCurrencyCode = fixedTermPayableInterestNationalCurrencyCode;
    }

    public CashAccount getFixedTermPayableInterestForeignCurrency() {
        return fixedTermPayableInterestForeignCurrency;
    }

    public void setFixedTermPayableInterestForeignCurrency(CashAccount fixedTermPayableInterestForeignCurrency) {
        this.fixedTermPayableInterestForeignCurrency = fixedTermPayableInterestForeignCurrency;
        setFixedTermPayableInterestForeignCurrencyCode(this.fixedTermPayableInterestForeignCurrency != null ? this.fixedTermPayableInterestForeignCurrency.getAccountCode() : null);
    }

    public String getFixedTermPayableInterestForeignCurrencyCode() {
        return fixedTermPayableInterestForeignCurrencyCode;
    }

    public void setFixedTermPayableInterestForeignCurrencyCode(String fixedTermPayableInterestForeignCurrencyCode) {
        this.fixedTermPayableInterestForeignCurrencyCode = fixedTermPayableInterestForeignCurrencyCode;
    }

    public String getFixedTermInterestNationalCurrencyCode() {
        return fixedTermInterestNationalCurrencyCode;
    }

    public void setFixedTermInterestNationalCurrencyCode(String fixedTermInterestNationalCurrencyCode) {
        this.fixedTermInterestNationalCurrencyCode = fixedTermInterestNationalCurrencyCode;
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
        setFixedAssetProvidersAccountCode(this.fixedAssetProvidersAccount != null ? this.fixedAssetProvidersAccount.getAccountCode() : null);
    }

    public String getFixedAssetProvidersAccountCode() {
        return fixedAssetProvidersAccountCode;
    }

    public void setFixedAssetProvidersAccountCode(String fixedAssetProvidersAccountCode) {
        this.fixedAssetProvidersAccountCode = fixedAssetProvidersAccountCode;
    }

    public CashAccount getFiscalDebitLiability() {
        return fiscalDebitLiability;
    }

    public void setFiscalDebitLiability(CashAccount fiscalDebitLiability) {
        this.fiscalDebitLiability = fiscalDebitLiability;
        setFiscalDebitLiabilityCode(this.fiscalDebitLiability != null ? this.fiscalDebitLiability.getAccountCode() : null);
    }

    public String getFiscalDebitLiabilityCode() {
        return fiscalDebitLiabilityCode;
    }

    public void setFiscalDebitLiabilityCode(String fiscalDebitLiabilityCode) {
        this.fiscalDebitLiabilityCode = fiscalDebitLiabilityCode;
    }

    public CashAccount getTransactionTaxPayable() {
        return transactionTaxPayable;
    }

    public void setTransactionTaxPayable(CashAccount transactionTaxPayable) {
        this.transactionTaxPayable = transactionTaxPayable;
        setTransactionTaxPayableCode(this.transactionTaxPayable != null ? this.transactionTaxPayable.getAccountCode() : null);
    }

    public String getTransactionTaxPayableCode() {
        return transactionTaxPayableCode;
    }

    public void setTransactionTaxPayableCode(String transactionTaxPayableCode) {
        this.transactionTaxPayableCode = transactionTaxPayableCode;
    }

    public CashAccount getPrimarySaleProduct() {
        return primarySaleProduct;
    }

    public void setPrimarySaleProduct(CashAccount primarySaleProduct) {
        this.primarySaleProduct = primarySaleProduct;
        setPrimarySaleProductCode(this.primarySaleProduct != null ? this.primarySaleProduct.getAccountCode() : null);
    }

    public String getPrimarySaleProductCode() {
        return primarySaleProductCode;
    }

    public void setPrimarySaleProductCode(String primarySaleProductCode) {
        this.primarySaleProductCode = primarySaleProductCode;
    }

    public CashAccount getSecondarySaleProduct() {
        return secondarySaleProduct;
    }

    public void setSecondarySaleProduct(CashAccount secondarySaleProduct) {
        this.secondarySaleProduct = secondarySaleProduct;
        setSecondarySaleProductCode(this.secondarySaleProduct != null ? this.secondarySaleProduct.getAccountCode() : null);
    }

    public String getSecondarySaleProductCode() {
        return secondarySaleProductCode;
    }

    public void setSecondarySaleProductCode(String secondarySaleProductCode) {
        this.secondarySaleProductCode = secondarySaleProductCode;
    }

    public CashAccount getTransactionTaxExpense() {
        return transactionTaxExpense;
    }

    public void setTransactionTaxExpense(CashAccount transactionTaxExpense) {
        this.transactionTaxExpense = transactionTaxExpense;
        setTransactionTaxExpenseCode(this.transactionTaxExpense != null ? this.transactionTaxExpense.getAccountCode() : null);
    }

    public String getTransactionTaxExpenseCode() {
        return transactionTaxExpenseCode;
    }

    public void setTransactionTaxExpenseCode(String transactionTaxExpenseCode) {
        this.transactionTaxExpenseCode = transactionTaxExpenseCode;
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
        setCommissionSalesCashAccountCode(this.commissionSalesCashAccount != null ? this.commissionSalesCashAccount.getAccountCode() : null);
    }

    public String getCommissionSalesCashAccountCode() {
        return commissionSalesCashAccountCode;
    }

    public void setCommissionSalesCashAccountCode(String commissionSalesCashAccountCode) {
        this.commissionSalesCashAccountCode = commissionSalesCashAccountCode;
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
        setCtaAlmPTAGCode(this.ctaAlmPTAG != null ? this.ctaAlmPTAG.getAccountCode() : null);
    }

    public String getCtaAlmPTAGCode() {
        return ctaAlmPTAGCode;
    }

    public void setCtaAlmPTAGCode(String ctaAlmPTAGCode) {
        this.ctaAlmPTAGCode = ctaAlmPTAGCode;
    }

    public CashAccount getWasteAccount() {
        return wasteAccount;
    }

    public void setWasteAccount(CashAccount wasteAccount) {
        this.wasteAccount = wasteAccount;
        setWasteAccountCode(this.wasteAccount != null ? this.wasteAccount.getAccountCode() : null);
    }

    public String getWasteAccountCode() {
        return wasteAccountCode;
    }

    public void setWasteAccountCode(String wasteAccountCode) {
        this.wasteAccountCode = wasteAccountCode;
    }

    public CashAccount getAverageAccount() {
        return averageAccount;
    }

    public void setAverageAccount(CashAccount averageAccount) {
        this.averageAccount = averageAccount;
        setAverageAccountCode(this.averageAccount != null ? this.averageAccount.getAccountCode() : null);
    }

    public String getAverageAccountCode() {
        return averageAccountCode;
    }

    public void setAverageAccountCode(String averageAccountCode) {
        this.averageAccountCode = averageAccountCode;
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
        // Codigo de tipo de documento contable: alimenta getNextSeq() para la
        // numeracion de comprobantes, que resuelve por tipo. Se normaliza a
        // mayusculas para no depender del collation de la BD (antes lo hacia el
        // UpperCaseStringListener, que se quito de esta entidad).
        this.paymentDocumentOC = paymentDocumentOC != null ? paymentDocumentOC.toUpperCase() : null;
    }

    public CashAccount getAccountPayableSupplier() {
        return accountPayableSupplier;
    }

    public void setAccountPayableSupplier(CashAccount accountPayableSupplier) {
        this.accountPayableSupplier = accountPayableSupplier;
        setAccountPayableSupplierCode(this.accountPayableSupplier != null ? this.accountPayableSupplier.getAccountCode() : null);
    }

    public String getAccountPayableSupplierCode() {
        return accountPayableSupplierCode;
    }

    public void setAccountPayableSupplierCode(String accountPayableSupplierCode) {
        this.accountPayableSupplierCode = accountPayableSupplierCode;
    }

    public CashAccount getAccountBalanceSheet1() {
        return accountBalanceSheet1;
    }

    public void setAccountBalanceSheet1(CashAccount accountBalanceSheet1) {
        this.accountBalanceSheet1 = accountBalanceSheet1;
        setAccountBalanceSheet1Code(this.accountBalanceSheet1 != null ? this.accountBalanceSheet1.getAccountCode() : null);
    }

    public String getAccountBalanceSheet1Code() {
        return accountBalanceSheet1Code;
    }

    public void setAccountBalanceSheet1Code(String accountBalanceSheet1Code) {
        this.accountBalanceSheet1Code = accountBalanceSheet1Code;
    }

    public CashAccount getAccountBalanceSheet2() {
        return accountBalanceSheet2;
    }

    public void setAccountBalanceSheet2(CashAccount accountBalanceSheet2) {
        this.accountBalanceSheet2 = accountBalanceSheet2;
        setAccountBalanceSheet2Code(this.accountBalanceSheet2 != null ? this.accountBalanceSheet2.getAccountCode() : null);
    }

    public String getAccountBalanceSheet2Code() {
        return accountBalanceSheet2Code;
    }

    public void setAccountBalanceSheet2Code(String accountBalanceSheet2Code) {
        this.accountBalanceSheet2Code = accountBalanceSheet2Code;
    }

    public CashAccount getAccountBalanceSheet3() {
        return accountBalanceSheet3;
    }

    public void setAccountBalanceSheet3(CashAccount accountBalanceSheet3) {
        this.accountBalanceSheet3 = accountBalanceSheet3;
        setAccountBalanceSheet3Code(this.accountBalanceSheet3 != null ? this.accountBalanceSheet3.getAccountCode() : null);
    }

    public String getAccountBalanceSheet3Code() {
        return accountBalanceSheet3Code;
    }

    public void setAccountBalanceSheet3Code(String accountBalanceSheet3Code) {
        this.accountBalanceSheet3Code = accountBalanceSheet3Code;
    }

    public CashAccount getAccountBalanceSheet4() {
        return accountBalanceSheet4;
    }

    public void setAccountBalanceSheet4(CashAccount accountBalanceSheet4) {
        this.accountBalanceSheet4 = accountBalanceSheet4;
        setAccountBalanceSheet4Code(this.accountBalanceSheet4 != null ? this.accountBalanceSheet4.getAccountCode() : null);
    }

    public String getAccountBalanceSheet4Code() {
        return accountBalanceSheet4Code;
    }

    public void setAccountBalanceSheet4Code(String accountBalanceSheet4Code) {
        this.accountBalanceSheet4Code = accountBalanceSheet4Code;
    }

    public CashAccount getAccountBalanceSheet5() {
        return accountBalanceSheet5;
    }

    public void setAccountBalanceSheet5(CashAccount accountBalanceSheet5) {
        this.accountBalanceSheet5 = accountBalanceSheet5;
        setAccountBalanceSheet5Code(this.accountBalanceSheet5 != null ? this.accountBalanceSheet5.getAccountCode() : null);
    }

    public String getAccountBalanceSheet5Code() {
        return accountBalanceSheet5Code;
    }

    public void setAccountBalanceSheet5Code(String accountBalanceSheet5Code) {
        this.accountBalanceSheet5Code = accountBalanceSheet5Code;
    }

    public CashAccount getAccountPayableIVA() {
        return accountPayableIVA;
    }

    public void setAccountPayableIVA(CashAccount accountPayableIVA) {
        this.accountPayableIVA = accountPayableIVA;
        setAccountPayableIVACode(this.accountPayableIVA != null ? this.accountPayableIVA.getAccountCode() : null);
    }

    public String getAccountPayableIVACode() {
        return accountPayableIVACode;
    }

    public void setAccountPayableIVACode(String accountPayableIVACode) {
        this.accountPayableIVACode = accountPayableIVACode;
    }

    public CashAccount getAccountRegalia() {
        return accountRegalia;
    }

    public void setAccountRegalia(CashAccount accountRegalia) {
        this.accountRegalia = accountRegalia;
        setAccountRegaliaCode(this.accountRegalia != null ? this.accountRegalia.getAccountCode() : null);
    }

    public String getAccountRegaliaCode() {
        return accountRegaliaCode;
    }

    public void setAccountRegaliaCode(String accountRegaliaCode) {
        this.accountRegaliaCode = accountRegaliaCode;
    }

    public CashAccount getAccountRetentionCNS() {
        return accountRetentionCNS;
    }

    public void setAccountRetentionCNS(CashAccount accountRetentionCNS) {
        this.accountRetentionCNS = accountRetentionCNS;
        setAccountRetentionCNSCode(this.accountRetentionCNS != null ? this.accountRetentionCNS.getAccountCode() : null);
    }

    public String getAccountRetentionCNSCode() {
        return accountRetentionCNSCode;
    }

    public void setAccountRetentionCNSCode(String accountRetentionCNSCode) {
        this.accountRetentionCNSCode = accountRetentionCNSCode;
    }

    public BigDecimal getRetentionCNSValue() {
        return retentionCNSValue;
    }

    public void setRetentionCNSValue(BigDecimal retentionCNSValue) {
        this.retentionCNSValue = retentionCNSValue;
    }

    public String getDocumentFixedAssetOC() {
        return documentFixedAssetOC;
    }

    public void setDocumentFixedAssetOC(String documentFixedAssetOC) {
        // Ver nota en setPaymentDocumentOC: codigo de tipo de documento usado en
        // getNextSeq(); se mantiene en mayusculas por seguridad de la numeracion.
        this.documentFixedAssetOC = documentFixedAssetOC != null ? documentFixedAssetOC.toUpperCase() : null;
    }

    public CashAccount getLossCashAccount() {
        return lossCashAccount;
    }

    public void setLossCashAccount(CashAccount lossCashAccount) {
        this.lossCashAccount = lossCashAccount;
        setLossCashAccountCode(this.lossCashAccount != null ? this.lossCashAccount.getAccountCode() : null);
    }

    public String getLossCashAccountCode() {
        return lossCashAccountCode;
    }

    public void setLossCashAccountCode(String lossCashAccountCode) {
        this.lossCashAccountCode = lossCashAccountCode;
    }

    public CashAccount getProfitCashAccount() {
        return profitCashAccount;
    }

    public void setProfitCashAccount(CashAccount profitCashAccount) {
        this.profitCashAccount = profitCashAccount;
        setProfitCashAccountCode(this.profitCashAccount != null ? this.profitCashAccount.getAccountCode() : null);
    }

    public String getProfitCashAccountCode() {
        return profitCashAccountCode;
    }

    public void setProfitCashAccountCode(String profitCashAccountCode) {
        this.profitCashAccountCode = profitCashAccountCode;
    }

    public CashAccount getDefaultAccountPurchaseOrder() {
        return defaultAccountPurchaseOrder;
    }

    public void setDefaultAccountPurchaseOrder(CashAccount defaultAccountPurchaseOrder) {
        this.defaultAccountPurchaseOrder = defaultAccountPurchaseOrder;
        setDefaultAccountPurchaseOrderCode(this.defaultAccountPurchaseOrder != null ? this.defaultAccountPurchaseOrder.getAccountCode() : null);
    }

    public String getDefaultAccountPurchaseOrderCode() {
        return defaultAccountPurchaseOrderCode;
    }

    public void setDefaultAccountPurchaseOrderCode(String defaultAccountPurchaseOrderCode) {
        this.defaultAccountPurchaseOrderCode = defaultAccountPurchaseOrderCode;
    }

    public File getHeaderLogo() {
        return headerLogo;
    }

    public void setHeaderLogo(File headerLogo) {
        this.headerLogo = headerLogo;
    }

    public File getLoginLogo() {
        return loginLogo;
    }

    public void setLoginLogo(File loginLogo) {
        this.loginLogo = loginLogo;
    }

    /* ===================== Cuentas requeridas ===================== */

    /**
     * Valida que una cuenta de la configuracion este realmente utilizable.
     * Cubre los dos modos de falla que traia esta tabla, y que hasta ahora
     * llegaban al usuario como un NullPointerException o como un
     * EntityNotFoundException salido de un proxy de Hibernate, sin decir que
     * cuenta faltaba:
     * <ol>
     * <li>la columna esta en NULL;</li>
     * <li>la columna tiene un codigo que ya no existe en el plan de cuentas
     * (arcgms). Como la asociacion es LAZY, ahi Hibernate devuelve un proxy y
     * solo explota al tocarlo; por eso se fuerza la carga aca.</li>
     * </ol>
     */
    private static CashAccount requireAccount(CashAccount account, String columnName, String labelKey) {
        if (account == null) {
            throw new CompanyAccountNotConfiguredException(columnName, labelKey);
        }
        try {
            account.getAccountCode(); // fuerza la inicializacion del proxy lazy
        } catch (EntityNotFoundException e) {
            throw new CompanyAccountNotConfiguredException(columnName, labelKey);
        }
        return account;
    }

    /**
     * @return la cuenta de la columna <code>ctadiftipcam</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireBalanceExchangeRateAccount() {
        return requireAccount(getBalanceExchangeRateAccount(), "ctadiftipcam", "CompanyConfiguration.balanceExchangeRateAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaaitb</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAdjustmentForInflationAccount() {
        return requireAccount(getAdjustmentForInflationAccount(), "ctaaitb", "CompanyConfiguration.adjustmentForInflationAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaantprovme</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAdvancePaymentForeignCurrencyAccount() {
        return requireAccount(getAdvancePaymentForeignCurrencyAccount(), "ctaantprovme", "CompanyConfiguration.advancePaymentForeignCurrencyAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaantprovmn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAdvancePaymentNationalCurrencyAccount() {
        return requireAccount(getAdvancePaymentNationalCurrencyAccount(), "ctaantprovmn", "CompanyConfiguration.advancePaymentNationalCurrencyAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctadeptrame</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireDepositInTransitForeignCurrencyAccount() {
        return requireAccount(getDepositInTransitForeignCurrencyAccount(), "ctadeptrame", "CompanyConfiguration.depositInTransitForeignCurrencyAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctadeptramn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireDepositInTransitNationalCurrencyAccount() {
        return requireAccount(getDepositInTransitNationalCurrencyAccount(), "ctadeptramn", "CompanyConfiguration.depositInTransitNationalCurrencyAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaafet</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireFixedAssetInTransitAccount() {
        return requireAccount(getFixedAssetInTransitAccount(), "ctaafet", "CompanyConfiguration.fixedAssetInTransitAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaivacrefime</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireForeignCurrencyVATFiscalCreditAccount() {
        return requireAccount(getForeignCurrencyVATFiscalCreditAccount(), "ctaivacrefime", "CompanyConfiguration.foreignCurrencyVATFiscalCreditAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaivacrefimn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireNationalCurrencyVATFiscalCreditAccount() {
        return requireAccount(getNationalCurrencyVATFiscalCreditAccount(), "ctaivacrefimn", "CompanyConfiguration.nationalCurrencyVATFiscalCreditAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaivacrefitrmn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireNationalCurrencyVATFiscalCreditTransientAccount() {
        return requireAccount(getNationalCurrencyVATFiscalCreditTransientAccount(), "ctaivacrefitrmn", "CompanyConfiguration.nationalCurrencyVATFiscalCreditTransientAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaprovobu</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireProvisionByTangibleFixedAssetObsolescenceAccount() {
        return requireAccount(getProvisionByTangibleFixedAssetObsolescenceAccount(), "ctaprovobu", "CompanyConfiguration.provisionByTangibleFixedAssetObsolescenceAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaalmme</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWarehouseForeignCurrencyAccount() {
        return requireAccount(getWarehouseForeignCurrencyAccount(), "ctaalmme", "CompanyConfiguration.warehouseForeignCurrencyAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaalmmn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWarehouseNationalCurrencyAccount() {
        return requireAccount(getWarehouseNationalCurrencyAccount(), "ctaalmmn", "CompanyConfiguration.warehouseNationalCurrencyAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctatransalmme</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWarehouseForeignCurrencyTransientAccount() {
        return requireAccount(getWarehouseForeignCurrencyTransientAccount(), "ctatransalmme", "CompanyConfiguration.warehouseForeignCurrencyTransientAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctatransalmmn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWarehouseNationalCurrencyTransientAccount() {
        return requireAccount(getWarehouseNationalCurrencyTransientAccount(), "ctatransalmmn", "CompanyConfiguration.warehouseNationalCurrencyTransientAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctatransalm1mn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWarehouseNationalCurrencyTransientAccount1() {
        return requireAccount(getWarehouseNationalCurrencyTransientAccount1(), "ctatransalm1mn", "CompanyConfiguration.warehouseNationalCurrencyTransientAccount1");
    }
    /**
     * @return la cuenta de la columna <code>ctatransalm2mn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWarehouseNationalCurrencyTransientAccount2() {
        return requireAccount(getWarehouseNationalCurrencyTransientAccount2(), "ctatransalm2mn", "CompanyConfiguration.warehouseNationalCurrencyTransientAccount2");
    }
    /**
     * @return la cuenta de la columna <code>iue_ret</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireIueRetention() {
        return requireAccount(getIueRetention(), "iue_ret", "CompanyConfiguration.iueRetention");
    }
    /**
     * @return la cuenta de la columna <code>it_ret</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireItRetention() {
        return requireAccount(getItRetention(), "it_ret", "CompanyConfiguration.itRetention");
    }
    /**
     * @return la cuenta de la columna <code>ctacostpt</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireCtaCostPT() {
        return requireAccount(getCtaCostPT(), "ctacostpt", "CompanyConfiguration.ctaCostPT");
    }
    /**
     * @return la cuenta de la columna <code>ctaalmpt</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireCtaAlmPT() {
        return requireAccount(getCtaAlmPT(), "ctaalmpt", "CompanyConfiguration.ctaAlmPT");
    }
    /**
     * @return la cuenta de la columna <code>ctaalmptag</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireCtaAlmPTAG() {
        return requireAccount(getCtaAlmPTAG(), "ctaalmptag", "CompanyConfiguration.ctaAlmPTAG");
    }
    /**
     * @return la cuenta de la columna <code>ctacostpv</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireCtaCostPV() {
        return requireAccount(getCtaCostPV(), "ctacostpv", "CompanyConfiguration.ctaCostPV");
    }
    /**
     * @return la cuenta de la columna <code>ctaalmpv</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireCtaAlmPV() {
        return requireAccount(getCtaAlmPV(), "ctaalmpv", "CompanyConfiguration.ctaAlmPV");
    }
    /**
     * @return la cuenta de la columna <code>ctaMerma</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireWasteAccount() {
        return requireAccount(getWasteAccount(), "ctaMerma", "CompanyConfiguration.wasteAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaProm</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAverageAccount() {
        return requireAccount(getAverageAccount(), "ctaProm", "CompanyConfiguration.averageAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctamermabaj</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireLowAccount() {
        return requireAccount(getLowAccount(), "ctamermabaj", "CompanyConfiguration.lowAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctareproc</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireReworkAccount() {
        return requireAccount(getReworkAccount(), "ctareproc", "CompanyConfiguration.reworkAccount");
    }
    /**
     * @return la cuenta de la columna <code>ct_cajaahorro</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireSavingsBankAccount() {
        return requireAccount(getSavingsBankAccount(), "ct_cajaahorro", "CompanyConfiguration.savingsBankAccount");
    }
    /**
     * @return la cuenta de la columna <code>ct_cajaveter</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireVeterinaryCashAccount() {
        return requireAccount(getVeterinaryCashAccount(), "ct_cajaveter", "CompanyConfiguration.veterinaryCashAccount");
    }
    /**
     * @return la cuenta de la columna <code>cajagral1mn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireGeneralCashAccountNational() {
        return requireAccount(getGeneralCashAccountNational(), "cajagral1mn", "CompanyConfiguration.generalCashAccountNational");
    }
    /**
     * @return la caja general en moneda extranjera (columna <code>cajagral1me</code>).
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireGeneralCashAccountForeign() {
        return requireAccount(getGeneralCashAccountForeign(), "cajagral1me", "CompanyConfiguration.generalCashAccountForeign");
    }
    /**
     * @return la cuenta de la columna <code>i_pvig_pf_mn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireFixedTermInterestNationalCurrency() {
        return requireAccount(getFixedTermInterestNationalCurrency(), "i_pvig_pf_mn", "CompanyConfiguration.fixedTermInterestNationalCurrency");
    }
    /**
     * @return la cuenta de gasto de la provision de intereses sobre DPF en MN
     *         (columna <code>i_ppag_dpf_mn</code>).
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireFixedTermPayableInterestNationalCurrency() {
        return requireAccount(getFixedTermPayableInterestNationalCurrency(), "i_ppag_dpf_mn", "CompanyConfiguration.fixedTermPayableInterestNationalCurrency");
    }
    /**
     * @return la cuenta de gasto de la provision de intereses sobre DPF en ME
     *         (columna <code>i_ppag_dpf_me</code>).
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireFixedTermPayableInterestForeignCurrency() {
        return requireAccount(getFixedTermPayableInterestForeignCurrency(), "i_ppag_dpf_me", "CompanyConfiguration.fixedTermPayableInterestForeignCurrency");
    }
    /**
     * @return la cuenta de la columna <code>ctaprovaf</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireFixedAssetProvidersAccount() {
        return requireAccount(getFixedAssetProvidersAccount(), "ctaprovaf", "CompanyConfiguration.fixedAssetProvidersAccount");
    }
    /**
     * @return la cuenta de la columna <code>ctaG_it</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireTransactionTaxExpense() {
        return requireAccount(getTransactionTaxExpense(), "ctaG_it", "CompanyConfiguration.transactionTaxExpense");
    }
    /**
     * @return la cuenta de la columna <code>ctaP_debFisIva</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireFiscalDebitLiability() {
        return requireAccount(getFiscalDebitLiability(), "ctaP_debFisIva", "CompanyConfiguration.fiscalDebitLiability");
    }
    /**
     * @return la cuenta de la columna <code>ctaP_itxpagar</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireTransactionTaxPayable() {
        return requireAccount(getTransactionTaxPayable(), "ctaP_itxpagar", "CompanyConfiguration.transactionTaxPayable");
    }
    /**
     * @return la cuenta de la columna <code>ctaI_ventapri</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requirePrimarySaleProduct() {
        return requireAccount(getPrimarySaleProduct(), "ctaI_ventapri", "CompanyConfiguration.primarySaleProduct");
    }
    /**
     * @return la cuenta de la columna <code>ctaI_ventasec</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireSecondarySaleProduct() {
        return requireAccount(getSecondarySaleProduct(), "ctaI_ventasec", "CompanyConfiguration.secondarySaleProduct");
    }
    /**
     * @return la cuenta de la columna <code>ctacomision</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireCommissionSalesCashAccount() {
        return requireAccount(getCommissionSalesCashAccount(), "ctacomision", "CompanyConfiguration.commissionSalesCashAccount");
    }
    /**
     * @return la cuenta de la columna <code>cxp_provmn</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountPayableSupplier() {
        return requireAccount(getAccountPayableSupplier(), "cxp_provmn", "CompanyConfiguration.accountPayableSupplier");
    }
    /**
     * @return la cuenta de la columna <code>cxp_iva</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountPayableIVA() {
        return requireAccount(getAccountPayableIVA(), "cxp_iva", "CompanyConfiguration.accountPayableIVA");
    }
    /**
     * @return la cuenta de la columna <code>cxp_regalia</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountRegalia() {
        return requireAccount(getAccountRegalia(), "cxp_regalia", "CompanyConfiguration.accountRegalia");
    }
    /**
     * @return la cuenta de la columna <code>cxp_cns</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountRetentionCNS() {
        return requireAccount(getAccountRetentionCNS(), "cxp_cns", "CompanyConfiguration.accountRetentionCNS");
    }
    /**
     * @return la cuenta de la columna <code>cta_pat01</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountBalanceSheet1() {
        return requireAccount(getAccountBalanceSheet1(), "cta_pat01", "CompanyConfiguration.accountBalanceSheet1");
    }
    /**
     * @return la cuenta de la columna <code>cta_pat02</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountBalanceSheet2() {
        return requireAccount(getAccountBalanceSheet2(), "cta_pat02", "CompanyConfiguration.accountBalanceSheet2");
    }
    /**
     * @return la cuenta de la columna <code>cta_pat03</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountBalanceSheet3() {
        return requireAccount(getAccountBalanceSheet3(), "cta_pat03", "CompanyConfiguration.accountBalanceSheet3");
    }
    /**
     * @return la cuenta de la columna <code>cta_pat04</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountBalanceSheet4() {
        return requireAccount(getAccountBalanceSheet4(), "cta_pat04", "CompanyConfiguration.accountBalanceSheet4");
    }
    /**
     * @return la cuenta de la columna <code>cta_pat05</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireAccountBalanceSheet5() {
        return requireAccount(getAccountBalanceSheet5(), "cta_pat05", "CompanyConfiguration.accountBalanceSheet5");
    }
    /**
     * @return la cuenta de la columna <code>res_perdida</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireLossCashAccount() {
        return requireAccount(getLossCashAccount(), "res_perdida", "CompanyConfiguration.lossCashAccount");
    }
    /**
     * @return la cuenta de la columna <code>res_utilidad</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireProfitCashAccount() {
        return requireAccount(getProfitCashAccount(), "res_utilidad", "CompanyConfiguration.profitCashAccount");
    }
    /**
     * @return la cuenta de la columna <code>oc_pagodefault</code>.
     * @throws CompanyAccountNotConfiguredException si no esta configurada.
     */
    public CashAccount requireDefaultAccountPurchaseOrder() {
        return requireAccount(getDefaultAccountPurchaseOrder(), "oc_pagodefault", "CompanyConfiguration.defaultAccountPurchaseOrder");
    }
}