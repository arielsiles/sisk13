package com.encens.khipus.action.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.exception.finances.CompanyAccountNotConfiguredException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.finances.VoucherState;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.FixedTermDepositAccrual;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.AccountService;
import com.encens.khipus.service.customers.CreditService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("accountAction")
@Scope(ScopeType.CONVERSATION)
public class AccountAction extends GenericAction<Account> {


    @In
    private AccountService accountService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private SequenceGeneratorService sequenceGeneratorService;
    @In
    private CreditService creditService;
    @In
    private CompanyConfigurationService companyConfigurationService;


    private BigDecimal totalCredit  = BigDecimal.ZERO;
    private BigDecimal totalDebit   = BigDecimal.ZERO;
    private BigDecimal totalBalance = BigDecimal.ZERO;

    private BigDecimal totalCreditMe  = BigDecimal.ZERO;
    private BigDecimal totalDebitMe   = BigDecimal.ZERO;
    private BigDecimal totalBalanceMe = BigDecimal.ZERO;

    /**
     * Cuantos asientos tiene la cuenta en el mayor. Distingue "saldo cero porque se retiro
     * todo" de "saldo cero porque todavia no se contabilizo nada". Lo carga
     * {@link #calculateTotalAmounts(Account)}.
     */
    private int accountMovementCount;

    private Boolean partialRenewal;

    /** Current DPF **/
    private String newAccountCodeDPF;
    private BigDecimal capitalDPF;
    private BigDecimal interestDPF;
    private BigDecimal rcivaDPF;
    private BigDecimal totalAmountDPF;

    /** Renovation DPF **/
    /**
     * Si la operacion retiene el RC-IVA. Es de la OPERACION, no del certificado: el socio
     * presenta el NIT en el momento de renovar o de cerrar, y tiene que volver a
     * presentarlo la proxima vez. Arranca reflejando el flag del certificado y se decide
     * en cada operacion.
     * <p/>
     * El certificado nuevo de una renovacion sigue naciendo CON retencion, justamente
     * porque la exencion no se hereda.
     */
    private Boolean retentionRenewDPF;
    private BigDecimal capitalRenewDPF;
    /** Arranca en null para que el campo se vea vacio, no con un 0,00 heredado. */
    private BigDecimal partialCapitalRenewDPF;

    /** Tramos del interes ganado: uno por cada aumento de capital durante el plazo. */
    private List<FixedTermDepositProvision> interestSegmentsDPF = new ArrayList<FixedTermDepositProvision>();
    private AccountType accountTypeRenewDPF;
    private BigDecimal interestRenewDPF;
    private DocType documentType = new DocType();
    private String glossRenewDPF;
    private String beneficiary1;
    private String beneficiary2;

    private Date startDateDPF = new Date();
    private Date expirationDateDPF;

    /** Cierre de DPF **/
    private Date closingDateDPF;
    private BigDecimal closingCapitalDPF     = BigDecimal.ZERO;
    /** Interes bruto: cero si el cierre es hasta el vencimiento inclusive. */
    private BigDecimal closingInterestDPF    = BigDecimal.ZERO;
    private BigDecimal closingRcivaDPF       = BigDecimal.ZERO;
    private BigDecimal closingNetInterestDPF = BigDecimal.ZERO;
    /** Lo que se le entrega al socio: capital + interes neto. */
    private BigDecimal closingTotalDPF       = BigDecimal.ZERO;
    /** Lo que la provision mensual ya acredito en el pasivo por este certificado. */
    private BigDecimal closingAccruedDPF     = BigDecimal.ZERO;
    /** Interes bruto - provision acumulada. Negativo = reversa. */
    private BigDecimal closingExpenseDPF     = BigDecimal.ZERO;
    private List<FixedTermDepositProvision> closingAccruedDetail = new ArrayList<FixedTermDepositProvision>();
    /** Retencion del cierre. Mismo criterio que {@link #retentionRenewDPF}. */
    private Boolean retentionCloseDPF;
    private String glossCloseDPF;

    /**
     * Estado con el que se abrio la ficha. Es contra este valor que se decide si un cambio
     * de estado es una edicion legitima o una transicion que solo puede hacer una
     * operacion; ver {@link #validateStateTransition()}.
     */
    private AccountState originalAccountState;

    /** Codigo con el que se abrio la ficha; ver {@link #validateAccountCode()}. */
    private String originalAccountCode;

    /** Aprobacion del DPF: comprobante de ingreso de la apertura **/
    private Date approvalDateDPF;
    private String approvalGlossDPF;
    private BigDecimal approvalExchangeRateDPF = BigDecimal.ONE;
    private DocType approvalDocumentTypeDPF;
    /** Las lineas que se van a contabilizar, ya armadas: el modal muestra estas mismas. */
    private List<VoucherDetail> approvalDetailsDPF = new ArrayList<VoucherDetail>();
    private BigDecimal approvalTotalDebitDPF    = BigDecimal.ZERO;
    private BigDecimal approvalTotalCreditDPF   = BigDecimal.ZERO;
    private BigDecimal approvalTotalDebitMeDPF  = BigDecimal.ZERO;
    private BigDecimal approvalTotalCreditMeDPF = BigDecimal.ZERO;
    /** El modal solo se abre si el asiento se pudo armar entero. */
    private boolean approvalReady;

    /** Comprobante que se esta viendo en el modal de la pestana Transacciones. */
    private Voucher selectedVoucher;
    private List<VoucherDetail> selectedVoucherDetails = new ArrayList<VoucherDetail>();
    private BigDecimal selectedVoucherTotalDebit    = BigDecimal.ZERO;
    private BigDecimal selectedVoucherTotalCredit   = BigDecimal.ZERO;
    private BigDecimal selectedVoucherTotalDebitMe  = BigDecimal.ZERO;
    private BigDecimal selectedVoucherTotalCreditMe = BigDecimal.ZERO;


    private Partner partnerDPF;

    /** For capitalization **/
    private Date startDate;
    private Date endDate;

    private SavingType savingType;

    /** End **/

    @Factory(value = "account", scope = ScopeType.STATELESS)
    public Account initAccount() {
        return getInstance();
    }

    @Factory(value = "currencyTypeEnum", scope = ScopeType.STATELESS)
    public FinancesCurrencyType[] getFinancesCurrencyTypeEnum() {
        return FinancesCurrencyType.values();
    }

    @Factory(value = "accountStateEnum", scope = ScopeType.STATELESS)
    public AccountState[] getAccountStateEnum() {
        return AccountState.values();
    }

    @Factory(value = "savingTypeEnum", scope = ScopeType.STATELESS)
    public SavingType[] getSavingTypeEnum() {
        return SavingType.values();
    }

    @Override
    @Begin(join=true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Account instance) {
        calculateTotalAmounts(instance);
        String outcome = super.select(instance);
        originalAccountState = getInstance().getAccountState();
        originalAccountCode = getInstance().getCode();
        return outcome;
    }

    /**
     * Un DPF nuevo nace PENDIENTE: la ficha queda cargada pero el dinero todavia no entro.
     * Lo pasa a ACTIVE el boton Aprobar, que es lo unico que genera el comprobante de
     * ingreso. Las cuentas que no son DPF siguen tomando el estado del formulario.
     * <p/>
     * Ya no termina la conversacion: devuelve SUCCESS con <code>op=UPDATE</code> y
     * pages.xml redirige a esta misma pantalla, con lo que vuelve en modo edicion sobre el
     * registro recien grabado. Antes redibujaba el formulario sin recargarlo -- el
     * vencimiento y el capital se guardaban bien pero desaparecian de la pantalla -- y un
     * segundo clic en Guardar daba de alta la cuenta dos veces.
     */
    @Override
    public String create() {
        try {
            if (isDpfAccount(getInstance())) {
                getInstance().setAccountState(AccountState.PENDING);
                String code = generateAccountCode(getInstance());
                if (code == null) {
                    return Outcome.REDISPLAY;
                }
                getInstance().setCode(code);
            } else if (getInstance().getAccountState() == null) {
                /** El estado dejo de ser un campo del formulario: una cuenta de ahorro
                 *  nace activa, que es lo unico que tiene sentido al darla de alta. */
                getInstance().setAccountState(AccountState.ACTIVE);
            }
            if (!validateAccountCode()) {
                return Outcome.REDISPLAY;
            }
            getInstance().setAccountNumber(generateAccountNumber());
            getService().create(getInstance());
            addCreatedMessage();
            setOp(OP_UPDATE);
            originalAccountState = getInstance().getAccountState();
            originalAccountCode = getInstance().getCode();
            calculateTotalAmounts(getInstance());
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End(beforeRedirect = true)
    public String cancelRenovationDPF() {

        setCapitalRenewDPF(null);
        setInterestRenewDPF(null);
        setAccountTypeRenewDPF(null);
        setDocumentType(null);
        setGlossRenewDPF(null);
        setRetentionRenewDPF(null);

        return Outcome.CANCEL;
    }

    /**
     * Un certificado terminado: ya se renovo, o se retiro todo el dinero, o nunca fue una
     * operacion real. No queda capital que renovar y volver a hacerlo duplicaria el pasivo.
     * <p/>
     * Se mira por dos lados porque se llega por dos caminos distintos:
     * <p/>
     * 1) El estado. {@link #createDpfRenewal()} deja el certificado viejo en INACTIVE, y
     *    ANNULLED marca al que nunca existio como operacion.
     * 2) El mayor. Un retiro total registrado por comprobante NO toca el estado: la cuenta
     *    queda en ACTIVE con saldo cero. Por eso el estado solo no alcanza.
     * <p/>
     * El saldo cero solo cuenta si hay movimientos. Un certificado sin ningun asiento
     * tambien da saldo cero y no esta cerrado: esta sin contabilizar (caso ME00148).
     * <p/>
     * Los totales salen de {@link #calculateTotalAmounts(Account)}, que corre en select():
     * este metodo describe la cuenta que se esta editando, no una cualquiera.
     */
    public boolean isClosed(Account account) {
        if (account == null) {
            return false;
        }
        if (AccountState.ANNULLED.equals(account.getAccountState())
                || AccountState.INACTIVE.equals(account.getAccountState())) {
            return true;
        }
        if (accountMovementCount <= 0) {
            return false;
        }
        BigDecimal balance = isForeignAccount() ? getTotalBalanceMe() : getTotalBalance();
        return balance != null && balance.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Condiciones para poder renovar un DPF:
     * <p/>
     * 1) Que no este terminado (ver {@link #isClosed(Account)}).
     * 2) Que tenga fecha de vencimiento cargada; sin plazo no hay interes que calcular.
     * 3) Que el plazo se haya cumplido: nunca se paga interes por dias que no
     *    transcurrieron. El dia del vencimiento cuenta como cumplido, porque el
     *    certificado devenga hasta esa fecha inclusive.
     */
    public boolean isRenewable(Account account) {
        if (account == null
                || isClosed(account)
                || account.getExpirationDate() == null) {
            return false;
        }
        return !DateUtils.removeTime(account.getExpirationDate()).after(DateUtils.toDay());
    }

    /**
     * Motivo por el que Renovar esta deshabilitado, ya resuelto a texto. Cadena vacia si
     * se puede renovar. Se arma aca y no en la vista para no repetir la cadena de
     * condiciones de {@link #isRenewable(Account)} en un EL.
     */
    public String getRenewalBlockedReason() {
        Account account = getInstance();
        if (account == null || isRenewable(account)) {
            return "";
        }
        if (AccountState.ANNULLED.equals(account.getAccountState())) {
            return messages.get("Account.renewal.blocked.annulled");
        }
        if (isClosed(account)) {
            return messages.get("Account.renewal.blocked.closed");
        }
        if (account.getExpirationDate() == null) {
            return messages.get("Account.renewal.blocked.noExpirationDate");
        }
        return MessageFormat.format(messages.get("Account.renewal.blocked.notExpiredYet"),
                getExpirationDateLabel());
    }

    /** Vencimiento del certificado ya formateado, para el mensaje de la pantalla. */
    public String getExpirationDateLabel() {
        Date expirationDate = getInstance() != null ? getInstance().getExpirationDate() : null;
        return expirationDate != null ? DateUtils.format(expirationDate, "dd/MM/yyyy") : "";
    }

    /**
     * Fecha de inicio del nuevo certificado: el dia siguiente al vencimiento, o hoy si el
     * vencimiento ya paso hace tiempo. Asi no queda un hueco sin devengar entre los dos
     * certificados ni se arranca antes de que el anterior termine.
     * <p/>
     * Sin fecha de vencimiento se cae a hoy en vez de reventar: la pantalla se abre igual
     * para consultar y el boton Renovar ya queda deshabilitado por {@link #isRenewable}.
     */
    private Date calculateRenewalStartDate(Account account) {
        Date today = DateUtils.toDay();
        Date expirationDate = DateUtils.removeTime(account.getExpirationDate());
        if (expirationDate == null) {
            return today;
        }
        Date dayAfterExpiration = DateUtils.addDay(expirationDate, 1);
        return dayAfterExpiration.after(today) ? dayAfterExpiration : today;
    }

    /**
     * Un certificado terminado ni siquiera abre la pantalla: ya se renovo o se retiro todo
     * el dinero, no hay nada que calcular ni que renovar.
     * <p/>
     * El plazo, en cambio, NO se valida aca a proposito: mientras el DPF sigue vigente la
     * pantalla se abre y muestra todos los calculos (capital, interes por tramos, retencion,
     * total). Lo que se bloquea en ese caso es el boton Renovar, asi se puede consultar sin
     * poder ejecutar.
     */
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String renewalDPF() {
        if (isClosed(getInstance())) {
            facesMessages.add(StatusMessage.Severity.ERROR, getRenewalBlockedReason());
            return Outcome.REDISPLAY;
        }
        setOp(OP_UPDATE);

        if (!isForeignAccount())
            setCapitalDPF(getTotalBalance());
        if (isForeignAccount())
            setCapitalDPF(getTotalBalanceMe());

        /** El interes se calcula por tramos: si el certificado recibio un aumento de
         *  capital durante el plazo, los dias anteriores devengan sobre el capital que
         *  tenia antes. Con un solo tramo -- el caso normal -- da igual que antes. */
        setInterestSegmentsDPF(buildInterestSegments(getInstance()));
        BigDecimal interestVal = BigDecimal.ZERO;
        for (FixedTermDepositProvision segment : getInterestSegmentsDPF()) {
            interestVal = BigDecimalUtil.sum(interestVal, segment.getProvision(), 6);
        }
        setInterestDPF(interestVal);

        /** Arranca como venga el certificado; el operador lo cambia si el socio presenta
         *  el NIT en esta renovacion. */
        setRetentionRenewDPF(getInstance().getRetentionFlag());
        calculateRenewalAmounts();

        /** Defaults del nuevo certificado: mismo socio y mismo tipo de cuenta, con el
         *  vencimiento ya calculado. Quedan editables. */
        setPartnerDPF(getInstance().getPartner());
        setAccountTypeRenewDPF(getInstance().getAccountType());
        setStartDateDPF(calculateRenewalStartDate(getInstance()));
        calculateExpirationDateDPF();

        return Outcome.SUCCESS;
    }

    /**
     * Recalcula la retencion y los importes que dependen de ella. Lo dispara el check de
     * retencion de la pantalla de renovacion.
     * <p/>
     * Pisa el capital a renovar con el total sugerido: al cambiar la retencion cambia lo
     * que hay para capitalizar, y dejar el importe anterior seria mostrar un total que no
     * corresponde. El operador puede volver a editarlo despues.
     * <p/>
     * La retencion se calcula sobre el interes total, no por tramo.
     */
    public void calculateRenewalAmounts() {
        recalculateRenewalRciva();
        setCapitalRenewDPF(totalAmountDPF);
    }

    /**
     * Retencion y total sugerido, sin tocar el capital a renovar: lo llama tambien el
     * grabado, donde el operador pudo haber editado ese importe a mano.
     */
    private void recalculateRenewalRciva() {
        if (Boolean.TRUE.equals(retentionRenewDPF)) {
            setRcivaDPF(BigDecimalUtil.multiply(getInterestDPF(), Constants.VAT));
        } else {
            setRcivaDPF(BigDecimal.ZERO);
        }
        totalAmountDPF = BigDecimalUtil.sum(capitalDPF, interestDPF);
        totalAmountDPF = BigDecimalUtil.subtract(totalAmountDPF, rcivaDPF);
    }

    /**
     * Abre la pantalla de cierre de un DPF. Reemplaza al procedimiento manual de cargar el
     * comprobante por Contabilidad y despues acordarse de inactivar el certificado.
     */
    @Restrict("#{s:hasPermission('DPFCLOSE','VIEW')}")
    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String closeDPF() {
        if (isClosed(getInstance())) {
            facesMessages.add(StatusMessage.Severity.ERROR, getRenewalBlockedReason());
            return Outcome.REDISPLAY;
        }
        setOp(OP_UPDATE);
        setClosingDateDPF(DateUtils.toDay());
        setGlossCloseDPF(buildClosingGloss());
        /** Igual que en la renovacion: arranca como venga el certificado y el operador lo
         *  cambia si el socio presenta el NIT al cerrar. */
        setRetentionCloseDPF(getInstance().getRetentionFlag());
        calculateClosure();
        return Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    public String cancelCloseDPF() {
        clearClosingFields();
        return Outcome.CANCEL;
    }

    /**
     * Recalcula todo el cierre. Se dispara al cambiar la fecha, que es lo unico editable
     * que mueve los importes: de ella depende si el plazo se cumplio o no.
     */
    public void calculateClosure() {
        Account account = getInstance();
        Date closingDate = DateUtils.removeTime(closingDateDPF);

        closingCapitalDPF = isForeignAccount() ? getTotalBalanceMe() : getTotalBalance();

        /**
         * El interes depende de una sola cosa: si el plazo se cumplio.
         * <p/>
         * Cerrar hasta el dia del vencimiento inclusive es retiro anticipado y no paga
         * interes: el socio resigna lo devengado. Desde el dia siguiente el plazo esta
         * cumplido y cobra lo mismo que si renovara, con el mismo calculo por tramos.
         */
        if (isClosedAfterExpiration(account, closingDate)) {
            setInterestSegmentsDPF(buildInterestSegments(account));
            BigDecimal gross = BigDecimal.ZERO;
            for (FixedTermDepositProvision segment : getInterestSegmentsDPF()) {
                gross = BigDecimalUtil.sum(gross, segment.getProvision(), 6);
            }
            closingInterestDPF = BigDecimalUtil.roundBigDecimal(gross, 2);
            closingRcivaDPF = Boolean.TRUE.equals(retentionCloseDPF)
                    ? BigDecimalUtil.roundBigDecimal(
                            BigDecimalUtil.multiply(closingInterestDPF, Constants.VAT, 6), 2)
                    : BigDecimal.ZERO;
        } else {
            setInterestSegmentsDPF(new ArrayList<FixedTermDepositProvision>());
            closingInterestDPF = BigDecimal.ZERO;
            closingRcivaDPF = BigDecimal.ZERO;
        }

        closingNetInterestDPF = BigDecimalUtil.subtract(closingInterestDPF, closingRcivaDPF, 2);
        closingTotalDPF = BigDecimalUtil.sum(closingCapitalDPF, closingNetInterestDPF, 2);

        /**
         * Lo que la provision mensual ya acredito en "cargos financieros por pagar" por
         * este certificado. El asiento tiene que debitarlo COMPLETO: si se debita de menos
         * queda un saldo en el pasivo que no le corresponde a ningun DPF vivo y que ya no
         * se limpia nunca.
         */
        closingAccruedDetail = buildAccruedProvisionDetail(account, closingDate);
        BigDecimal accrued = BigDecimal.ZERO;
        for (FixedTermDepositProvision detail : closingAccruedDetail) {
            accrued = BigDecimalUtil.sum(accrued, detail.getProvision(), 6);
        }
        closingAccruedDPF = BigDecimalUtil.roundBigDecimal(accrued, 2);

        /**
         * Lo que falta reconocer como gasto: se compara contra el interes NETO, no contra
         * el bruto, porque el asiento no lleva linea de RC-IVA. Es el criterio que se viene
         * usando desde 2019 -- ni una sola retencion registrada en 2420310100 en toda la
         * base --, y se mantiene para no cambiarlo de golpe.
         * <p/>
         * Con eso la retencion no queda colgada en ningun lado: simplemente baja el costo
         * financiero del certificado, porque es plata que no se pago.
         * <p/>
         * Negativo cuando se provisiono mas de lo que finalmente se paga: el caso del
         * retiro anticipado, donde el interes es cero y todo lo provisionado se reversa.
         */
        closingExpenseDPF = BigDecimalUtil.subtract(closingNetInterestDPF, closingAccruedDPF, 2);
    }

    /** Un cierre posterior al vencimiento paga el plazo completo; hasta el vencimiento, nada. */
    public boolean isClosedAfterExpiration(Account account, Date closingDate) {
        Date expirationDate = DateUtils.removeTime(account.getExpirationDate());
        return expirationDate != null && closingDate != null && closingDate.after(expirationDate);
    }

    /**
     * Provision mensual ya contabilizada de este certificado, mes por mes.
     * <p/>
     * El mes del cierre NO entra: un DPF no devenga en el mes en que se cierra, porque este
     * mismo asiento liquida ese tramo. Es el mismo criterio que aplica la provision para
     * saltearlo, asi los dos lados coinciden.
     * <p/>
     * Se recalcula en vez de leerse del mayor porque la provision agrupa por cuenta
     * contable: el asiento mensual no deja rastro de cuanto le toco a cada DPF. Por eso el
     * calculo comparte codigo con la provision ({@link FixedTermDepositAccrual}) -- si
     * cada uno tuviera el suyo, cualquier diferencia quedaria colgada en el pasivo.
     */
    private List<FixedTermDepositProvision> buildAccruedProvisionDetail(Account account, Date closingDate) {
        List<FixedTermDepositProvision> months = new ArrayList<FixedTermDepositProvision>();

        Date openingDate = DateUtils.removeTime(account.getOpeningDate());
        BigDecimal rate = account.getAccountType() != null ? account.getAccountType().getInta() : null;
        if (closingDate == null || openingDate == null || rate == null) {
            return months;
        }
        Date expirationDate = DateUtils.removeTime(account.getExpirationDate());
        boolean foreign = FinancesCurrencyType.D.equals(account.getCurrency());
        List<Object[]> movements = loadLedgerMovements(account);

        Calendar cursor = Calendar.getInstance();
        cursor.setTime(openingDate);
        cursor.set(Calendar.DAY_OF_MONTH, 1);

        while (true) {
            Date monthStart = DateUtils.removeTime(cursor.getTime());
            Calendar endOfMonth = (Calendar) cursor.clone();
            endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date monthEnd = DateUtils.removeTime(endOfMonth.getTime());

            /** Corta en el mes del cierre: ese ya no se provisiona. */
            if (!monthEnd.before(closingDate)) {
                break;
            }

            Date from = monthStart.before(openingDate) ? openingDate : monthStart;
            Date until = (expirationDate != null && expirationDate.before(monthEnd))
                    ? expirationDate : monthEnd;

            if (!until.before(from)) {
                BigDecimal amount = FixedTermDepositAccrual.accrualBetween(movements, from, until, rate, foreign);
                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    FixedTermDepositProvision detail = new FixedTermDepositProvision();
                    detail.setAccount(account);
                    detail.setProvisionFromDate(from);
                    detail.setProvisionUntilDate(until);
                    detail.setRate(rate);
                    detail.setCapital(FixedTermDepositAccrual.capitalAt(movements, from, foreign));
                    detail.setDays((int) DateUtils.daysBetween(from, until, true));
                    detail.setProvision(amount);
                    months.add(detail);
                }
            }
            cursor.add(Calendar.MONTH, 1);
        }
        return months;
    }

    /**
     * Carga el comprobante completo de una fila de la pestana Transacciones, para verlo en
     * el modal. Sirve para revisar como quedo contabilizado el movimiento sin salir de la
     * ficha del certificado.
     * <p/>
     * No se reusa voucherCreateAction.initView() a proposito: ese action es del modulo de
     * contabilidad, vive en conversacion y ademas carga documentos de compra que aca no
     * hacen falta.
     */
    public void viewVoucher(VoucherDetail accountDetail) {
        selectedVoucher = accountDetail != null ? accountDetail.getVoucher() : null;
        selectedVoucherDetails = new ArrayList<VoucherDetail>();
        selectedVoucherTotalDebit = BigDecimal.ZERO;
        selectedVoucherTotalCredit = BigDecimal.ZERO;
        selectedVoucherTotalDebitMe = BigDecimal.ZERO;
        selectedVoucherTotalCreditMe = BigDecimal.ZERO;
        if (selectedVoucher == null) {
            return;
        }
        /**
         * Refresh y no una consulta a secas: un asiento generado en esta misma conversacion
         * -- el cierre, la aprobacion, la renovacion -- sigue vivo en el contexto de
         * persistencia con la asociacion cashAccount sin cargar, porque buildAccountEntryDetail
         * solo setea el codigo de cuenta. Consultar de nuevo devuelve esos mismos objetos
         * desde el cache de primer nivel, y getFullCashAccount() revienta al dereferenciarla.
         */
        selectedVoucherDetails = voucherAccoutingService.refreshVoucherDetailList(selectedVoucher);
        for (VoucherDetail detail : selectedVoucherDetails) {
            selectedVoucherTotalDebit = BigDecimalUtil.sum(selectedVoucherTotalDebit, detail.getDebit(), 2);
            selectedVoucherTotalCredit = BigDecimalUtil.sum(selectedVoucherTotalCredit, detail.getCredit(), 2);
            selectedVoucherTotalDebitMe = BigDecimalUtil.sum(selectedVoucherTotalDebitMe, detail.getDebitMe(), 2);
            selectedVoucherTotalCreditMe = BigDecimalUtil.sum(selectedVoucherTotalCreditMe, detail.getCreditMe(), 2);
        }
    }

    private List<Object[]> loadLedgerMovements(Account account) {
        List<Long> accountIds = new ArrayList<Long>();
        accountIds.add(account.getId());
        return accountService.getAccountLedgerMovements(accountIds);
    }

    /**
     * Registra el cierre: el asiento contable Y la baja del certificado, en una sola
     * operacion. Antes eran dos pasos manuales y el segundo se olvidaba, con lo que el DPF
     * quedaba figurando vivo y la provision lo seguia devengando.
     * <p/>
     * El asiento replica el que venia armando el contador a mano:
     * <pre>
     *   Debe   capital del DPF                    capital
     *   Debe   cargos financieros por pagar       provision ya acumulada
     *   Debe   intereses sobre DPF (gasto)        interes neto - provision acumulada
     *   Haber  caja                               capital + interes neto
     * </pre>
     * La tercera linea se invierte a HABER cuando da negativo, que es el retiro anticipado:
     * el interes es cero y lo provisionado se reversa entero contra el gasto.
     * <p/>
     * Sin linea de RC-IVA, igual que las renovaciones desde 2019.
     */
    @Restrict("#{s:hasPermission('DPFCLOSE','CREATE')}")
    @End(beforeRedirect = true, ifOutcome = Outcome.SUCCESS)
    public String createDpfClosure() {
        if (!validateClosure()) {
            return Outcome.REDISPLAY;
        }

        /** Mismo criterio que la renovacion: los importes se recalculan con la retencion
         *  marcada antes de armar el asiento. */
        calculateClosure();

        Account account = getInstance();
        Date closingDate = DateUtils.removeTime(closingDateDPF);
        boolean foreign = isForeignAccount();
        FinancesCurrencyType currency = foreign ? FinancesCurrencyType.D : FinancesCurrencyType.P;

        BigDecimal exchangeRate = BigDecimal.ONE;
        if (foreign) {
            try {
                /** El tipo de cambio del dia del asiento, no el ultimo cargado. */
                exchangeRate = financesExchangeRateService
                        .findExchangeRateByDateByCurrency(closingDate, FinancesCurrencyType.D.toString());
            } catch (FinancesExchangeRateNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                return Outcome.REDISPLAY;
            } catch (FinancesCurrencyNotFoundException e) {
                addFinancesCurrencyNotFoundMessage();
                return Outcome.REDISPLAY;
            }
        }

        CompanyConfiguration companyConfiguration;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return Outcome.REDISPLAY;
        }

        AccountType accountType = account.getAccountType();
        CashAccount capitalAccount = foreign ? accountType.getCashAccountMe() : accountType.getCashAccountMn();
        CashAccount chargeAccount = foreign ? accountType.getCashAccountChargeMe() : accountType.getCashAccountChargeMn();
        if (capitalAccount == null || chargeAccount == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.closingAccountsMissing", accountType.getName());
            return Outcome.REDISPLAY;
        }

        CashAccount cashAccount;
        CashAccount expenseAccount;
        try {
            cashAccount = foreign
                    ? companyConfiguration.requireGeneralCashAccountForeign()
                    : companyConfiguration.requireGeneralCashAccountNational();
            expenseAccount = foreign
                    ? companyConfiguration.requireFixedTermPayableInterestForeignCurrency()
                    : companyConfiguration.requireFixedTermPayableInterestNationalCurrency();
        } catch (CompanyAccountNotConfiguredException e) {
            facesMessages.add(StatusMessage.Severity.ERROR,
                    MessageUtils.getMessage("CompanyConfiguration.account.notConfigured",
                            MessageUtils.getMessage(e.getLabelKey())));
            return Outcome.REDISPLAY;
        }

        Voucher voucher = buildDpfVoucher(documentType.getName(), glossCloseDPF, closingDate);

        VoucherDetail capitalDetail = buildAccountEntryDetail(capitalAccount.getAccountCode(),
                closingCapitalDPF, "DEBIT", currency, foreign, exchangeRate);
        capitalDetail.setPartnerAccount(account);
        voucher.getDetails().add(capitalDetail);

        if (closingAccruedDPF.compareTo(BigDecimal.ZERO) > 0) {
            voucher.getDetails().add(buildAccountEntryDetail(chargeAccount.getAccountCode(),
                    closingAccruedDPF, "DEBIT", currency, foreign, exchangeRate));
        }

        if (closingExpenseDPF.compareTo(BigDecimal.ZERO) > 0) {
            voucher.getDetails().add(buildAccountEntryDetail(expenseAccount.getAccountCode(),
                    closingExpenseDPF, "DEBIT", currency, foreign, exchangeRate));
        } else if (closingExpenseDPF.compareTo(BigDecimal.ZERO) < 0) {
            voucher.getDetails().add(buildAccountEntryDetail(expenseAccount.getAccountCode(),
                    closingExpenseDPF.negate(), "CREDIT", currency, foreign, exchangeRate));
        }

        /**
         * NO se registra la retencion RC-IVA, por decision expresa: se sigue el criterio de
         * las renovaciones de 2019 en adelante, que la descuentan del importe pagado pero no
         * la acreditan en 2420310100. Al socio se le informa igual, en pantalla y en el
         * certificado impreso.
         */
        voucher.getDetails().add(buildAccountEntryDetail(cashAccount.getAccountCode(),
                closingTotalDPF, "CREDIT", currency, foreign, exchangeRate));

        /** El cierre del certificado y su asiento van juntos: misma transaccion. */
        account.setAccountState(AccountState.INACTIVE);
        accountService.updateAccount(account);
        voucherAccoutingService.saveVoucher(voucher);

        /** Se vuelve a la ficha del certificado, no al listado: ahi se ve el asiento que
         *  acaba de generarse en la pestana Transacciones y se lo puede imprimir. Por eso
         *  hay que dejar los totales al dia, que si no muestran el saldo de antes. */
        originalAccountState = account.getAccountState();
        calculateTotalAmounts(account);

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Account.message.closed", account.getCode());
        clearClosingFields();
        return Outcome.SUCCESS;
    }

    /**
     * Controles previos al cierre. Se validan aca y no en la vista porque son reglas del
     * negocio y tienen que valer aunque alguien fuerce el submit.
     */
    private boolean validateClosure() {
        boolean valid = true;
        Account account = getInstance();

        if (isClosed(account)) {
            facesMessages.add(StatusMessage.Severity.ERROR, getRenewalBlockedReason());
            return false;
        }
        if (documentType == null || documentType.getName() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.renewalDocumentTypeRequired");
            valid = false;
        }
        if (closingDateDPF == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.closingDateRequired");
            return false;
        }
        Date closingDate = DateUtils.removeTime(closingDateDPF);
        Date openingDate = DateUtils.removeTime(account.getOpeningDate());
        if (openingDate != null && closingDate.before(openingDate)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.closingDateBeforeOpening",
                    DateUtils.format(openingDate, "dd/MM/yyyy"));
            valid = false;
        }
        if (closingCapitalDPF == null || closingCapitalDPF.doubleValue() <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.closingCapitalRequired", account.getCode());
            valid = false;
        }
        return valid;
    }

    /** Glosa sugerida con el formato que se venia usando a mano. Queda editable. */
    private String buildClosingGloss() {
        Account account = getInstance();
        String partner = account.getPartner() != null ? account.getPartner().getFullName() : "";
        return MessageUtils.getMessage("Account.closing.gloss", partner, account.getCode());
    }

    private void clearClosingFields() {
        closingDateDPF = null;
        closingCapitalDPF = BigDecimal.ZERO;
        closingInterestDPF = BigDecimal.ZERO;
        closingRcivaDPF = BigDecimal.ZERO;
        closingNetInterestDPF = BigDecimal.ZERO;
        closingTotalDPF = BigDecimal.ZERO;
        closingAccruedDPF = BigDecimal.ZERO;
        closingExpenseDPF = BigDecimal.ZERO;
        closingAccruedDetail = new ArrayList<FixedTermDepositProvision>();
        retentionCloseDPF = null;
        glossCloseDPF = null;
        documentType = new DocType();
    }

    /** Un DPF cargado pero todavia sin contabilizar: el unico que se puede aprobar. */
    public boolean isPendingDpf(Account account) {
        return account != null
                && isDpfAccount(account)
                && AccountState.PENDING.equals(account.getAccountState());
    }

    /**
     * Certificado DPF ya aprobado, es decir con su apertura contabilizada. Sus datos de
     * fondo -- capital, fechas y tipo de cuenta -- quedan bloqueados en la ficha: cambiar
     * cualquiera de ellos la desincroniza del asiento que ya esta en el mayor y del
     * interes que se viene devengando.
     * <p/>
     * Alcanza tambien a los certificados anteriores a esta pantalla, que nacieron ACTIVE
     * sin pasar por PENDING. Es correcto: todos ellos ya estan contabilizados.
     */
    public boolean isApprovedDpf(Account account) {
        return account != null
                && isManaged()
                && isDpfAccount(account)
                && !AccountState.PENDING.equals(account.getAccountState());
    }

    /**
     * Arma el comprobante de ingreso de la apertura y lo deja listo para que el modal lo
     * muestre. Lo que se ve es exactamente lo que se graba: la lista que se dibuja es la
     * misma que despues recibe saveVoucher, asi que no hay forma de que la pantalla
     * prometa un asiento y se contabilice otro.
     * <p/>
     * Cualquier cosa que falte -- tipo de cambio del dia, cuentas sin configurar, capital
     * en cero -- corta aca con un mensaje y deja {@link #approvalReady} en falso, que es
     * lo que impide que el modal se abra.
     */
    public void prepareApprovalDPF() {
        approvalReady = false;
        approvalDetailsDPF = new ArrayList<VoucherDetail>();
        approvalTotalDebitDPF = BigDecimal.ZERO;
        approvalTotalCreditDPF = BigDecimal.ZERO;
        approvalTotalDebitMeDPF = BigDecimal.ZERO;
        approvalTotalCreditMeDPF = BigDecimal.ZERO;

        Account account = getInstance();
        if (!isPendingDpf(account)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.approvalNotPending");
            return;
        }
        if (account.getCapital() == null || account.getCapital().doubleValue() <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.approvalCapitalRequired");
            return;
        }
        if (account.getExpirationDate() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.approvalExpirationDateRequired");
            return;
        }

        boolean foreign = isForeignAccount();
        /** El comprobante se fecha el dia de la apertura del certificado, no el dia en que
         *  se lo aprueba: es la fecha desde la que devenga. */
        approvalDateDPF = DateUtils.removeTime(account.getOpeningDate());
        approvalExchangeRateDPF = BigDecimal.ONE;
        if (foreign) {
            try {
                approvalExchangeRateDPF = financesExchangeRateService
                        .findExchangeRateByDateByCurrency(approvalDateDPF, FinancesCurrencyType.D.toString());
            } catch (FinancesExchangeRateNotFoundException e) {
                addFinancesExchangeRateNotFoundExceptionMessage();
                return;
            } catch (FinancesCurrencyNotFoundException e) {
                addFinancesCurrencyNotFoundMessage();
                return;
            }
        }

        CompanyConfiguration configuration;
        try {
            configuration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return;
        }

        approvalDocumentTypeDPF = configuration.getFixedTermDepositDocumentType();
        if (approvalDocumentTypeDPF == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.approvalDocumentTypeNotConfigured");
            return;
        }

        CashAccount cashAccount;
        try {
            cashAccount = foreign
                    ? configuration.requireGeneralCashAccountForeign()
                    : configuration.requireGeneralCashAccountNational();
        } catch (CompanyAccountNotConfiguredException e) {
            facesMessages.add(StatusMessage.Severity.ERROR,
                    MessageUtils.getMessage("CompanyConfiguration.account.notConfigured",
                            MessageUtils.getMessage(e.getLabelKey())));
            return;
        }

        AccountType accountType = account.getAccountType();
        CashAccount capitalAccount = foreign ? accountType.getCashAccountMe() : accountType.getCashAccountMn();
        if (capitalAccount == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.approvalCapitalAccountMissing", accountType.getName());
            return;
        }

        FinancesCurrencyType currency = foreign ? FinancesCurrencyType.D : FinancesCurrencyType.P;
        BigDecimal capital = BigDecimalUtil.roundBigDecimal(account.getCapital(), 2);

        /** Entra la plata: Debe caja. */
        VoucherDetail cashDetail = buildAccountEntryDetail(cashAccount.getAccountCode(),
                capital, "DEBIT", currency, foreign, approvalExchangeRateDPF);
        cashDetail.setCashAccount(cashAccount);

        /** Nace el pasivo con el socio: Haber capital del DPF, con el certificado enlazado
         *  en la linea. Ese enlace es el que hace que el movimiento aparezca despues en la
         *  pestana Transacciones de la ficha. */
        VoucherDetail capitalDetail = buildAccountEntryDetail(capitalAccount.getAccountCode(),
                capital, "CREDIT", currency, foreign, approvalExchangeRateDPF);
        capitalDetail.setCashAccount(capitalAccount);
        capitalDetail.setPartnerAccount(account);

        approvalDetailsDPF.add(cashDetail);
        approvalDetailsDPF.add(capitalDetail);

        for (VoucherDetail detail : approvalDetailsDPF) {
            approvalTotalDebitDPF = BigDecimalUtil.sum(approvalTotalDebitDPF, detail.getDebit(), 2);
            approvalTotalCreditDPF = BigDecimalUtil.sum(approvalTotalCreditDPF, detail.getCredit(), 2);
            approvalTotalDebitMeDPF = BigDecimalUtil.sum(approvalTotalDebitMeDPF, detail.getDebitMe(), 2);
            approvalTotalCreditMeDPF = BigDecimalUtil.sum(approvalTotalCreditMeDPF, detail.getCreditMe(), 2);
        }

        approvalGlossDPF = buildApprovalGloss(account);
        approvalReady = true;
    }

    /** Glosa sugerida con el formato que se venia usando a mano. Queda editable. */
    private String buildApprovalGloss(Account account) {
        String partner = account.getPartner() != null ? account.getPartner().getFullName() : "";
        Integer days = account.getAccountType() != null ? account.getAccountType().getDays() : null;
        String symbol = account.getCurrency() != null
                ? MessageUtils.getMessage(account.getCurrency().getSymbolResourceKey()).toUpperCase()
                : "";
        return MessageUtils.getMessage("Account.approval.gloss", partner, account.getCode(),
                symbol, formatAmountForGloss(account.getCapital()), days != null ? days : 0);
    }

    /**
     * Importe para la glosa: separador de miles y decimales solo si los tiene, para que un
     * capital redondo se lea "20.000" y no "20.000,00". Los simbolos van fijos y no por
     * locale: la glosa es un texto contable, no depende de la configuracion del servidor.
     */
    private String formatAmountForGloss(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        boolean whole = amount.stripTrailingZeros().scale() <= 0;
        return new DecimalFormat(whole ? "#,##0" : "#,##0.00", symbols).format(amount);
    }

    /**
     * Registra la apertura: el comprobante de ingreso Y el paso del certificado a ACTIVO,
     * en una sola operacion. Desde aca empieza a devengar y sus datos quedan bloqueados.
     * <p/>
     * Se vuelve a armar el asiento en vez de confiar en lo que quedo del modal, para que
     * las validaciones corran igual aunque alguien fuerce el submit sin haber pasado por
     * la preparacion. Lo unico que se toma de la pantalla es la glosa, que es editable.
     */
    public String approveDpf() {
        String gloss = approvalGlossDPF;
        prepareApprovalDPF();
        if (!approvalReady) {
            return Outcome.REDISPLAY;
        }
        if (gloss != null && gloss.trim().length() > 0) {
            approvalGlossDPF = gloss;
        }

        Account account = getInstance();

        Voucher voucher = buildDpfVoucher(approvalDocumentTypeDPF.getName(),
                approvalGlossDPF, approvalDateDPF);
        voucher.getDetails().addAll(approvalDetailsDPF);

        account.setAccountState(AccountState.ACTIVE);
        accountService.updateAccount(account);
        voucherAccoutingService.saveVoucher(voucher);

        /** Se relee el certificado: quedo tocado por una transaccion de fuera de esta
         *  conversacion y hay que traerse la version nueva antes de seguir editandolo. */
        refreshInstance();
        originalAccountState = getInstance().getAccountState();
        originalAccountCode = getInstance().getCode();
        calculateTotalAmounts(getInstance());
        clearApprovalFields();

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Account.message.approved", account.getCode());
        return Outcome.REDISPLAY;
    }

    private void clearApprovalFields() {
        approvalReady = false;
        approvalDetailsDPF = new ArrayList<VoucherDetail>();
        approvalGlossDPF = null;
        approvalDocumentTypeDPF = null;
        approvalExchangeRateDPF = BigDecimal.ONE;
        approvalTotalDebitDPF = BigDecimal.ZERO;
        approvalTotalCreditDPF = BigDecimal.ZERO;
        approvalTotalDebitMeDPF = BigDecimal.ZERO;
        approvalTotalCreditMeDPF = BigDecimal.ZERO;
    }

    /**
     * Tramos de interes ganado del certificado durante su plazo. Uno por cada aumento de
     * capital; lo normal es uno solo.
     * <p/>
     * Los dias del ultimo tramo se calculan por diferencia contra el plazo contratado
     * (<code>tipocuenta.dias</code>), asi la suma de los tramos siempre da el plazo exacto
     * y no depende de como caigan las fechas.
     */
    private List<FixedTermDepositProvision> buildInterestSegments(Account account) {
        List<FixedTermDepositProvision> segments = new ArrayList<FixedTermDepositProvision>();

        AccountType accountType = account.getAccountType();
        BigDecimal rate = accountType.getInta();
        int totalDays = accountType.getDays() != null ? accountType.getDays() : 0;
        Date openingDate = DateUtils.removeTime(account.getOpeningDate());
        boolean foreign = FinancesCurrencyType.D.equals(account.getCurrency());

        List<Long> accountIds = new ArrayList<Long>();
        accountIds.add(account.getId());
        List<Object[]> movements = accountService.getAccountLedgerMovements(accountIds);

        /** Fechas de aumento: abonos posteriores a la apertura, mismo criterio que la
         *  provision mensual. */
        List<Date> increaseDates = new ArrayList<Date>();
        for (Object[] movement : movements) {
            Date movementDate = DateUtils.removeTime((Date) movement[1]);
            BigDecimal credit = (BigDecimal) (foreign ? movement[5] : movement[3]);
            if (movementDate != null && movementDate.after(openingDate)
                    && credit != null && credit.doubleValue() > 0
                    && !increaseDates.contains(movementDate)) {
                increaseDates.add(movementDate);
            }
        }
        Collections.sort(increaseDates);

        Date segmentFrom = openingDate;
        int consumedDays = 0;
        for (int i = 0; i <= increaseDates.size(); i++) {
            boolean last = i == increaseDates.size();
            int days = last
                    ? totalDays - consumedDays
                    : (int) DateUtils.daysBetween(segmentFrom, DateUtils.addDay(increaseDates.get(i), -1), true);
            if (days <= 0) {
                if (!last) {
                    segmentFrom = increaseDates.get(i);
                }
                continue;
            }

            BigDecimal capital = accumulatedCredits(movements, segmentFrom, foreign);

            FixedTermDepositProvision segment = new FixedTermDepositProvision();
            segment.setAccount(account);
            segment.setProvisionFromDate(segmentFrom);
            segment.setProvisionUntilDate(last ? DateUtils.addDay(segmentFrom, days - 1)
                    : DateUtils.addDay(increaseDates.get(i), -1));
            segment.setRate(rate);
            segment.setCapital(capital);
            segment.setDays(days);
            segment.setProvision(calculateInterestForDays(days, capital, rate));
            if (i > 0) {
                segment.setCapitalIncreaseDate(increaseDates.get(i - 1));
            }
            segments.add(segment);

            consumedDays += days;
            if (!last) {
                segmentFrom = increaseDates.get(i);
            }
        }
        return segments;
    }

    /** Capital vigente a una fecha: suma de los abonos hasta esa fecha inclusive. */
    private BigDecimal accumulatedCredits(List<Object[]> movements, Date date, boolean foreign) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] movement : movements) {
            Date movementDate = DateUtils.removeTime((Date) movement[1]);
            if (movementDate == null || movementDate.after(date)) {
                continue;
            }
            BigDecimal credit = (BigDecimal) (foreign ? movement[5] : movement[3]);
            if (credit != null) {
                total = total.add(credit);
            }
        }
        return total;
    }

    /**
     * El <code>ifOutcome</code> es necesario: sin el, una validacion fallida cerraria
     * igual la conversacion y la pantalla quedaria sin datos.
     */
    @End(beforeRedirect = true, ifOutcome = Outcome.SUCCESS)
    public String createDpfRenewal(){

        if (!validateRenewal()) {
            return Outcome.REDISPLAY;
        }

        /** El asiento se arma con la retencion que quedo marcada, no con lo que dejo el
         *  ultimo refresco por ajax. */
        recalculateRenewalRciva();

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            /** El tipo de cambio del dia del asiento, no el ultimo cargado: una
             *  renovacion registrada dias despues tiene que valuarse a su fecha. */
            exchangeRate = financesExchangeRateService
                    .findExchangeRateByDateByCurrency(DateUtils.removeTime(startDateDPF), FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
            return Outcome.REDISPLAY;
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
            return Outcome.REDISPLAY;
        }

        Account currentAccount = getInstance();

        /**
         * Caja del retiro parcial: sale de Preferencias de compania segun la moneda. Antes
         * estaban hardcodeadas 1110220000 (ME) y 1110110100 (MN), que son justamente las
         * dos que quedaron configuradas en cajagral1me / cajagral1mn.
         */
        CashAccount withdrawalCashAccount = null;
        if (Boolean.TRUE.equals(partialRenewal)) {
            try {
                CompanyConfiguration configuration = companyConfigurationService.findCompanyConfiguration();
                withdrawalCashAccount = FinancesCurrencyType.D.equals(currentAccount.getCurrency())
                        ? configuration.requireGeneralCashAccountForeign()
                        : configuration.requireGeneralCashAccountNational();
            } catch (CompanyConfigurationNotFoundException e) {
                addCompanyConfigurationNotFoundErrorMessage();
                return Outcome.REDISPLAY;
            } catch (CompanyAccountNotConfiguredException e) {
                facesMessages.add(StatusMessage.Severity.ERROR,
                        MessageUtils.getMessage("CompanyConfiguration.account.notConfigured",
                                MessageUtils.getMessage(e.getLabelKey())));
                return Outcome.REDISPLAY;
            }
        }

        /**
         * Codigo del certificado nuevo, de la misma secuencia que usa el alta. Se tipeaba a
         * mano y esta era la ultima via por la que un codigo podia repetirse.
         * <p/>
         * Se genera antes de tocar nada: si falla, la pantalla vuelve sin haber dejado el
         * certificado viejo marcado como inactivo a medio camino. La serie la elige la
         * moneda, que es la del certificado que se renueva.
         */
        Account newAccount = new Account();
        newAccount.setCurrency(currentAccount.getCurrency());
        newAccountCodeDPF = generateAccountCode(newAccount);
        if (newAccountCodeDPF == null) {
            return Outcome.REDISPLAY;
        }
        newAccount.setCode(newAccountCodeDPF);

        currentAccount.setAccountState(AccountState.INACTIVE);

        AccountType currentAccountType = currentAccount.getAccountType();
        //newAccount.setCapital(capitalRenewDPF);
        newAccount.setOpeningDate(startDateDPF);
        newAccount.setExpirationDate(expirationDateDPF);
        newAccount.setAccountNumber(generateAccountNumber());
        newAccount.setAccountType(accountTypeRenewDPF);
        newAccount.setPartner(partnerDPF);
        newAccount.setAccountState(AccountState.ACTIVE);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setRetentionFlag(Boolean.TRUE);
        newAccount.setCompanyAccountFlag(Boolean.FALSE);
        newAccount.setBeneficiary1(beneficiary1);
        newAccount.setBeneficiary2(beneficiary2);

        //accountService.createAccount(newAccount);

        BigDecimal withdrawal = BigDecimal.ZERO;

        /** La fecha es la de la renovacion, no el dia en que se opera: coincidian solo
         *  porque siempre se hacen el mismo dia. */
        Voucher voucher = buildDpfVoucher(documentType.getName(), glossRenewDPF,
                DateUtils.removeTime(startDateDPF));

        VoucherDetail vd1 = new VoucherDetail();
        VoucherDetail vd2 = new VoucherDetail();
        VoucherDetail vd3 = new VoucherDetail();
        VoucherDetail vd4 = new VoucherDetail();

        BigDecimal interestValue = BigDecimalUtil.subtract(interestDPF, rcivaDPF);
        BigDecimal diff = BigDecimalUtil.subtract(totalAmountDPF, capitalRenewDPF);
        interestValue   = BigDecimalUtil.subtract(interestValue, diff);

        if (partialRenewal) {
            withdrawal = BigDecimalUtil.subtract(capitalRenewDPF, partialCapitalRenewDPF);
            capitalRenewDPF = partialCapitalRenewDPF;
        }

        if (currentAccount.getCurrency().equals(FinancesCurrencyType.D)) {
            vd1 = buildAccountEntryDetail(currentAccount.getAccountType().getCashAccountMe().getAccountCode(), capitalDPF, "DEBIT", FinancesCurrencyType.D, Boolean.TRUE, exchangeRate);
            vd2 = buildAccountEntryDetail(currentAccountType.getCashAccountChargeMe().getAccountCode(), interestValue, "DEBIT", FinancesCurrencyType.D, Boolean.TRUE, exchangeRate);
            vd3 = buildAccountEntryDetail(accountTypeRenewDPF.getCashAccountMe().getAccountCode(), capitalRenewDPF, "CREDIT", FinancesCurrencyType.D, Boolean.TRUE, exchangeRate);
            if (partialRenewal)
                vd4 = buildAccountEntryDetail(withdrawalCashAccount.getAccountCode(), withdrawal, "CREDIT", FinancesCurrencyType.D, Boolean.TRUE, exchangeRate);

        }

        if (currentAccount.getCurrency().equals(FinancesCurrencyType.P)) {
            vd1 = buildAccountEntryDetail(currentAccount.getAccountType().getCashAccountMn().getAccountCode(), capitalDPF, "DEBIT", FinancesCurrencyType.P, Boolean.FALSE, exchangeRate);
            vd2 = buildAccountEntryDetail(currentAccountType.getCashAccountChargeMn().getAccountCode(), interestValue, "DEBIT", FinancesCurrencyType.P, Boolean.FALSE, exchangeRate);
            vd3 = buildAccountEntryDetail(accountTypeRenewDPF.getCashAccountMn().getAccountCode(), capitalRenewDPF, "CREDIT", FinancesCurrencyType.P, Boolean.FALSE, exchangeRate);
            if (partialRenewal)
                /** Cuenta en bolivianos: el retiro tambien va en MN. Estaba armandose
                 *  como moneda extranjera y convertia el importe por el tipo de cambio. */
                vd4 = buildAccountEntryDetail(withdrawalCashAccount.getAccountCode(), withdrawal, "CREDIT", FinancesCurrencyType.P, Boolean.FALSE, exchangeRate);
        }

        newAccount.setCapital(capitalRenewDPF);

        /**
         * Candado: el capital que se guarda en la ficha tiene que ser exactamente el que
         * se acredita en el asiento. Es lo que evita que la cuenta quede con un capital
         * que la contabilidad desmiente, como paso historicamente.
         */
        BigDecimal creditedCapital = FinancesCurrencyType.D.equals(currentAccount.getCurrency())
                ? vd3.getCreditMe() : vd3.getCredit();
        if (BigDecimalUtil.roundBigDecimal(capitalRenewDPF, 2)
                .compareTo(BigDecimalUtil.roundBigDecimal(creditedCapital, 2)) != 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.renewalCapitalMismatch", capitalRenewDPF, creditedCapital);
            return Outcome.REDISPLAY;
        }

        accountService.createAccount(newAccount);
        accountService.updateAccount(currentAccount);

        vd1.setPartnerAccount(currentAccount);
        vd3.setPartnerAccount(newAccount);

        voucher.getDetails().add(vd1);
        voucher.getDetails().add(vd2);
        voucher.getDetails().add(vd3);
        if (partialRenewal)
            voucher.getDetails().add(vd4);

        voucherAccoutingService.saveVoucher(voucher);


        return Outcome.SUCCESS;
    }

    /**
     * Controles previos a generar la renovacion. Se validan aca y no en la vista porque
     * son reglas del negocio, y porque el campo de capital parcial solo se renderiza
     * condicionado al check: su <code>required</code> no corre si el check llega apagado.
     */
    private boolean validateRenewal() {
        boolean valid = true;

        /** Ultima barrera: la pantalla ya no deja llegar aca -- Renovar esta deshabilitado
         *  --, pero renovar un certificado cerrado o antes de plazo son reglas del negocio
         *  y se validan antes de grabar, no solo en la vista. */
        if (!isRenewable(getInstance())) {
            facesMessages.add(StatusMessage.Severity.ERROR, getRenewalBlockedReason());
            valid = false;
        }

        if (documentType == null || documentType.getName() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.renewalDocumentTypeRequired");
            valid = false;
        }

        if (startDateDPF == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.renewalStartDateRequired");
            valid = false;
        }

        if (capitalRenewDPF == null || capitalRenewDPF.doubleValue() <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.renewalCapitalRequired");
            valid = false;
        }

        if (Boolean.TRUE.equals(partialRenewal)) {
            if (partialCapitalRenewDPF == null || partialCapitalRenewDPF.doubleValue() <= 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Account.error.partialCapitalRequired");
                valid = false;
            } else if (capitalRenewDPF != null
                    && partialCapitalRenewDPF.compareTo(capitalRenewDPF) > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Account.error.partialCapitalTooBig", partialCapitalRenewDPF, capitalRenewDPF);
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Cabecera del comprobante de una operacion de DPF: apertura, renovacion o cierre.
     * <p/>
     * Nace APROBADO. No es una carga manual que alguien tenga que revisar despues: lo arma
     * el sistema con datos ya validados, las cuentas salen de la configuracion y el asiento
     * cuadra por construccion. Dejarlo pendiente obligaba a entrar a Contabilidad a
     * aprobarlo a mano detras de cada operacion.
     * <p/>
     * Las tres operaciones pasan por aca para que el criterio sea uno solo y no se separe
     * con el tiempo.
     */
    private Voucher buildDpfVoucher(String documentType, String gloss, Date date) {
        Voucher voucher = new Voucher();
        voucher.setDocumentType(documentType);
        voucher.setGloss(gloss);
        voucher.setDate(date);
        voucher.setState(VoucherState.APR.toString());
        return voucher;
    }

    public String generateAccountNumber(){
        String result = String.valueOf(sequenceGeneratorService.nextValue(Constants.SAVINGS_ACCOUNT_NUMBER));
        return result;
    }

    /**
     * Codigo del certificado, tomado de <code>gensecuencia</code>: prefijo por moneda mas
     * cinco digitos, que es el formato de los ultimos codigos cargados (ME00303).
     * <p/>
     * Hay una secuencia por moneda porque las dos series son independientes: la de ME va
     * por 303 y la de MN por 2, y un solo contador haria que el proximo DPF en bolivianos
     * saliera MN00304, heredando el numero de la serie de dolares.
     * <p/>
     * El incremento es atomico y sale de
     * {@link AccountService#nextAccountCodeNumber(String)}: dos altas simultaneas se
     * serializan en el motor y cada una se lleva su numero.
     * <p/>
     * Si el codigo que entrega la secuencia ya existe se pide el siguiente, hasta diez
     * veces. Es lo que hace que una base con la secuencia atrasada -- el caso de una copia
     * de produccion restaurada sobre desarrollo -- se acomode sola en vez de trabar el
     * alta.
     *
     * @return el codigo, o <code>null</code> si falta la secuencia en la configuracion.
     */
    private String generateAccountCode(Account account) {
        boolean national = FinancesCurrencyType.P.equals(account.getCurrency());
        String prefix = national ? "MN" : "ME";
        String sequenceName = national ? Constants.ACCOUNT_DPF_CODE_MN : Constants.ACCOUNT_DPF_CODE_ME;

        String code = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            long number = accountService.nextAccountCodeNumber(sequenceName);
            if (number <= 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "Account.error.codeSequenceMissing", sequenceName);
                return null;
            }
            code = String.format("%s%05d", prefix, number);
            if (!accountService.existsAccountCode(code, null)) {
                return code;
            }
        }
        return code;
    }

    /**
     * El codigo identifica al certificado en toda la contabilidad y en los reportes. Hay 5
     * codigos repetidos en el historico, cada par apuntando a dos socios distintos, y de
     * ahi que cualquier consulta por codigo toque dos certificados. No se agregan mas.
     * <p/>
     * En una edicion solo se controla si el codigo cambio. Validarlo siempre dejaria esos
     * 10 certificados historicos imposibles de guardar: chocarian contra su propio gemelo
     * en cada intento, aunque nadie estuviera tocando el codigo.
     */
    private boolean validateAccountCode() {
        Account account = getInstance();
        String code = account.getCode();
        if (isManaged() && code != null && code.equals(originalAccountCode)) {
            return true;
        }
        if (accountService.existsAccountCode(code, account.getId())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.duplicatedCode", account.getCode());
            return false;
        }
        return true;
    }

    /**
     * El codigo solo se muestra, y solo cuando existe: lo genera el alta, asi que durante
     * el alta no hay nada que mostrar. Una cuenta de ahorro no tiene codigo, se identifica
     * por su numero de cuenta.
     */
    public boolean isAccountCodeVisible() {
        return isManaged() && isDpfAccount(getInstance());
    }

    /**
     * Anular es para el certificado que nunca fue una operacion: se cargo por error o se
     * duplico. Por eso exige que no tenga ningun asiento; si ya se contabilizo, lo que
     * corresponde es cerrarlo, no borrarlo del devengue historico.
     */
    public boolean isAnnullable() {
        Account account = getInstance();
        return isManaged()
                && account != null
                && !AccountState.ANNULLED.equals(account.getAccountState())
                && accountMovementCount <= 0;
    }

    /** Por que Anular esta deshabilitado. Cadena vacia si se puede anular. */
    public String getAnnulBlockedReason() {
        Account account = getInstance();
        if (account == null || isAnnullable()) {
            return "";
        }
        if (AccountState.ANNULLED.equals(account.getAccountState())) {
            return messages.get("Account.annul.blocked.alreadyAnnulled");
        }
        return messages.get("Account.annul.blocked.hasMovements");
    }

    /** Texto del modal de confirmacion, ya resuelto. */
    public String getAnnulQuestion() {
        Account account = getInstance();
        if (account == null) {
            return "";
        }
        String partner = account.getPartner() != null ? account.getPartner().getFullName() : "";
        return MessageFormat.format(messages.get("Account.annul.question"), account.getCode(), partner);
    }

    @Restrict("#{s:hasPermission('ACCOUNTANNUL','UPDATE')}")
    public String annulAccount() {
        if (!isAnnullable()) {
            facesMessages.add(StatusMessage.Severity.ERROR, getAnnulBlockedReason());
            return Outcome.REDISPLAY;
        }
        Account account = getInstance();
        account.setAccountState(AccountState.ANNULLED);
        accountService.updateAccount(account);
        refreshInstance();
        originalAccountState = getInstance().getAccountState();
        originalAccountCode = getInstance().getCode();
        calculateTotalAmounts(getInstance());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Account.message.annulled", account.getCode());
        return Outcome.REDISPLAY;
    }

    @Begin(nested = true, flushMode = FlushModeType.MANUAL)
    public String addPayment() {
        //setOp(OP_UPDATE);
        //return creditTransactionAction.addCreditTransaction();
        return Outcome.SUCCESS;
    }

    public List<VoucherDetail> getAccountDetailList(Account account){

        List<VoucherDetail> voucherDetails = accountService.getAccountDetailList(account);

        return voucherDetails;
    }

    public void calculateTotalAmounts(Account account){
        List<VoucherDetail> voucherDetails = accountService.getAccountDetailList(account);

        /**
         * Se parte de cero. La accion es de conversacion y este metodo se llama desde
         * select(): sin el reset, entrar dos veces a una cuenta dentro de la misma
         * conversacion duplicaba los totales, y de ahi sale el capital base de la
         * renovacion (renewalDPF usa getTotalBalance/getTotalBalanceMe).
         */
        setTotalCredit(BigDecimal.ZERO);
        setTotalDebit(BigDecimal.ZERO);
        setTotalCreditMe(BigDecimal.ZERO);
        setTotalDebitMe(BigDecimal.ZERO);
        accountMovementCount = voucherDetails.size();

        for (VoucherDetail voucherDetail : voucherDetails){
            setTotalCredit(BigDecimalUtil.sum(getTotalCredit(), voucherDetail.getCredit(), 2));
            setTotalDebit(BigDecimalUtil.sum(getTotalDebit(), voucherDetail.getDebit(), 2));

            setTotalCreditMe(BigDecimalUtil.sum(getTotalCreditMe(), voucherDetail.getCreditMe(), 2));
            setTotalDebitMe(BigDecimalUtil.sum(getTotalDebitMe(), voucherDetail.getDebitMe(), 2));
        }
        setTotalBalance(BigDecimalUtil.subtract(getTotalCredit(), getTotalDebit(), 2));
        setTotalBalanceMe(BigDecimalUtil.subtract(getTotalCreditMe(), getTotalDebitMe(), 2));
    }

    public BigDecimal getTotalBalanceAccount(Account account){

        BigDecimal result = BigDecimal.ZERO;

        BigDecimal balanceMN = BigDecimal.ZERO;
        BigDecimal balanceME = BigDecimal.ZERO;

        BigDecimal totalDebitMN  = BigDecimal.ZERO;
        BigDecimal totalCreditMN = BigDecimal.ZERO;

        BigDecimal totalDebitME  = BigDecimal.ZERO;
        BigDecimal totalCreditME = BigDecimal.ZERO;

        List<VoucherDetail> voucherDetails = accountService.getAccountDetailList(account);
        for (VoucherDetail voucherDetail : voucherDetails){
            totalCreditMN = BigDecimalUtil.sum(totalCreditMN, voucherDetail.getCredit());
            totalDebitMN  = BigDecimalUtil.sum(totalDebitMN, voucherDetail.getDebit());

            totalCreditME = BigDecimalUtil.sum(totalCreditME, voucherDetail.getCreditMe());
            totalDebitME  = BigDecimalUtil.sum(totalDebitME, voucherDetail.getDebitMe());
        }
        balanceMN = BigDecimalUtil.subtract(totalCreditMN, totalDebitMN);
        balanceME = BigDecimalUtil.subtract(totalCreditME, totalDebitME);

        if (account.getCurrency().equals(FinancesCurrencyType.P))
            result = balanceMN;
        if (account.getCurrency().equals(FinancesCurrencyType.D) || account.getCurrency().equals(FinancesCurrencyType.M))
            result = balanceME;

        return result;
    }


    /** Aportes **/
    public BigDecimal getTotalBalancePartner(Partner partner){
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal totalDebitMN  = BigDecimal.ZERO;
        BigDecimal totalCreditMN = BigDecimal.ZERO;

        List<VoucherDetail> voucherDetails = accountService.getPartnerDetailList(partner);
        for (VoucherDetail voucherDetail : voucherDetails){
            totalCreditMN = BigDecimalUtil.sum(totalCreditMN, voucherDetail.getCredit());
            totalDebitMN  = BigDecimalUtil.sum(totalDebitMN, voucherDetail.getDebit());
        }
        result = BigDecimalUtil.subtract(totalCreditMN, totalDebitMN);
        return result;
    }




    public String getFullAccountBalance(Account account){
        return account.getFullAccount() + " (" + getTotalBalanceAccount(account) + ")";
    }


    public void assignPartner(Partner partner){
        getInstance().setPartner(partner);
    }

    public void assignPartnerDPF(Partner partner){
        setPartnerDPF(partner);
    }

    public void clearPartner(){
        getInstance().setPartner(null);
    }

    public void clearPartnerDPF(){
        setPartnerDPF(null);
    }

    /**
     * Al tildar o destildar "Renovacion parcial" se limpia el capital parcial. Sin esto el
     * valor quedaba vivo en la conversacion: al destildar no se borraba, y al volver a
     * tildar reaparecia el importe tipeado antes.
     */
    public void togglePartialRenewal() {
        setPartialCapitalRenewDPF(null);
    }

    public boolean isForeignAccount(){

        boolean result = false;
        if (getInstance().getCurrency() != null){
            if (getInstance().getCurrency().equals(FinancesCurrencyType.D) || getInstance().getCurrency().equals(FinancesCurrencyType.M))
                result = true;
        }

        return result;
    }

    /**
     * Se consulta en cada evaluacion de un <code>rendered</code> o un <code>disabled</code>
     * de la ficha, o sea muchas veces por request: nada de trazas por consola aca.
     */
    public Boolean isDpfAccount(Account account){
        return account != null
                && account.getAccountType() != null
                && SavingType.DPF.equals(account.getAccountType().getSavingType());
    }


    public void capitaliationOfInterests(){
        if (this.savingType.equals(SavingType.SOC)){
            capitaliationOfInterests(SavingType.SOC);
        }

        if (this.savingType.equals(SavingType.CAJ)){
            capitaliationOfInterests(SavingType.CAJ);
        }

        /*if (this.savingType.equals(SavingType.DPF)){

        }*/
    }

    public Boolean hasCredit(Partner partner){
        Boolean result = Boolean.FALSE;
        List<Credit> creditList = creditService.getCreditList(partner);

        if (creditList.size() > 0) result = Boolean.TRUE;

        return result;
    }

    public void capitaliationOfInterests(SavingType savingType){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        Voucher voucher = new Voucher();
        voucher.setDocumentType("CD");
        voucher.setDate(this.endDate);
        voucher.setGloss("CAPITALIZACION DE INTERESES SOBRE " + MessageUtils.getMessage(savingType.getResourceKey()).toUpperCase() + " AL " + DateUtils.format(endDate, "dd/MM/yyyy"));

        List<Account> accountList = accountService.getSavingsAccounts(savingType);
        // for (Account account :accountList) System.out.println("-----> CUENTA AHORRO: " + account.getFullAccountName());

        /** Para calcular el saldo a inicio del periodo a capitalizar, resta un dia a fecha inicio **/
        /** todo: corregir segundos en fecha inicio toma los segundos del sistema **/
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        //Date start = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate));
        Date start = DateUtils.getDate(Constants.YEAR, Constants.MONTH, Constants.DAY);
        Date end   = calendar.getTime();
        System.out.println("=====> FECHAS: " + start + " - " + end);

        //accountList = removeZeroBalance(accountList, start, end); // revisar

        /*for (Account account : accountList){
            BigDecimal balance = accountService.calculateAccountBalance(account, start, end);
            System.out.println("=====> SALDO CUENTA: " + account.getPartner().getFullName() + " : " + balance);
        }*/

        for (Account account : accountList){
            BigDecimal interest = BigDecimal.ZERO;
            BigDecimal ivaTax = BigDecimal.ZERO;

            BigDecimal accountBalance = accountService.calculateAccountBalance(account, start, end);
            if (accountBalance.compareTo(BigDecimal.ZERO) == 0) continue;

            List<VoucherDetail> accountMovements = accountService.getMovementAccountBetweenDates(account, startDate, endDate);
            List<AccountKardex> kardexList       = calculateAccountKardex(accountMovements, account.getCurrency(), startDate, accountBalance);

            /** change for conditions: with credit, without credit **/
            BigDecimal percentage = account.getAccountType().getInta(); /** Without credit **/
            if (hasCredit(account.getPartner()))
                percentage = account.getAccountType().getIntb(); /** With credit **/

            /** Para 1 transaccion en el periodo **/
            if (kardexList.size() == 1){
                AccountKardex previous = kardexList.get(0);
                previous.setInterest(calculateInterest(previous.getDate(), endDate, previous.balance, percentage));
                interest = BigDecimalUtil.sum(interest, previous.getInterest(), 6);
                ivaTax = BigDecimalUtil.multiply(interest, Constants.VAT, 6);
                //System.out.println(DateUtils.format(this.endDate, "dd/MM/yyyy") + " ===> D:" + previous.getDebit() + " H:" + previous.getCredit() + " Int: " + interest);
            }

            /** Para 2 o mas transacciones en el periodo **/
            for (int i=1 ; i < kardexList.size() ; i++){
                AccountKardex previous = kardexList.get(i-1);
                AccountKardex current = kardexList.get(i);
                current.setInterest(calculateInterest(previous.getDate(), current.getDate(), previous.balance, percentage));
                interest = BigDecimalUtil.sum(interest, current.getInterest(), 6);
                //System.out.println(DateUtils.format(current.getDate(), "dd/MM/yyyy") + " ++-> D:" + current.getDebit() + " H:" + current.getCredit() + " Int: " + current.getInterest());

                /** Si ultima transaccion es menor al 31/mm/aaaaa **/
                if (i == kardexList.size()-1){
                    if (kardexList.get(i).getDate().compareTo(this.endDate) < 0){
                        BigDecimal endInterest = calculateInterest(kardexList.get(i).getDate(), this.endDate, current.getBalance(), percentage);
                        interest = BigDecimalUtil.sum(interest, endInterest, 6);
                        //System.out.println(DateUtils.format(this.endDate, "dd/MM/yyyy") + " ---> D:" + current.getDebit() + " H:" + current.getCredit() + " Int: " + endInterest);
                    }
                }
            }

            ivaTax = BigDecimalUtil.multiply(interest, Constants.VAT, 6);

            String cashAccountCode = "";
            if (account.getCurrency().equals(FinancesCurrencyType.P)){
                cashAccountCode = account.getAccountType().getCashAccountMn().getAccountCode();
            }
            if (account.getCurrency().equals(FinancesCurrencyType.D)) {
                cashAccountCode = account.getAccountType().getCashAccountMe().getAccountCode();
            }
            if (account.getCurrency().equals(FinancesCurrencyType.M)) {
                cashAccountCode = account.getAccountType().getCashAccountMv().getAccountCode();
            }

            /** Adicionar cuentas contables **/
            if (BigDecimalUtil.roundBigDecimal(interest, 2).doubleValue() > 0){ // Para no crear con valores 0.00

                if (!account.getRetentionFlag() && account.getCompanyAccountFlag()) interest = BigDecimalUtil.subtract(interest, ivaTax, 6);

                VoucherDetail detailInterest = buildAccountEntryDetail(cashAccountCode, interest, "CREDIT", account.getCurrency(), Boolean.TRUE, exchangeRate);
                detailInterest.setPartnerAccount(account);
                voucher.getDetails().add(detailInterest);
            }

            if (BigDecimalUtil.roundBigDecimal(ivaTax, 2).doubleValue()>0){ // Para no crear con valores 0.00
                if (account.getRetentionFlag()) {
                    VoucherDetail detailIvaTax = buildAccountEntryDetail(cashAccountCode, ivaTax, "DEBIT", account.getCurrency(), Boolean.TRUE, exchangeRate);
                    detailIvaTax.setPartnerAccount(account);
                    voucher.getDetails().add(detailIvaTax);
                }
            }
            /** End **/

        }

        /** TOTAL INTEREST **/
        BigDecimal totalInterest_AHO_SOC_MN     = calculateTotalInterestSum(voucher.getDetails(), Constants.CTA_AHO_SOC_MN_2120110200, Boolean.TRUE);
        BigDecimal totalInterest_DEP_CAJ_AHO_MN = calculateTotalInterestSum(voucher.getDetails(), Constants.DEP_CAJ_AHO_MN_2120110100, Boolean.TRUE);

        BigDecimal totalInterest_AHO_SOC_MV     = calculateTotalInterestSum(voucher.getDetails(), Constants.CTA_AHO_SOC_MV_2120130200, Boolean.TRUE);
        BigDecimal totalInterest_DEP_CAJ_AHO_ME = calculateTotalInterestSum(voucher.getDetails(), Constants.DEP_CAJ_AHO_ME_2120120100, Boolean.TRUE);

        System.out.println("totalInterest_AHO_SOC_MN: " + totalInterest_AHO_SOC_MN);
        System.out.println("totalInterest_DEP_CAJ_AHO_MN: " + totalInterest_DEP_CAJ_AHO_MN);
        System.out.println("totalInterest_AHO_SOC_MV: " + totalInterest_AHO_SOC_MV);
        System.out.println("totalInterest_DEP_CAJ_AHO_ME: " + totalInterest_DEP_CAJ_AHO_ME);

        VoucherDetail detailTotalInterest_AHO_SOC_MN     = buildAccountEntryDetail(Constants.ACOUNT_INTEREST_4110210200_AHO_SOC_MN, totalInterest_AHO_SOC_MN, "DEBIT", FinancesCurrencyType.P, Boolean.TRUE, exchangeRate);
        VoucherDetail detailTotalInterest_DEP_CAJ_AHO_MN = buildAccountEntryDetail(Constants.ACOUNT_INTEREST_4110210100_DEP_CAJ_AHO_MN, totalInterest_DEP_CAJ_AHO_MN, "DEBIT", FinancesCurrencyType.P, Boolean.TRUE, exchangeRate);

        VoucherDetail detailTotalInterest_AHO_SOC_MV     = buildAccountEntryDetail(Constants.ACOUNT_INTEREST_4110230200_AHO_SOC_MV, totalInterest_AHO_SOC_MV, "DEBIT", FinancesCurrencyType.M, Boolean.FALSE, exchangeRate);
        VoucherDetail detailTotalInterest_DEP_CAJ_AHO_ME = buildAccountEntryDetail(Constants.ACOUNT_INTEREST_4110220100_DEP_CAJ_AHO_ME, totalInterest_DEP_CAJ_AHO_ME, "DEBIT", FinancesCurrencyType.D, Boolean.FALSE, exchangeRate);

        /** FOR RETENTION **/
        BigDecimal totalIvaTax_AHO_SOC_MN = calculateTotalInterestSum(voucher.getDetails(),     Constants.CTA_AHO_SOC_MN_2120110200, Boolean.FALSE);
        BigDecimal totalIvaTax_DEP_CAJ_AHO_MN = calculateTotalInterestSum(voucher.getDetails(), Constants.DEP_CAJ_AHO_MN_2120110100, Boolean.FALSE);
        BigDecimal totalIvaTax_AHO_SOC_MV = calculateTotalInterestSum(voucher.getDetails(),     Constants.CTA_AHO_SOC_MV_2120130200, Boolean.FALSE);
        BigDecimal totalIvaTax_DEP_CAJ_AHO_ME = calculateTotalInterestSum(voucher.getDetails(), Constants.DEP_CAJ_AHO_ME_2120120100, Boolean.FALSE);

        System.out.println("totalIvaTax_AHO_SOC_MN: " + totalIvaTax_AHO_SOC_MN);
        System.out.println("totalIvaTax_DEP_CAJ_AHO_MN: " + totalIvaTax_DEP_CAJ_AHO_MN);
        System.out.println("totalIvaTax_AHO_SOC_MV: " + totalIvaTax_AHO_SOC_MV);
        System.out.println("totalIvaTax_DEP_CAJ_AHO_ME: " + totalIvaTax_DEP_CAJ_AHO_ME);

        VoucherDetail detailTotalIvaTax_AHO_SOC_MN   = buildAccountEntryDetail(Constants.ACOUNT_RCIVA_2420310100, totalIvaTax_AHO_SOC_MN,    "CREDIT", FinancesCurrencyType.P, Boolean.TRUE, exchangeRate);
        VoucherDetail detailTotalIvaTax_DEP_CAJ_AHO_MN   = buildAccountEntryDetail(Constants.ACOUNT_RCIVA_2420310100, totalIvaTax_DEP_CAJ_AHO_MN,"CREDIT", FinancesCurrencyType.P, Boolean.TRUE, exchangeRate);
        VoucherDetail detailTotalIvaTax_AHO_SOC_MV   = buildAccountEntryDetail(Constants.ACOUNT_RCIVA_2420310100, totalIvaTax_AHO_SOC_MV,    "CREDIT", FinancesCurrencyType.P, Boolean.TRUE, exchangeRate);
        VoucherDetail detailTotalIvaTax_DEP_CAJ_AHO_ME   = buildAccountEntryDetail(Constants.ACOUNT_RCIVA_2420310100, totalIvaTax_DEP_CAJ_AHO_ME,"CREDIT", FinancesCurrencyType.P, Boolean.TRUE, exchangeRate);


        if (detailTotalInterest_AHO_SOC_MN.getDebit().doubleValue()>0) voucher.getDetails().add(detailTotalInterest_AHO_SOC_MN);
        if (detailTotalInterest_DEP_CAJ_AHO_MN.getDebit().doubleValue()>0) voucher.getDetails().add(detailTotalInterest_DEP_CAJ_AHO_MN);
        if (detailTotalInterest_AHO_SOC_MV.getDebit().doubleValue()>0) voucher.getDetails().add(detailTotalInterest_AHO_SOC_MV);
        if (detailTotalInterest_DEP_CAJ_AHO_ME.getDebit().doubleValue()>0) voucher.getDetails().add(detailTotalInterest_DEP_CAJ_AHO_ME);

        if (detailTotalIvaTax_AHO_SOC_MN.getCredit().doubleValue()>0) voucher.getDetails().add(detailTotalIvaTax_AHO_SOC_MN);
        if (detailTotalIvaTax_DEP_CAJ_AHO_MN.getCredit().doubleValue()>0) voucher.getDetails().add(detailTotalIvaTax_DEP_CAJ_AHO_MN);
        if (detailTotalIvaTax_AHO_SOC_MV.getCredit().doubleValue()>0) voucher.getDetails().add(detailTotalIvaTax_AHO_SOC_MV);
        if (detailTotalIvaTax_DEP_CAJ_AHO_ME.getCredit().doubleValue()>0) voucher.getDetails().add(detailTotalIvaTax_DEP_CAJ_AHO_ME);

        voucherAccoutingService.saveVoucher(voucher);

    }

    private List<Account> removeZeroBalance(List<Account> accountList, Date start, Date end){

        List<Account> result = new ArrayList<Account>();
        for (Account account: accountList){
            BigDecimal balance = accountService.calculateAccountBalance(account, start, end);
            if (balance.doubleValue() > 0)
                result.add(account);
        }
        return result;
    }

    public BigDecimal calculateTotalInterestSum(List<VoucherDetail> voucherDetailList, String account, Boolean interest){
        BigDecimal result = BigDecimal.ZERO;

        for (VoucherDetail detail : voucherDetailList){
            if (detail.getAccount().equals(account))
                if (interest)
                    result = BigDecimalUtil.sum(result, detail.getCredit(), 2);
                else
                    result = BigDecimalUtil.sum(result, detail.getDebit(), 2);
        }

        return result;
    }

    public VoucherDetail buildAccountEntryDetail(String cashAccountCode, BigDecimal amount, String flag, FinancesCurrencyType currencyType, Boolean change, BigDecimal exchangeRate){

        /*BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }*/

        VoucherDetail detail = new VoucherDetail();

        if (currencyType.equals(FinancesCurrencyType.P)){
            if (flag.equals("DEBIT")){
                detail.setDebit(amount);
                detail.setCredit(BigDecimal.ZERO);
                detail.setAccount(cashAccountCode);

                detail.setExchangeAmount(BigDecimal.ONE);
                detail.setDebitMe(BigDecimal.ZERO);
                detail.setCreditMe(BigDecimal.ZERO);
            }
            if (flag.equals("CREDIT")){
                detail.setDebit(BigDecimal.ZERO);
                detail.setCredit(amount);
                detail.setAccount(cashAccountCode);

                detail.setExchangeAmount(BigDecimal.ONE);
                detail.setDebitMe(BigDecimal.ZERO);
                detail.setCreditMe(BigDecimal.ZERO);
            }

        }

        if (currencyType.equals(FinancesCurrencyType.D) || currencyType.equals(FinancesCurrencyType.M)){
            if (change) {
                if (flag.equals("DEBIT")) {
                    detail.setDebitMe(BigDecimalUtil.roundBigDecimal(amount, 2));
                    detail.setCreditMe(BigDecimal.ZERO);
                    detail.setAccount(cashAccountCode);

                    detail.setExchangeAmount(exchangeRate);
                    detail.setDebit(BigDecimalUtil.roundBigDecimal(BigDecimalUtil.multiply(amount, exchangeRate, 6), 2));
                    detail.setCredit(BigDecimal.ZERO);
                }
                if (flag.equals("CREDIT")) {
                    detail.setDebitMe(BigDecimal.ZERO);
                    detail.setCreditMe(BigDecimalUtil.roundBigDecimal(amount, 2));
                    detail.setAccount(cashAccountCode);

                    detail.setExchangeAmount(exchangeRate);
                    detail.setDebit(BigDecimal.ZERO);
                    //detail.setCredit(BigDecimalUtil.multiply(detail.getCreditMe(), exchangeRate, 2));
                    detail.setCredit(BigDecimalUtil.roundBigDecimal(BigDecimalUtil.multiply(amount, exchangeRate, 6), 2));
                }
            }

            if (!change) {
                if (flag.equals("DEBIT")) {
                    detail.setAccount(cashAccountCode);
                    detail.setExchangeAmount(exchangeRate);

                    detail.setDebit(amount);
                    detail.setCredit(BigDecimal.ZERO);

                    detail.setDebitMe(BigDecimalUtil.divide(amount, exchangeRate, 2));
                    detail.setCreditMe(BigDecimal.ZERO);
                }
                if (flag.equals("CREDIT")) {
                    detail.setAccount(cashAccountCode);
                    detail.setExchangeAmount(exchangeRate);

                    detail.setDebit(BigDecimal.ZERO);
                    detail.setCredit(amount);

                    detail.setDebitMe(BigDecimal.ZERO);
                    detail.setCreditMe(BigDecimalUtil.divide(amount, exchangeRate, 2));
                }
            }

        }

        return detail;
    }

    public List<AccountKardex> calculateAccountKardex(List<VoucherDetail> accountMovements, FinancesCurrencyType currencyType, Date start, BigDecimal accountBalance){

        List<AccountKardex> dataList = new ArrayList<AccountKardex>();
        BigDecimal balance = BigDecimal.ZERO;
        /** For M.N. **/

        balance = accountBalance;
        AccountKardex initAccountKardex = new AccountKardex(start, BigDecimal.ZERO, accountBalance, balance);
        dataList.add(initAccountKardex);

        if (currencyType.equals(FinancesCurrencyType.P)){
            //System.out.println("------- KARDEX MN -------");
            for (VoucherDetail detail : accountMovements){
                balance = BigDecimalUtil.subtract(balance, detail.getDebit(), 6);
                balance = BigDecimalUtil.sum(balance, detail.getCredit(), 6);

                AccountKardex data = new AccountKardex(detail.getVoucher().getDate(), detail.getDebit(),detail.getCredit(), balance);
                dataList.add(data);
                //System.out.println("---> " + data.getDate() + " - " + data.getDebit() + " - " + data.getCredit() + " - " + balance);
            }
        }

        /** For M.E. **/
        if (currencyType.equals(FinancesCurrencyType.D) || currencyType.equals(FinancesCurrencyType.M)){
            //System.out.println("------- KARDEX MEV -------");
            for (VoucherDetail detail : accountMovements){
                balance = BigDecimalUtil.subtract(balance, detail.getDebitMe(), 6);
                balance = BigDecimalUtil.sum(balance, detail.getCreditMe(), 6);

                AccountKardex data = new AccountKardex(detail.getVoucher().getDate(), detail.getDebitMe(), detail.getCreditMe(), balance);
                dataList.add(data);
                //System.out.println("---> " + data.getDate() + " - " + data.getDebit() + " - " + data.getCredit() + " - " + balance);
            }
        }
        /*System.out.println("--------------------ACCOUNT KARDEX----------------------");
        for (AccountKardex data : dataList){
            System.out.println("---> " + data.getDate() + " - " + data.getDebit() + " - " + data.getCredit() + " - " + data.getBalance());
        }*/

        return dataList;
    }

    public BigDecimal calculateInterest(Date previousDate, Date currentDate, BigDecimal balance, BigDecimal percentage){

        BigDecimal saldoCapital = balance;

        Date currentPaymentDate = currentDate;
        currentPaymentDate      = DateUtils.removeTime(currentPaymentDate);
        Date lastPaymentDate    = previousDate; // previous

        Long days = DateUtils.daysBetween(lastPaymentDate, currentPaymentDate)-1;
        if (previousDate.equals(startDate))
            days = days+1;

        //System.out.println("--> D:" + days);

        BigDecimal var_interest = BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 6);
        BigDecimal var_time = BigDecimalUtil.divide(BigDecimalUtil.toBigDecimal(days.toString()), BigDecimalUtil.toBigDecimal(360), 6);
        BigDecimal interest = BigDecimalUtil.multiply(saldoCapital, var_interest, 6);
        interest = BigDecimalUtil.multiply(interest, var_time, 6);


        return interest;
    }

    public BigDecimal calculateInterestForDays(Integer daysValue, BigDecimal balance, BigDecimal percentage){

        BigDecimal saldoCapital = balance;
        Long days = daysValue.longValue();
        System.out.println("--> SALDO:" + balance);
        System.out.println("--> D:" + days);

        BigDecimal var_interest = BigDecimalUtil.divide(percentage, BigDecimalUtil.toBigDecimal(100), 6);
        BigDecimal var_time = BigDecimalUtil.divide(BigDecimalUtil.toBigDecimal(days.toString()), BigDecimalUtil.toBigDecimal(360), 6);
        BigDecimal interest = BigDecimalUtil.multiply(saldoCapital, var_interest, 6);
        interest = BigDecimalUtil.multiply(interest, var_time, 6);

        return interest;
    }


    public void calculateInterestForNewDPF(){
        BigDecimal interestValue = calculateInterestForDays(accountTypeRenewDPF.getDays(), getCapitalRenewDPF(), accountTypeRenewDPF.getInta());
        setInterestRenewDPF(interestValue);
    }

    /**
     * Corre al elegir el tipo de cuenta. El vencimiento sale del plazo del tipo, y un DPF
     * que se esta dando de alta muestra desde ya el estado con el que va a nacer.
     * <p/>
     * Tolera que no haya tipo elegido: el combo tiene opcion en blanco y volver a ella
     * dejaba el certificado sin tipo de cuenta y esta llamada reventaba.
     */
    public void calculateExpirationDate(){
        Account account = getInstance();
        if (!isDpfAccount(account)) {
            return;
        }
        account.setExpirationDate(DateUtils.addDay(account.getOpeningDate(), account.getAccountType().getDays()));
        if (!isManaged()) {
            account.setAccountState(AccountState.PENDING);
        }
    }

    public void calculateExpirationDateDPF(){
        setExpirationDateDPF(DateUtils.addDay(startDateDPF, accountTypeRenewDPF.getDays()));
    }


    public boolean isActive(Account account){
        if (account != null)
            return account.getAccountState().equals(AccountState.ACTIVE);

        return false;
    }

    /**
     * Solo se puede anular una cuenta que nunca tuvo movimiento contable. Si tiene
     * asientos es un certificado real: corresponde INACTIVE, no ANNULLED.
     * <p/>
     * Se queda en la pantalla en vez de terminar la conversacion. La regla de navegacion
     * que habia abria una conversacion nueva y redirigia aca: el action volvia vacio y la
     * ficha se veia como si fuera un alta, con el vencimiento y el capital en blanco.
     */
    @Override
    public String update() {
        if (!validateStateTransition() || !validateAccountCode()) {
            return Outcome.REDISPLAY;
        }
        if (AccountState.ANNULLED.equals(getInstance().getAccountState())
                && !accountService.getAccountDetailList(getInstance()).isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.cannotAnnulWithMovements", getInstance().getCode());
            return Outcome.REDISPLAY;
        }
        super.update();
        originalAccountState = getInstance().getAccountState();
        originalAccountCode = getInstance().getCode();
        calculateTotalAmounts(getInstance());
        return Outcome.REDISPLAY;
    }

    /**
     * El estado de un DPF es consecuencia de una operacion, no un campo libre del
     * formulario. De PENDIENTE a ACTIVO se pasa por Aprobar, que es lo unico que genera el
     * comprobante de ingreso; hacerlo a mano dejaria un certificado activo devengando
     * sobre un capital que nunca entro. Y volver a PENDIENTE uno ya aprobado permitiria
     * aprobarlo de nuevo y contabilizar la apertura dos veces.
     * <p/>
     * Anular un pendiente si se permite: es exactamente el deposito que nunca ocurrio.
     */
    private boolean validateStateTransition() {
        Account account = getInstance();
        AccountState state = account.getAccountState();
        String errorKey = null;

        if (!AccountState.PENDING.equals(originalAccountState)
                && AccountState.PENDING.equals(state)) {
            /** Vale para toda cuenta: PENDIENTE no se elige a mano, lo pone el alta de un
             *  DPF y lo saca la aprobacion. En una cuenta de ahorro no significa nada. */
            errorKey = "Account.error.stateChangeToPending";
        } else if (isDpfAccount(account)
                && AccountState.PENDING.equals(originalAccountState)
                && !AccountState.PENDING.equals(state)
                && !AccountState.ANNULLED.equals(state)) {
            errorKey = "Account.error.stateChangeNeedsApproval";
        }

        if (errorKey == null) {
            return true;
        }
        /** Se devuelve el estado real a la ficha: si no, la pantalla sigue mostrando el
         *  valor rechazado como si hubiera quedado guardado. */
        account.setAccountState(originalAccountState);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, errorKey);
        return false;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTotalCreditMe() {
        return totalCreditMe;
    }

    public void setTotalCreditMe(BigDecimal totalCreditMe) {
        this.totalCreditMe = totalCreditMe;
    }

    public BigDecimal getTotalDebitMe() {
        return totalDebitMe;
    }

    public void setTotalDebitMe(BigDecimal totalDebitMe) {
        this.totalDebitMe = totalDebitMe;
    }

    public BigDecimal getTotalBalanceMe() {
        return totalBalanceMe;
    }

    public void setTotalBalanceMe(BigDecimal totalBalanceMe) {
        this.totalBalanceMe = totalBalanceMe;
    }

    /** For capitalize interest **/
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

    public SavingType getSavingType() {
        return savingType;
    }

    public void setSavingType(SavingType savingType) {
        this.savingType = savingType;
    }

    public BigDecimal getCapitalDPF() {
        return capitalDPF;
    }

    public void setCapitalDPF(BigDecimal capitalDPF) {
        this.capitalDPF = capitalDPF;
    }

    public BigDecimal getInterestDPF() {
        return interestDPF;
    }

    public void setInterestDPF(BigDecimal interestDPF) {
        this.interestDPF = interestDPF;
    }

    public BigDecimal getCapitalRenewDPF() {
        return capitalRenewDPF;
    }

    public void setCapitalRenewDPF(BigDecimal capitalRenewDPF) {
        this.capitalRenewDPF = capitalRenewDPF;
    }

    public AccountType getAccountTypeRenewDPF() {
        return accountTypeRenewDPF;
    }

    public void setAccountTypeRenewDPF(AccountType accountTypeRenewDPF) {
        this.accountTypeRenewDPF = accountTypeRenewDPF;
    }

    public Date getExpirationDateDPF() {
        return expirationDateDPF;
    }

    public void setExpirationDateDPF(Date expirationDateDPF) {
        this.expirationDateDPF = expirationDateDPF;
    }

    public Date getStartDateDPF() {
        return startDateDPF;
    }

    public void setStartDateDPF(Date startDateDPF) {
        this.startDateDPF = startDateDPF;
    }

    public BigDecimal getInterestRenewDPF() {
        return interestRenewDPF;
    }

    public void setInterestRenewDPF(BigDecimal interestRenewDPF) {
        this.interestRenewDPF = interestRenewDPF;
    }

    public DocType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocType documentType) {
        this.documentType = documentType;
    }

    public String getGlossRenewDPF() {
        return glossRenewDPF;
    }

    public void setGlossRenewDPF(String glossRenewDPF) {
        this.glossRenewDPF = glossRenewDPF;
    }

    public String getNewAccountCodeDPF() {
        return newAccountCodeDPF;
    }

    public void setNewAccountCodeDPF(String newAccountCodeDPF) {
        this.newAccountCodeDPF = newAccountCodeDPF;
    }

    public Partner getPartnerDPF() {
        return partnerDPF;
    }

    public void setPartnerDPF(Partner partnerDPF) {
        this.partnerDPF = partnerDPF;
    }

    public Voucher getSelectedVoucher() {
        return selectedVoucher;
    }

    public List<VoucherDetail> getSelectedVoucherDetails() {
        return selectedVoucherDetails;
    }

    public BigDecimal getSelectedVoucherTotalDebit() {
        return selectedVoucherTotalDebit;
    }

    public BigDecimal getSelectedVoucherTotalCredit() {
        return selectedVoucherTotalCredit;
    }

    public BigDecimal getSelectedVoucherTotalDebitMe() {
        return selectedVoucherTotalDebitMe;
    }

    public BigDecimal getSelectedVoucherTotalCreditMe() {
        return selectedVoucherTotalCreditMe;
    }

    public Date getClosingDateDPF() {
        return closingDateDPF;
    }

    public void setClosingDateDPF(Date closingDateDPF) {
        this.closingDateDPF = closingDateDPF;
    }

    public BigDecimal getClosingCapitalDPF() {
        return closingCapitalDPF;
    }

    public BigDecimal getClosingInterestDPF() {
        return closingInterestDPF;
    }

    public BigDecimal getClosingRcivaDPF() {
        return closingRcivaDPF;
    }

    public BigDecimal getClosingNetInterestDPF() {
        return closingNetInterestDPF;
    }

    public BigDecimal getClosingTotalDPF() {
        return closingTotalDPF;
    }

    public BigDecimal getClosingAccruedDPF() {
        return closingAccruedDPF;
    }

    public BigDecimal getClosingExpenseDPF() {
        return closingExpenseDPF;
    }

    /** Para la vista: el ajuste al gasto se muestra en la columna que corresponde. */
    public boolean isClosingExpenseReversal() {
        return closingExpenseDPF != null && closingExpenseDPF.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getClosingExpenseAbsolute() {
        return closingExpenseDPF == null ? BigDecimal.ZERO : closingExpenseDPF.abs();
    }

    public List<FixedTermDepositProvision> getClosingAccruedDetail() {
        return closingAccruedDetail;
    }

    public Boolean getRetentionRenewDPF() {
        return retentionRenewDPF;
    }

    public void setRetentionRenewDPF(Boolean retentionRenewDPF) {
        this.retentionRenewDPF = retentionRenewDPF;
    }

    public Boolean getRetentionCloseDPF() {
        return retentionCloseDPF;
    }

    public void setRetentionCloseDPF(Boolean retentionCloseDPF) {
        this.retentionCloseDPF = retentionCloseDPF;
    }

    public String getGlossCloseDPF() {
        return glossCloseDPF;
    }

    public void setGlossCloseDPF(String glossCloseDPF) {
        this.glossCloseDPF = glossCloseDPF;
    }

    public Date getApprovalDateDPF() {
        return approvalDateDPF;
    }

    public String getApprovalGlossDPF() {
        return approvalGlossDPF;
    }

    public void setApprovalGlossDPF(String approvalGlossDPF) {
        this.approvalGlossDPF = approvalGlossDPF;
    }

    public BigDecimal getApprovalExchangeRateDPF() {
        return approvalExchangeRateDPF;
    }

    public DocType getApprovalDocumentTypeDPF() {
        return approvalDocumentTypeDPF;
    }

    public List<VoucherDetail> getApprovalDetailsDPF() {
        return approvalDetailsDPF;
    }

    public BigDecimal getApprovalTotalDebitDPF() {
        return approvalTotalDebitDPF;
    }

    public BigDecimal getApprovalTotalCreditDPF() {
        return approvalTotalCreditDPF;
    }

    public BigDecimal getApprovalTotalDebitMeDPF() {
        return approvalTotalDebitMeDPF;
    }

    public BigDecimal getApprovalTotalCreditMeDPF() {
        return approvalTotalCreditMeDPF;
    }

    public boolean isApprovalReady() {
        return approvalReady;
    }

    public BigDecimal getRcivaDPF() {
        return rcivaDPF;
    }

    public void setRcivaDPF(BigDecimal rcivaDPF) {
        this.rcivaDPF = rcivaDPF;
    }

    public BigDecimal getTotalAmountDPF() {
        return totalAmountDPF;
    }

    public void setTotalAmountDPF(BigDecimal totalAmountDPF) {
        this.totalAmountDPF = totalAmountDPF;
    }

    public String getBeneficiary1() {
        return beneficiary1;
    }

    public void setBeneficiary1(String beneficiary1) {
        this.beneficiary1 = beneficiary1;
    }

    public String getBeneficiary2() {
        return beneficiary2;
    }

    public void setBeneficiary2(String beneficiary2) {
        this.beneficiary2 = beneficiary2;
    }

    public List<FixedTermDepositProvision> getInterestSegmentsDPF() {
        return interestSegmentsDPF;
    }

    public void setInterestSegmentsDPF(List<FixedTermDepositProvision> interestSegmentsDPF) {
        this.interestSegmentsDPF = interestSegmentsDPF;
    }

    /** Solo hay que mostrar el desglose cuando hubo un aumento de capital. */
    public boolean isInterestSegmented() {
        return interestSegmentsDPF != null && interestSegmentsDPF.size() > 1;
    }

    public Boolean getPartialRenewal() {
        return partialRenewal;
    }

    public void setPartialRenewal(Boolean partialRenewal) {
        this.partialRenewal = partialRenewal;
    }

    public BigDecimal getPartialCapitalRenewDPF() {
        return partialCapitalRenewDPF;
    }

    public void setPartialCapitalRenewDPF(BigDecimal partialCapitalRenewDPF) {
        this.partialCapitalRenewDPF = partialCapitalRenewDPF;
    }

    private class AccountKardex{

        private Date date;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal balance;
        private BigDecimal interest;

        AccountKardex(Date date, BigDecimal debit, BigDecimal credit, BigDecimal balance){
            this.setDate(date);
            this.setDebit(debit);
            this.setCredit(credit);
            this.setBalance(balance);
            this.setInterest(BigDecimal.ZERO);
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public BigDecimal getDebit() {
            return debit;
        }

        public void setDebit(BigDecimal debit) {
            this.debit = debit;
        }

        public BigDecimal getCredit() {
            return credit;
        }

        public void setCredit(BigDecimal credit) {
            this.credit = credit;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public BigDecimal getInterest() {
            return interest;
        }

        public void setInterest(BigDecimal interest) {
            this.interest = interest;
        }
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
    }

}
