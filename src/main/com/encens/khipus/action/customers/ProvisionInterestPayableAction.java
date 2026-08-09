package com.encens.khipus.action.customers;

import com.encens.khipus.exception.finances.CompanyAccountNotConfiguredException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.AccountType;
import com.encens.khipus.model.customers.FixedTermDepositProvision;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.AccountService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.FixedTermDepositAccrual;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import javax.faces.model.SelectItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provision mensual de intereses por pagar sobre Depositos a Plazo Fijo.
 * <p/>
 * Replica el calculo que hasta ahora se hacia en la planilla "ESTADO DE DEPOSITOS A
 * PLAZO FIJO POR PAGAR". Para cada DPF vigente en algun tramo del mes:
 * <pre>
 *   desde     = max(inicio de mes, fecha de apertura)
 *   hasta     = min(fin de mes,    fecha de vencimiento)
 *   dias      = hasta - desde + 1        (inclusivo en AMBOS extremos)
 *   provision = capital * tasa% * dias / 36000
 * </pre>
 * Los dias se cuentan inclusive en los dos extremos, igual que la planilla: cuando un
 * DPF se renueva, el dia del vencimiento se cuenta tanto en el certificado viejo como
 * en el nuevo. Se mantiene asi a proposito para que los importes cuadren con el
 * historico ya contabilizado.
 * <p/>
 * Las cuentas se seleccionan por SOLAPAMIENTO DE FECHAS, sin mirar el estado: al
 * renovar un DPF la cuenta anterior queda INACTIVE, y sus dias devengados hasta el
 * vencimiento igual forman parte de la provision del mes.
 * <p/>
 * Orden del redondeo (importa, y esta verificado contra los asientos historicos):
 * primero se redondea el total en $us a 2 decimales y recien despues se multiplica
 * por el tipo de cambio. Redondear al final da diferencias de centavos.
 *
 * @author
 */
@Name("provisionInterestPayableAction")
@Scope(ScopeType.CONVERSATION)
public class ProvisionInterestPayableAction extends GenericAction {

    private Month month;
    private Integer year;

    private List<FixedTermDepositProvision> detailList = new ArrayList<FixedTermDepositProvision>();
    private List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();

    private BigDecimal exchangeRate;
    /** Total en $us ya redondeado a 2 decimales: es la base del importe en Bs. */
    private BigDecimal totalForeignCurrency = BigDecimal.ZERO;
    private BigDecimal totalForeignCurrencyNational = BigDecimal.ZERO;
    private BigDecimal totalNationalCurrency = BigDecimal.ZERO;

    private boolean calculated = false;

    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private AccountService accountService;
    @In
    private FinancesExchangeRateService financesExchangeRateService;
    @In
    private CompanyConfigurationService companyConfigurationService;

    public ProvisionInterestPayableAction() {
        /** Por defecto el ultimo mes cerrado, que es el que normalmente se provisiona. */
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        this.month = Month.values()[calendar.get(Calendar.MONTH)];
        this.year = calendar.get(Calendar.YEAR);
    }

    /**
     * Arma el detalle del mes y la vista previa del asiento. No persiste nada: es el
     * paso que se usa tanto para generar como para verificar meses ya contabilizados.
     */
    @Restrict("#{s:hasPermission('PROVISIONDPF','VIEW')}")
    public void calculate() {
        clearResults();

        if (month == null || year == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProvisionInterestPayable.error.periodRequired");
            return;
        }

        Date startDate = getPeriodStartDate();
        Date endDate = getPeriodEndDate();

        CompanyConfiguration companyConfiguration;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            addCompanyConfigurationNotFoundErrorMessage();
            return;
        }

        List<Account> accountList = accountService.getSavingsAccountsByPeriod(SavingType.DPF, startDate, endDate);
        if (accountList.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ProvisionInterestPayable.warn.noAccounts", getPeriodLabel());
            return;
        }

        /**
         * Se traen todos los movimientos, sin recortar por el fin del periodo: el control
         * contra cuenta.capital necesita el total del certificado, y los tramos filtran
         * por fecha cada uno por su cuenta.
         */
        Map<Long, List<Object[]>> movementsByAccount = loadMovements(accountList);

        if (!validateAgainstLedger(accountList, movementsByAccount)) {
            clearResults();
            return;
        }

        List<FixedTermDepositProvision> details = new ArrayList<FixedTermDepositProvision>();
        for (Account account : accountList) {
            List<FixedTermDepositProvision> accountDetails =
                    buildDetails(account, startDate, endDate, movementsByAccount.get(account.getId()));
            if (accountDetails == null) {
                /** buildDetails ya reporto el motivo; se corta para no generar a medias. */
                clearResults();
                return;
            }
            details.addAll(accountDetails);
        }

        warnAboutCapitalIncreases(details);

        if (details.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "ProvisionInterestPayable.warn.noAccounts", getPeriodLabel());
            return;
        }

        /** El tipo de cambio solo se exige si efectivamente hay DPF en moneda extranjera. */
        if (hasForeignCurrency(details) && !loadExchangeRate(endDate)) {
            clearResults();
            return;
        }

        try {
            this.voucherDetails = buildVoucherDetails(details, companyConfiguration);
        } catch (CompanyAccountNotConfiguredException e) {
            facesMessages.add(StatusMessage.Severity.ERROR,
                    MessageUtils.getMessage("CompanyConfiguration.account.notConfigured",
                            MessageUtils.getMessage(e.getLabelKey())));
            clearResults();
            return;
        }

        this.detailList = details;
        this.calculated = true;
    }

    /**
     * Registra el asiento de provision. Recalcula antes de persistir para no grabar
     * una vista previa que quedo vieja en la conversacion.
     */
    @Restrict("#{s:hasPermission('PROVISIONDPF','CREATE')}")
    public void generateProvisionInterest() {
        calculate();
        if (!calculated) {
            return;
        }

        Voucher voucher = new Voucher();
        voucher.setDocumentType(Constants.CB_VOUCHER_DOCTYPE);
        voucher.setDate(getPeriodEndDate());
        voucher.setGloss(getGloss());

        for (VoucherDetail voucherDetail : voucherDetails) {
            voucher.getDetails().add(voucherDetail);
        }

        voucherAccoutingService.saveVoucher(voucher);

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "ProvisionInterestPayable.message.generated",
                getPeriodLabel(), voucher.getDocumentNumber());

        clearResults();
    }

    /* ------------------------------------------------------------------ */
    /* Calculo                                                             */
    /* ------------------------------------------------------------------ */

    /**
     * Trae los movimientos del mayor de todo el lote en una sola consulta, agrupados por
     * cuenta. Una consulta por certificado seria inaceptable en la provision mensual.
     */
    private Map<Long, List<Object[]>> loadMovements(List<Account> accountList) {
        List<Long> accountIds = new ArrayList<Long>();
        for (Account account : accountList) {
            accountIds.add(account.getId());
        }

        Map<Long, List<Object[]>> movementsByAccount = new LinkedHashMap<Long, List<Object[]>>();
        for (Object[] movement : accountService.getAccountLedgerMovements(accountIds)) {
            Long accountId = (Long) movement[0];
            List<Object[]> movements = movementsByAccount.get(accountId);
            if (movements == null) {
                movements = new ArrayList<Object[]>();
                movementsByAccount.put(accountId, movements);
            }
            movements.add(movement);
        }
        return movementsByAccount;
    }

    /**
     * Candado: <code>cuenta.capital</code> tiene que coincidir con el capital que dice la
     * contabilidad del certificado, o sea con la suma de los importes acreditados.
     * <p/>
     * Se compara contra el TOTAL acreditado y no contra el saldo a una fecha: un capital
     * que cambio a mitad del plazo es legitimo y lo resuelve el calculo por tramos. Lo que
     * este candado atrapa es el dato genuinamente mal cargado, que es otra cosa.
     * <p/>
     * Se reportan TODOS los certificados con problema, no solo el primero, para que la
     * correccion se haga de una sola pasada.
     *
     * @return <code>false</code> si hay alguna inconsistencia.
     */
    private boolean validateAgainstLedger(List<Account> accountList,
                                          Map<Long, List<Object[]>> movementsByAccount) {
        boolean valid = true;
        for (Account account : accountList) {
            List<Object[]> movements = movementsByAccount.get(account.getId());
            if (movements == null || movements.isEmpty()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "ProvisionInterestPayable.error.noLedgerMovements", account.getCode());
                valid = false;
                continue;
            }

            /** Un certificado retirado anticipadamente no devenga en este periodo, asi que
             *  su capital no entra en ningun calculo: no tiene sentido bloquear la
             *  generacion por el. */
            Date closingDate = earlyClosingDate(account, movements);
            if (closingDate != null && !closingDate.after(getPeriodEndDate())) {
                continue;
            }

            BigDecimal credits = BigDecimalUtil.roundBigDecimal(
                    capitalAt(movements, null, account.getCurrency()), 2);
            BigDecimal capital = BigDecimalUtil.roundBigDecimal(
                    account.getCapital() != null ? account.getCapital() : BigDecimal.ZERO, 2);
            if (capital.compareTo(credits) != 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "ProvisionInterestPayable.error.capitalMismatch",
                        account.getCode(), capital, credits);
                valid = false;
            }
        }
        return valid;
    }

    /**
     * El calculo vive en {@link FixedTermDepositAccrual} y no aca porque el cierre de un
     * DPF tiene que devengar EXACTAMENTE lo mismo que esta provision: una debita el pasivo
     * y la otra lo acredita, y cualquier diferencia queda colgada para siempre.
     */
    private BigDecimal capitalAt(List<Object[]> movements, Date date, FinancesCurrencyType currency) {
        return FixedTermDepositAccrual.capitalAt(movements, date, isForeign(currency));
    }

    private List<Date> getCapitalIncreaseDates(List<Object[]> movements, Date from, Date until,
                                               FinancesCurrencyType currency) {
        return FixedTermDepositAccrual.capitalIncreaseDates(movements, from, until, isForeign(currency));
    }

    /**
     * Solo el cierre ANTICIPADO corta el devengue. Una renovacion tambien deja el saldo en
     * cero y ese certificado igual devengo su plazo completo.
     */
    private Date earlyClosingDate(Account account, List<Object[]> movements) {
        return FixedTermDepositAccrual.earlyClosingDate(movements, isForeign(account.getCurrency()),
                account.getExpirationDate());
    }

    private static boolean isForeign(FinancesCurrencyType currency) {
        return FinancesCurrencyType.D.equals(currency);
    }

    /**
     * Avisa arriba de la grilla que tal certificado recibio un aumento de capital en tal
     * fecha. Es un caso excepcional -- un DPF normalmente no recibe dinero a mitad de
     * plazo -- y el contador tiene que verlo, no descubrirlo cuadrando el importe.
     */
    private void warnAboutCapitalIncreases(List<FixedTermDepositProvision> details) {
        for (FixedTermDepositProvision detail : details) {
            if (detail.isFromCapitalIncrease()) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "ProvisionInterestPayable.warn.capitalIncrease",
                        detail.getCode(),
                        DateUtils.format(detail.getCapitalIncreaseDate(), "dd/MM/yyyy"),
                        detail.getCapital());
            }
        }
    }

    /**
     * Arma los tramos devengados del mes para un certificado.
     * <p/>
     * Lo normal es un solo tramo. Si el certificado recibio un aumento de capital dentro
     * del mes, se parte en tantos tramos como haga falta: el interes se devenga sobre el
     * capital vigente cada dia, y un solo importe para todo el mes seria incorrecto.
     *
     * @return los tramos, o <code>null</code> si la cuenta no se puede procesar (moneda no
     *         soportada, cuenta contable sin configurar o tasa sin definir).
     */
    private List<FixedTermDepositProvision> buildDetails(Account account, Date startDate, Date endDate,
                                                         List<Object[]> movements) {
        FinancesCurrencyType currency = account.getCurrency();
        if (!FinancesCurrencyType.P.equals(currency) && !FinancesCurrencyType.D.equals(currency)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProvisionInterestPayable.error.unsupportedCurrency",
                    account.getCode(), currency != null ? currency.name() : "");
            return null;
        }

        AccountType accountType = account.getAccountType();
        CashAccount liabilityAccount = FinancesCurrencyType.D.equals(currency)
                ? accountType.getCashAccountChargeMe()
                : accountType.getCashAccountChargeMn();
        if (liabilityAccount == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProvisionInterestPayable.error.liabilityAccountMissing",
                    accountType.getName(),
                    FinancesCurrencyType.D.equals(currency) ? "CTACF_ME" : "CTACF_MN");
            return null;
        }

        BigDecimal rate = accountType.getInta();
        if (rate == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProvisionInterestPayable.error.rateMissing", accountType.getName());
            return null;
        }

        List<FixedTermDepositProvision> result = new ArrayList<FixedTermDepositProvision>();

        /**
         * Un certificado retirado ANTES de su vencimiento no devenga mas, aunque su
         * fechavence sea posterior: el dinero ya se devolvio. Y no devenga nada en el mes en
         * que se retira, porque el asiento de cierre liquida ese tramo -- si igual se
         * provisionara, quedaria un pasivo posterior a la liquidacion que nadie reversa.
         * <p/>
         * OJO que es el cierre ANTICIPADO. Una renovacion tambien deja el saldo en cero
         * (el capital pasa al certificado nuevo) y ese SI devenga hasta su fechavence:
         * ademas la renovacion debita del pasivo el interes del plazo completo, asi que
         * saltear su ultimo mes dejaria un faltante permanente.
         * <p/>
         * El corte es por FECHA, nunca por "esta cerrado hoy": la provision de agosto se
         * genera en septiembre con fecha 31/08, y un retiro del 2 de septiembre no tiene
         * que tocar agosto.
         */
        Date closingDate = earlyClosingDate(account, movements);
        if (closingDate != null && !closingDate.after(endDate)) {
            return result;
        }

        Date from = maxDate(startDate, DateUtils.removeTime(account.getOpeningDate()));
        Date until = minDate(endDate, DateUtils.removeTime(account.getExpirationDate()));

        if (until.before(from)) {
            return result;
        }

        List<Date> increaseDates = getCapitalIncreaseDates(movements, from, until, currency);

        Date segmentFrom = from;
        for (int i = 0; i <= increaseDates.size(); i++) {
            /** El dia del aumento ya devenga con el capital nuevo: el tramo anterior
             *  cierra el dia previo. */
            Date segmentUntil = i < increaseDates.size()
                    ? DateUtils.addDay(increaseDates.get(i), -1)
                    : until;

            int days = segmentUntil.before(segmentFrom)
                    ? 0 : (int) DateUtils.daysBetween(segmentFrom, segmentUntil, true);
            if (days > 0) {
                BigDecimal capital = capitalAt(movements, segmentFrom, currency);

                FixedTermDepositProvision detail = new FixedTermDepositProvision();
                detail.setAccount(account);
                detail.setProvisionFromDate(segmentFrom);
                detail.setProvisionUntilDate(segmentUntil);
                detail.setRate(rate);
                detail.setLiabilityAccountCode(liabilityAccount.getAccountCode());
                detail.setCapital(capital);
                detail.setDays(days);
                detail.setProvision(FixedTermDepositAccrual.accrual(capital, rate, days));
                if (i > 0) {
                    detail.setCapitalIncreaseDate(increaseDates.get(i - 1));
                }
                result.add(detail);
            }

            if (i < increaseDates.size()) {
                segmentFrom = increaseDates.get(i);
            }
        }
        return result;
    }

    /**
     * Arma las lineas del comprobante: una de gasto por moneda y una de pasivo por cada
     * cuenta de cargos financieros distinta. Agrupar por cuenta (y no solo por moneda)
     * evita mezclar tipos de DPF que apunten a cuentas contables diferentes.
     */
    private List<VoucherDetail> buildVoucherDetails(List<FixedTermDepositProvision> details,
                                                    CompanyConfiguration companyConfiguration) {
        Map<String, BigDecimal> foreignByAccount = new LinkedHashMap<String, BigDecimal>();
        Map<String, BigDecimal> nationalByAccount = new LinkedHashMap<String, BigDecimal>();
        Map<String, CashAccount> liabilityAccounts = new LinkedHashMap<String, CashAccount>();

        for (FixedTermDepositProvision detail : details) {
            AccountType accountType = detail.getAccount().getAccountType();
            boolean foreign = FinancesCurrencyType.D.equals(detail.getCurrency());
            CashAccount liabilityAccount = foreign
                    ? accountType.getCashAccountChargeMe()
                    : accountType.getCashAccountChargeMn();
            String code = detail.getLiabilityAccountCode();
            liabilityAccounts.put(code, liabilityAccount);

            Map<String, BigDecimal> target = foreign ? foreignByAccount : nationalByAccount;
            BigDecimal accumulated = target.get(code);
            target.put(code, accumulated == null
                    ? detail.getProvision()
                    : accumulated.add(detail.getProvision()));
        }

        List<VoucherDetail> foreignLines = new ArrayList<VoucherDetail>();
        BigDecimal foreignTotal = BigDecimal.ZERO;
        BigDecimal foreignTotalNational = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : foreignByAccount.entrySet()) {
            /** Se redondea el $us y recien despues se convierte: asi lo hace la planilla. */
            BigDecimal amountForeign = BigDecimalUtil.roundBigDecimal(entry.getValue(), 2);
            BigDecimal amountNational = BigDecimalUtil.roundBigDecimal(amountForeign.multiply(exchangeRate), 2);
            if (amountForeign.doubleValue() <= 0 && amountNational.doubleValue() <= 0) {
                continue;
            }
            foreignTotal = foreignTotal.add(amountForeign);
            foreignTotalNational = foreignTotalNational.add(amountNational);
            foreignLines.add(buildLine(liabilityAccounts.get(entry.getKey()), entry.getKey(),
                    amountNational, amountForeign, false, FinancesCurrencyType.D));
        }

        List<VoucherDetail> nationalLines = new ArrayList<VoucherDetail>();
        BigDecimal nationalTotal = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : nationalByAccount.entrySet()) {
            BigDecimal amountNational = BigDecimalUtil.roundBigDecimal(entry.getValue(), 2);
            if (amountNational.doubleValue() <= 0) {
                continue;
            }
            nationalTotal = nationalTotal.add(amountNational);
            nationalLines.add(buildLine(liabilityAccounts.get(entry.getKey()), entry.getKey(),
                    amountNational, BigDecimal.ZERO, false, FinancesCurrencyType.P));
        }

        this.totalForeignCurrency = foreignTotal;
        this.totalForeignCurrencyNational = foreignTotalNational;
        this.totalNationalCurrency = nationalTotal;

        List<VoucherDetail> result = new ArrayList<VoucherDetail>();
        if (!foreignLines.isEmpty()) {
            CashAccount expenseAccount = companyConfiguration.requireFixedTermPayableInterestForeignCurrency();
            result.add(buildLine(expenseAccount, expenseAccount.getAccountCode(),
                    foreignTotalNational, foreignTotal, true, FinancesCurrencyType.D));
            result.addAll(foreignLines);
        }
        if (!nationalLines.isEmpty()) {
            CashAccount expenseAccount = companyConfiguration.requireFixedTermPayableInterestNationalCurrency();
            result.add(buildLine(expenseAccount, expenseAccount.getAccountCode(),
                    nationalTotal, BigDecimal.ZERO, true, FinancesCurrencyType.P));
            result.addAll(nationalLines);
        }
        return result;
    }

    /**
     * Construye una linea del comprobante. Las lineas en ME llevan el importe en Bs y su
     * equivalente en $us mas el tipo de cambio; las de MN llevan tipo de cambio 1 y los
     * importes en $us en cero, que es como las graba el resto del sistema.
     */
    private VoucherDetail buildLine(CashAccount cashAccount, String accountCode,
                                    BigDecimal amountNational, BigDecimal amountForeign,
                                    boolean debit, FinancesCurrencyType currency) {
        VoucherDetail detail = new VoucherDetail();
        detail.setAccount(accountCode);
        /** La asociacion es de solo lectura; se setea para que la vista previa muestre el nombre. */
        detail.setCashAccount(cashAccount);
        detail.setCurrency(currency);
        detail.setExchangeAmount(FinancesCurrencyType.D.equals(currency) ? exchangeRate : BigDecimal.ONE);

        if (debit) {
            detail.setDebit(amountNational);
            detail.setCredit(BigDecimal.ZERO);
            detail.setDebitMe(amountForeign);
            detail.setCreditMe(BigDecimal.ZERO);
        } else {
            detail.setDebit(BigDecimal.ZERO);
            detail.setCredit(amountNational);
            detail.setDebitMe(BigDecimal.ZERO);
            detail.setCreditMe(amountForeign);
        }
        return detail;
    }

    /**
     * @return <code>false</code> si no hay tipo de cambio cargado para el cierre del mes.
     *         Sin el no se puede ni verificar ni generar, asi que el flujo se corta.
     */
    private boolean loadExchangeRate(Date endDate) {
        try {
            BigDecimal rate = financesExchangeRateService
                    .findExchangeRateByDateByCurrency(endDate, FinancesCurrencyType.D.toString());
            if (rate == null || rate.doubleValue() <= 0) {
                addExchangeRateNotFoundMessage(endDate);
                return false;
            }
            this.exchangeRate = rate;
            return true;
        } catch (FinancesExchangeRateNotFoundException e) {
            addExchangeRateNotFoundMessage(endDate);
            return false;
        } catch (FinancesCurrencyNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProvisionInterestPayable.error.currencyNotFound", FinancesCurrencyType.D.toString());
            return false;
        }
    }

    private void addExchangeRateNotFoundMessage(Date endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "ProvisionInterestPayable.error.exchangeRateNotFound",
                DateUtils.format(endDate, "dd/MM/yyyy"));
    }

    private boolean hasForeignCurrency(List<FixedTermDepositProvision> details) {
        for (FixedTermDepositProvision detail : details) {
            if (FinancesCurrencyType.D.equals(detail.getCurrency())) {
                return true;
            }
        }
        return false;
    }

    private void clearResults() {
        this.detailList = new ArrayList<FixedTermDepositProvision>();
        this.voucherDetails = new ArrayList<VoucherDetail>();
        this.exchangeRate = null;
        this.totalForeignCurrency = BigDecimal.ZERO;
        this.totalForeignCurrencyNational = BigDecimal.ZERO;
        this.totalNationalCurrency = BigDecimal.ZERO;
        this.calculated = false;
    }

    /** Invalida el resultado al cambiar el periodo, para no mostrar totales de otro mes. */
    public void resetResults() {
        clearResults();
    }

    /* ------------------------------------------------------------------ */
    /* Periodo                                                             */
    /* ------------------------------------------------------------------ */

    public Date getPeriodStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month.getValue(), 1, 0, 0, 0);
        return calendar.getTime();
    }

    public Date getPeriodEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month.getValue(), 1, 0, 0, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public String getPeriodLabel() {
        return DateUtils.format(getPeriodEndDate(), "dd/MM/yyyy");
    }

    public String getGloss() {
        return MessageUtils.getMessage("ProvisionInterestPayable.gloss", getPeriodLabel());
    }

    private static Date maxDate(Date one, Date other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.after(other) ? one : other;
    }

    private static Date minDate(Date one, Date other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.before(other) ? one : other;
    }

    /* ------------------------------------------------------------------ */
    /* Accesores                                                           */
    /* ------------------------------------------------------------------ */

    public Month[] getMonthEnum() {
        return Month.values();
    }

    public List<SelectItem> getYearSelectItems() {
        List<SelectItem> years = new ArrayList<SelectItem>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear + 1; y >= currentYear - 6; y--) {
            years.add(new SelectItem(y, String.valueOf(y)));
        }
        return years;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<FixedTermDepositProvision> getDetailList() {
        return detailList;
    }

    public List<VoucherDetail> getVoucherDetails() {
        return voucherDetails;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getTotalForeignCurrency() {
        return totalForeignCurrency;
    }

    public BigDecimal getTotalForeignCurrencyNational() {
        return totalForeignCurrencyNational;
    }

    public BigDecimal getTotalNationalCurrency() {
        return totalNationalCurrency;
    }

    public boolean isCalculated() {
        return calculated;
    }
}
