package com.encens.khipus.action.accounting;

import com.encens.khipus.action.accounting.reports.VoucherReportAction;
import com.encens.khipus.action.purchases.PurchaseDocumentAction;
import com.encens.khipus.action.warehouse.reports.ValuedPhysicalInventoryReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.customers.FixedTermDepositConflict;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.transaction.Transaction;
import org.hibernate.StaleObjectStateException;

import javax.ejb.EJBException;
import javax.persistence.OptimisticLockException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author
 * @version 3.0
 */
@Name("voucherCreateAction")
@Scope(ScopeType.CONVERSATION)
public class VoucherCreateAction extends GenericAction<Voucher> {

    CashAccount cashAccount = null;
    public static String APPROVED_OUTCOME = "Approved";
    public static String ANNUL_OUTCOME = "Annul";

    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal credit = BigDecimal.ZERO;
    private BigDecimal debitMe = BigDecimal.ZERO;
    private BigDecimal creditMe = BigDecimal.ZERO;

    private String documentTypeCode = "";

    private DocType docType = new DocType();
    private Voucher voucher = new Voucher();

    private Provider provider;
    private CashAccount account;
    private Client client;
    private ProductItem productItem;
    private Account partnerAccount;
    /** Texto de la cuenta de ahorro ya resuelto: la vista NO debe tocar la asociacion lazy. */
    private String partnerAccountLabel;
    private Partner partner;

    //private PurchaseDocument purchaseDocument;
    private List<PurchaseDocument> purchaseDocumentList = new ArrayList<PurchaseDocument>();

    /** Factura que se esta cargando o editando en el modal. Vive solo en memoria hasta
        que se Guarda/Actualiza el asiento; nada se escribe en BD antes de eso. */
    private PurchaseDocument invoiceInEdit;
    /** true: se edita una factura ya cargada; false: alta de una factura nueva. */
    private boolean invoiceEditing = false;
    /** Cuenta de Credito Fiscal con la que se genera la linea contable de la factura. */
    private CashAccount fiscalCreditAccount;
    /** Resultado de la ultima operacion del modal, para cerrarlo solo si fue exitosa. */
    private boolean invoiceOperationSucceeded = false;
    /** Factura seleccionada para eliminar (se confirma en un modal antes de quitarla). */
    private PurchaseDocument invoiceToRemove;

    /** Bajas diferidas al editar un asiento ya guardado: lo persistido que se quita en
        pantalla NO se borra de la BD hasta pulsar Actualizar (todo en una transaccion). */
    private List<VoucherDetail> voucherDetailsToRemove = new ArrayList<VoucherDetail>();
    private List<PurchaseDocument> purchaseDocumentsToRemove = new ArrayList<PurchaseDocument>();

    /** Fila (linea contable) sobre la que se esta eligiendo proveedor. Si es null, la
        seleccion aplica al proveedor del ENCABEZADO (comportamiento historico). */
    private VoucherDetail voucherDetailForProvider;

    private Integer quantity;
    private BigDecimal amountDeposit;
    private BigDecimal contribution;

    private List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();
    private List<CashAccount> cashAccounts = new ArrayList<CashAccount>();

    private BigDecimal totalDebit = new BigDecimal("0.00");
    private BigDecimal totalCredit = new BigDecimal("0.00");

    private String clientFullName;
    private String providerFullName;

    private Boolean fiscalCredit = false;
    private Boolean currencyCondition = false;

    /** For closing results **/
    private Date startDate;
    private Date endDate;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private WarehouseService warehouseService;

    @In
    private VoucherService voucherService;

    @In(create = true)
    private VoucherUpdateAction voucherUpdateAction;

    @In(create = true)
    private VoucherDetailAction voucherDetailAction;

    @In(create = true, value = "valuedPhysicalInventoryReportAction")
    private ValuedPhysicalInventoryReportAction physicalInventoryReportAction;

    @In
    private ClientService clientService;

    @In
    private CashAccountService cashAccountService;

    @In
    private PurchaseDocumentService purchaseDocumentService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In(create = true)
    private PurchaseDocumentAction purchaseDocumentAction;
    @In(create = true)
    private VoucherReportAction voucherReportAction;

    @Factory(value = "voucherCreate")
    public Voucher initVoucher() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(Voucher instance) {
        String outCome = super.select(instance);
        voucher = getInstance();
        this.docType = voucherService.getDocType(voucher.getDocumentType());
        setVoucherDetails(voucherAccoutingService.getVoucherDetailList(voucher));
        setPurchaseDocumentList(voucherAccoutingService.getPurchaseDcumentList(voucher));
        /** Se abre una edicion limpia: sin bajas pendientes de una sesion anterior. */
        voucherDetailsToRemove.clear();
        purchaseDocumentsToRemove.clear();
        return outCome;
    }

    public void initView(Voucher instance) {
        System.out.println("===> " + instance.getFullDocument());
        setVoucher(instance);
        System.out.println("gloss: " + voucher.getGloss());
        setVoucherDetails(voucherAccoutingService.getVoucherDetailList(voucher));
        setPurchaseDocumentList(voucherAccoutingService.getPurchaseDcumentList(voucher));
    }

    @Override
    @End
    public String create() {

        voucher.setDocumentType(docType.getName());
        voucher.setDetails(voucherDetails);

        //voucher.setPurchaseDocumentList(purchaseDocumentList);

        /** Se valida antes de escribir nada en la base de datos **/
        if (!validateVoucherBalance()) {
            return Outcome.REDISPLAY;
        }

        if (!validateDuplicatedInvoices()) {
            return Outcome.REDISPLAY;
        }

        if (!validateFixedTermDepositCapital()) {
            return Outcome.REDISPLAY;
        }

        warnUnlinkedFixedTermDepositAccounts();
        warnIfFiscalCreditDoesNotMatch();

        try {

            /**
             * Guardado atomico: el asiento, sus detalles y las facturas de credito fiscal
             * se persisten dentro de una unica transaccion. Cada factura se escribe junto
             * a su linea de credito fiscal (misma unidad de trabajo), asi nunca queda una
             * factura sin asiento ni un asiento referenciando una factura inexistente.
             */
            voucherAccoutingService.saveVoucher(voucher);
            setInstance(voucher);

            setOp(OP_UPDATE);
            return Outcome.SUCCESS;

        } catch (Exception e) {
            /**
             * Se revierte todo lo escrito para no dejar facturas sin asiento y se devuelve
             * REDISPLAY (null) para NO cerrar la conversacion: el usuario conserva lo cargado.
             */
            rollbackAndDiscardGeneratedIds(e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.saveError");
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Valida que el asiento tenga cuentas y que el debe iguale al haber.
     * Se usa tanto al crear como al actualizar y aprobar.
     */
    private boolean validateVoucherBalance() {

        if (voucherDetails == null || voucherDetails.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.emptyDetails");
            return false;
        }

        /**
         * Se compara a 2 decimales (precision de la moneda, igual que lo mostrado en
         * pantalla). Asi ruido de sub-centimos no genera un falso descuadre cuando el
         * debe y el haber ya son iguales en lo visible.
         */
        BigDecimal totalDebit = getTotalsDebit().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalCredit = getTotalsCredit().setScale(2, RoundingMode.HALF_UP);

        if (totalDebit.compareTo(totalCredit) != 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.incorrectAccountingEntry");
            return false;
        }

        return true;
    }

    /**
     * Bloquea facturas repetidas. Una factura se considera la misma cuando coinciden
     * NIT, numero y fecha. Se controla contra las facturas ya registradas en la base
     * de datos (excepto anuladas) y tambien dentro del asiento que se esta cargando.
     */
    /**
     * Un DPF no recibe dinero a mitad de plazo: para aumentar el capital hay que cerrar el
     * certificado y abrir uno nuevo desde Renovacion. Si se carga como un asiento suelto, la
     * contabilidad queda diciendo un capital y la ficha de la cuenta otro.
     * <p/>
     * Se consulta ANTES de entrar al EJB, junto al resto de las validaciones. Antes esto
     * vivia dentro de saveVoucher() como una excepcion, y eso le mataba la transaccion a
     * cualquiera de los 61 llamadores de todos los modulos; en esta pantalla el redisplay
     * ademas reventaba al renderizar una asociacion lazy con la transaccion ya marcada para
     * rollback. Validando antes no hay excepcion, no hay rollback y el asiento cargado se
     * conserva.
     */
    private boolean validateFixedTermDepositCapital() {
        /** create() trabaja sobre el campo voucher; update() sobre la instancia gestionada. */
        Voucher target = isManaged() && getInstance() != null ? getInstance() : voucher;
        Date date = target != null ? target.getDate() : null;
        FixedTermDepositConflict conflict =
                voucherAccoutingService.findFixedTermDepositConflict(date, getVoucherDetails());
        if (conflict == null) {
            return true;
        }
        if (FixedTermDepositConflict.Type.CAPITAL_INCREASE.equals(conflict.getType())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.dpfCapitalAfterOpening",
                    conflict.getAccountCode(),
                    DateUtils.format(conflict.getOpeningDate(), "dd/MM/yyyy"),
                    DateUtils.format(conflict.getVoucherDate(), "dd/MM/yyyy"));
        } else if (FixedTermDepositConflict.Type.PARTIAL_WITHDRAWAL.equals(conflict.getType())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.dpfPartialWithdrawal", conflict.getAccountCode());
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Account.error.dpfWithdrawalUseClosing", conflict.getAccountCode());
        }
        return false;
    }

    /**
     * Nivel 2 del control de DPF: la linea toca una cuenta de capital de DPF pero no dice a
     * que certificado corresponde.
     * <p/>
     * Solo advierte. Sin el enlace no se sabe cual es el certificado, asi que no hay contra
     * que comparar, y un asiento de reclasificacion sobre esas cuentas es legitimo.
     */
    private void warnUnlinkedFixedTermDepositAccounts() {
        List<String> accounts = voucherAccoutingService.findUnlinkedFixedTermDepositAccounts(getVoucherDetails());
        for (String accountCode : accounts) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "Account.warn.dpfAccountWithoutCertificate", accountCode);
        }
    }

    private boolean validateDuplicatedInvoices() {

        for (PurchaseDocument purchaseDocument : purchaseDocumentList) {
            if (isDuplicatedInvoice(purchaseDocument)) {
                addDuplicatedInvoiceMessage(purchaseDocument.getNit(),
                                            purchaseDocument.getNumber(),
                                            purchaseDocument.getDate());
                return false;
            }
        }

        return true;
    }

    /** Una factura ya cargada en este asiento, o ya registrada en otro, cuenta como duplicada. **/
    public boolean isDuplicatedInvoice(PurchaseDocument document) {

        if (document == null) {
            return false;
        }

        String nit = document.getNit();
        String number = document.getNumber();
        Date date = document.getDate();
        BigDecimal amount = document.getAmount();

        /** Fila aun incompleta: no se puede evaluar todavia **/
        if (nit == null || number == null || date == null || amount == null) {
            return false;
        }

        String key = invoiceKey(nit, number, date, amount);

        for (PurchaseDocument other : purchaseDocumentList) {
            if (other == document) {
                continue;
            }
            if (other.getNit() != null && other.getNumber() != null && other.getDate() != null && other.getAmount() != null
                    && key.equals(invoiceKey(other.getNit(), other.getNumber(), other.getDate(), other.getAmount()))) {
                return true;
            }
        }

        return voucherAccoutingService.existsPurchaseDocument(nit, number, date, amount, document.getId());
    }

    private String invoiceKey(String nit, String number, Date date, BigDecimal amount) {
        return nit + "|" + number + "|" + date.getTime() + "|" + amount.stripTrailingZeros().toPlainString();
    }

    private void addDuplicatedInvoiceMessage(String nit, String number, Date date) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "Voucher.message.duplicatedInvoice",
                number,
                nit,
                DateUtils.format(date, MessageUtils.getMessage("patterns.date")));
    }

    /**
     * Compara el credito fiscal del asiento contra el IVA de las facturas y solo advierte.
     * Es informativo: si no se puede calcular no debe impedir el guardado.
     */
    private void warnIfFiscalCreditDoesNotMatch() {

        try {

            BigDecimal totalIVA = BigDecimal.ZERO;
            BigDecimal totalFiscalCredit = BigDecimal.ZERO;

            for (VoucherDetail voucherDetail : voucherDetails) {
                if (voucherDetail.getDebit() != null && isFiscalCredit(voucherDetail)) {
                    totalFiscalCredit = BigDecimalUtil.sum(totalFiscalCredit, voucherDetail.getDebit(), 2);
                }
            }

            for (PurchaseDocument purchaseDocument : purchaseDocumentList) {
                if (purchaseDocument.getAmount() == null) {
                    continue;
                }
                BigDecimal discounts = BigDecimalUtil.sum(purchaseDocument.getRates(), purchaseDocument.getNoTaxCredit(), purchaseDocument.getExempt(), purchaseDocument.getDiscounts());
                BigDecimal partial = BigDecimalUtil.subtract(purchaseDocument.getAmount(), discounts);
                totalIVA = BigDecimalUtil.sum(totalIVA, (BigDecimalUtil.multiply(partial, Constants.VAT)), 2);
            }

            if (totalFiscalCredit.compareTo(totalIVA) != 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incorrectFiscalCredit");
            }

        } catch (Exception e) {
            log.warn("No se pudo verificar el credito fiscal contra las facturas", e);
        }
    }

    /**
     * Marca la transaccion para rollback y limpia los identificadores que Hibernate ya
     * asigno en memoria, de modo que un segundo intento de guardado vuelva a insertar limpio.
     */
    private void rollbackAndDiscardGeneratedIds(Exception cause) {

        log.error("No se pudo guardar el asiento contable", cause);

        try {
            Transaction.instance().setRollbackOnly();
        } catch (Exception e) {
            log.error("No se pudo marcar la transaccion para rollback", e);
        }

        for (PurchaseDocument purchaseDocument : purchaseDocumentList) {
            purchaseDocument.setId(null);
        }

        for (VoucherDetail voucherDetail : voucherDetails) {
            voucherDetail.setId(null);
            voucherDetail.setTransactionNumber(null);
        }

        voucher.setId(null);
        voucher.setTransactionNumber(null);
        voucher.setDocumentNumber(null);
    }

    /**
     * Determina si la excepcion (o su causa, ya venga envuelta en EJBException) es un
     * conflicto de bloqueo optimista de @Version (edicion concurrente del asiento).
     */
    private boolean isOptimisticLock(Throwable e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof OptimisticLockException || t instanceof StaleObjectStateException) {
                return true;
            }
            if (t instanceof EJBException && ((EJBException) t).getCausedByException() != null) {
                t = ((EJBException) t).getCausedByException();
            } else {
                t = t.getCause();
            }
        }
        return false;
    }

    /**
     * Otro usuario modifico el asiento entre que se abrio y se guardo. Se recargan los
     * datos frescos desde la base y se avisa, sin pisar el cambio del otro usuario.
     */
    private String handleVoucherConcurrency(Exception cause) {
        if (cause != null) {
            log.info("Edicion concurrente del asiento contable: " + cause);
        }
        try {
            Voucher fresh = voucherAccoutingService.refreshVoucher(voucher.getId());
            if (fresh != null) {
                setVoucher(fresh);
                setInstance(fresh);
                setVoucherDetails(voucherAccoutingService.refreshVoucherDetailList(fresh));
                setPurchaseDocumentList(voucherAccoutingService.getPurchaseDcumentList(fresh));
                /** Se recargo el estado real: las bajas pendientes apuntaban a la vista
                    vieja y ya no aplican. Se descartan para no borrar filas equivocadas. */
                voucherDetailsToRemove.clear();
                purchaseDocumentsToRemove.clear();
            }
        } catch (Exception e) {
            log.warn("No se pudo recargar el asiento tras el conflicto de concurrencia", e);
        }
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.concurrency");
        return Outcome.REDISPLAY;
    }

    /**
     * Compara la version del asiento en memoria contra la persistida en BD (lectura
     * fresca). Si difieren, otro usuario ya lo modifico: hay que recargar y no pisar.
     */
    private boolean isVoucherStale() {
        if (voucher == null || voucher.getId() == null) {
            return false;
        }
        Long persistedVersion = voucherAccoutingService.getPersistedVersion(voucher.getId());
        return persistedVersion != null && persistedVersion.longValue() != voucher.getVersion();
    }

    @Override
    public String update(){

        if (!isPending()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.notPending");
            return Outcome.REDISPLAY;
        }

        if (isVoucherStale()) {
            return handleVoucherConcurrency(null);
        }

        if (!validateVoucherBalance()) {
            return Outcome.REDISPLAY;
        }

        if (!validateDuplicatedInvoices()) {
            return Outcome.REDISPLAY;
        }

        if (!validateFixedTermDepositCapital()) {
            return Outcome.REDISPLAY;
        }

        warnUnlinkedFixedTermDepositAccounts();
        warnIfFiscalCreditDoesNotMatch();

        try{

            getInstance().setDetails(getVoucherDetails());
            getInstance().setPurchaseList(getPurchaseDocumentList());
            getInstance().setDetailsToRemove(voucherDetailsToRemove);
            getInstance().setPurchaseDocumentsToRemove(purchaseDocumentsToRemove);
            voucherAccoutingService.updateVoucher(getInstance());

            /** Actualizacion exitosa: las bajas ya se aplicaron en la transaccion. */
            voucherDetailsToRemove.clear();
            purchaseDocumentsToRemove.clear();

        }catch (Exception e){
            if (isOptimisticLock(e)) {
                return handleVoucherConcurrency(e);
            }
            log.error("No se pudo actualizar el asiento contable", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.updateError");
            return Outcome.REDISPLAY;
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "Voucher.message.updated");
        return Outcome.SUCCESS;
    }

    public void generateClosingResults(){

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        this.voucher.setDate(endDate);
        this.voucher.setDocumentType(this.docType.getName());
        String number = voucherAccoutingService.getNextMaxNumberByDocType(this.docType.getName(), startDate, endDate).toString();
        System.out.println("====> VOUCHER DOC: " + number);
        voucher.setDocumentNumber(number);

        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        List<Object[]> datas = voucherAccoutingService.getSumsClosingResults(this.startDate, this.endDate);

        BigDecimal debit   = BigDecimal.ZERO;
        BigDecimal credit  = BigDecimal.ZERO;
        BigDecimal debitBalance  = BigDecimal.ZERO;
        BigDecimal creditBalance = BigDecimal.ZERO;

        for(Object[] data: datas){

            debit  = (BigDecimal)data[2];
            credit = (BigDecimal)data[3];

            if (debit.compareTo(credit) > 0){
                debitBalance = BigDecimalUtil.subtract(debit, credit, 2);
            }
            if (credit.compareTo(debit) > 0){
                creditBalance = BigDecimalUtil.subtract(credit, debit, 2);
            }

            if (debitBalance.compareTo(creditBalance) != 0){
                VoucherDetail voucherDetail = new VoucherDetail();
                voucherDetail.setDebit(creditBalance);
                voucherDetail.setCredit(debitBalance);
                voucherDetail.setAccount((String)data[0]);

                CashAccount cashAccount = cashAccountService.findByAccountCode(voucherDetail.getAccount());
                if (cashAccount.getCurrency().equals(FinancesCurrencyType.D) || cashAccount.getCurrency().equals(FinancesCurrencyType.M)){
                    voucherDetail.setDebitMe(BigDecimalUtil.divide(voucherDetail.getDebit(), exchangeRate, 2));
                    voucherDetail.setCreditMe(BigDecimalUtil.divide(voucherDetail.getCredit(), exchangeRate, 2));
                    voucherDetail.setExchangeAmount(exchangeRate);
                    voucherDetail.setCurrency(cashAccount.getCurrency());
                }

                voucher.getDetails().add(voucherDetail);
            }

            //Totaling
            totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)data[2]), 2);
            totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)data[3]), 2);
            debitBalance  = BigDecimal.ZERO;
            creditBalance = BigDecimal.ZERO;
        }


        System.out.println("===> TOTAL DEBE: " + totalDebit);
        System.out.println("===> TOTAL HABER: " + totalCredit);
        System.out.println("===> TOTAL DIFF: " + BigDecimalUtil.subtract(totalDebit, totalCredit, 2));
        if (totalDebit.compareTo(totalCredit) > 0){ /** Perdida **/
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setDebit(BigDecimalUtil.subtract(totalDebit, totalCredit, 2));
            voucherDetail.setCredit(BigDecimal.ZERO);
            voucherDetail.setAccount(companyConfiguration.requireLossCashAccount().getAccountCode());
            voucher.getDetails().add(voucherDetail);
        }
        if (totalCredit.compareTo(totalDebit) > 0){ /** Utilidades **/
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setDebit(BigDecimal.ZERO);
            voucherDetail.setCredit(BigDecimalUtil.subtract(totalCredit, totalDebit, 2));
            voucherDetail.setAccount(companyConfiguration.requireProfitCashAccount().getAccountCode());

            voucher.getDetails().add(voucherDetail);
        }

        voucher.setClosingSeat(Boolean.TRUE);
        voucherAccoutingService.saveVoucher(voucher);

    }

    public void generateBalanceClosure(){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        this.voucher.setDate(endDate);
        this.voucher.setDocumentType(this.docType.getName());
        voucher.setDocumentNumber(voucherAccoutingService.getNextMaxNumberByDocType(this.docType.getName(), startDate, endDate).toString());

        BigDecimal totalDebit   = new BigDecimal(0);
        BigDecimal totalCredit  = new BigDecimal(0);

        List<Object[]> datas = voucherAccoutingService.getSumsBalanceClosure(this.startDate, this.endDate);
        List<Warehouse> warehouseList = warehouseService.getWarehouseList();

        BigDecimal debit   = BigDecimal.ZERO;
        BigDecimal credit  = BigDecimal.ZERO;
        BigDecimal debitBalance  = BigDecimal.ZERO;
        BigDecimal creditBalance = BigDecimal.ZERO;

        String cashAccountCode = null;
        for(Object[] data: datas){
            boolean flagWarehouse = false;
            debit           = (BigDecimal)data[2];
            credit          = (BigDecimal)data[3];
            cashAccountCode = (String)data[0];

            if (debit.compareTo(credit) > 0){
                debitBalance = BigDecimalUtil.subtract(debit, credit, 2);
            }
            if (credit.compareTo(debit) > 0){
                creditBalance = BigDecimalUtil.subtract(credit, debit, 2);
            }

            /** Veerifica si es una cuenta de almacen **/
            for (Warehouse warehouse:warehouseList){
                if (cashAccountCode.equals(warehouse.getWarehouseCashAccount().getAccountCode())) {
                    flagWarehouse = true;
                    break;
                }
            }

            if (!flagWarehouse) { // Si no es una cuenta de almacen
                if (debitBalance.compareTo(creditBalance) != 0){

                    VoucherDetail voucherDetail = new VoucherDetail();
                    voucherDetail.setDebit(creditBalance);
                    voucherDetail.setCredit(debitBalance);
                    voucherDetail.setAccount(cashAccountCode);

                    CashAccount cashAccount = cashAccountService.findByAccountCode(voucherDetail.getAccount());

                        if (cashAccount.getCurrency().equals(FinancesCurrencyType.D) || cashAccount.getCurrency().equals(FinancesCurrencyType.M)) {
                            voucherDetail.setDebitMe(BigDecimalUtil.divide(voucherDetail.getDebit(), exchangeRate, 2));
                            voucherDetail.setCreditMe(BigDecimalUtil.divide(voucherDetail.getCredit(), exchangeRate, 2));
                            voucherDetail.setExchangeAmount(exchangeRate);
                            voucherDetail.setCurrency(cashAccount.getCurrency());
                        }
                        voucher.getDetails().add(voucherDetail);
                }
            }else {
                /** todo Genear cierre de inventario **/
                generateInventoryClosure(cashAccountCode, voucher);
            }

            //Totaling
            totalDebit  = BigDecimalUtil.sum(totalDebit, ((BigDecimal)data[2]), 2);
            totalCredit = BigDecimalUtil.sum(totalCredit, ((BigDecimal)data[3]), 2);
            debitBalance  = BigDecimal.ZERO;
            creditBalance = BigDecimal.ZERO;
        }

        System.out.println("===> TOTAL DEBE: " + totalDebit);
        System.out.println("===> TOTAL HABER: " + totalCredit);
        System.out.println("===> TOTAL DIFF: " + BigDecimalUtil.subtract(totalDebit, totalCredit, 2));

        voucher.setClosingSeat(Boolean.TRUE);
        voucher.setOpeningSeat(Boolean.FALSE);
        voucherAccoutingService.saveVoucher(voucher);

    }

    private void generateInventoryClosure(String cashAccountCode, Voucher voucher){
        Warehouse warehouse = warehouseService.findWarehouseByCashAccount(cashAccountCode);
        physicalInventoryReportAction.setStartDate(startDate);
        physicalInventoryReportAction.setEndDate(endDate);
        physicalInventoryReportAction.setWarehouse(warehouse);

        Collection<ValuedPhysicalInventoryReportAction.CollectionData> collectionDataInventory = physicalInventoryReportAction.calculateValuedInventory();

        for (ValuedPhysicalInventoryReportAction.CollectionData data : collectionDataInventory){
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setDebit(BigDecimal.ZERO);
            voucherDetail.setCredit(data.getAmount());
            voucherDetail.setQuantityArt(data.getQuantity());
            voucherDetail.setProductItemCode(data.getCodeArt());
            voucherDetail.setAccount(warehouse.getCashAccount());
            if (data.getAmount().doubleValue() > 0)
                voucher.getDetails().add(voucherDetail);
        }

    }

    public void generateOpeningSeat(){

        System.out.println("======> CIERRE: " + this.voucher.getDocumentType() + "-" + this.voucher.getDocumentNumber() + " - " + this.voucher.getGloss());

        Voucher newVoucher = new Voucher();
        Integer year = DateUtils.getCurrentYear(this.endDate);
        newVoucher.setDate(DateUtils.firstDayOfYear(year+1));
        newVoucher.setDocumentType(this.docType.getName());
        newVoucher.setDocumentNumber("1");
        newVoucher.setGloss("ASIENTO DE APERTURA GESTION " + (year+1));

        for (VoucherDetail voucherDetail : this.voucher.getDetails()){
            VoucherDetail newVoucherDetail = new VoucherDetail();
            newVoucherDetail.setAccount(voucherDetail.getAccount());
            newVoucherDetail.setDebit(voucherDetail.getCredit());
            newVoucherDetail.setCredit(voucherDetail.getDebit());
            newVoucherDetail.setProductItemCode(voucherDetail.getProductItemCode());
            newVoucherDetail.setQuantityArt(voucherDetail.getQuantityArt());
            BigDecimal exchangeRate = voucherDetail.getExchangeAmount();
            CashAccount cashAccount = cashAccountService.findByAccountCode(newVoucherDetail.getAccount());
            if (cashAccount.getCurrency().equals(FinancesCurrencyType.D) || cashAccount.getCurrency().equals(FinancesCurrencyType.M)){
                newVoucherDetail.setDebitMe(BigDecimalUtil.divide(newVoucherDetail.getDebit(), exchangeRate, 2));
                newVoucherDetail.setCreditMe(BigDecimalUtil.divide(newVoucherDetail.getCredit(), exchangeRate, 2));
                newVoucherDetail.setExchangeAmount(exchangeRate);
                newVoucherDetail.setCurrency(cashAccount.getCurrency());
            }

            //Double amountAux = voucherDetail.getDebit().doubleValue() + voucherDetail.getCredit().doubleValue() + voucherDetail.getDebitMe().doubleValue() + voucherDetail.getCreditMe().doubleValue();
            Double amountAux = 0.0;
            if (voucherDetail.getDebit()    != null) amountAux = amountAux + voucherDetail.getDebit().doubleValue();
            if (voucherDetail.getCredit()   != null) amountAux = amountAux + voucherDetail.getCredit().doubleValue();
            if (voucherDetail.getDebitMe()  != null) amountAux = amountAux + voucherDetail.getDebitMe().doubleValue();
            if (voucherDetail.getCreditMe() != null) amountAux = amountAux + voucherDetail.getCreditMe().doubleValue();

            if (amountAux > 0)
                newVoucher.getDetails().add(newVoucherDetail);
        }
        voucher.setOpeningSeat(Boolean.TRUE);
        voucher.setClosingSeat(Boolean.FALSE);
        voucherAccoutingService.saveVoucher(newVoucher);
    }

    public List<Client> autocomplete(Object suggest){
        String pref = (String)suggest;
        ArrayList<Client> result = new ArrayList<Client>();
        Iterator<Client> iterator = clientService.getAllClients().iterator();

        while (iterator.hasNext()) {
            Client elem = ((Client) iterator.next());
            if ((elem.getName() != null && elem.getName().toLowerCase().indexOf(pref.toLowerCase()) == 0) || "".equals(pref))
            {
                result.add(elem);
            }
        }
        return result;
    }

    public String annulVoucher(){

        if (!isPending()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.notPending");
            return Outcome.REDISPLAY;
        }

        if (isVoucherStale()) {
            return handleVoucherConcurrency(null);
        }

        voucher.setState(VoucherState.ANL.toString());
        try {
            voucherAccoutingService.annulVoucher(voucher);
            voucherAccoutingService.annulInvoicesInVoucher(voucher);
        } catch (Exception e) {
            if (isOptimisticLock(e)) {
                return handleVoucherConcurrency(e);
            }
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Voucher.message.annulAccountingEntry");
        return ANNUL_OUTCOME;
    }

    public String changeVoucherState(){
        voucher.setState(VoucherState.PEN.toString());
        voucherAccoutingService.pendingVoucher(voucher);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Voucher.message.pendingAccountingEntry");
        return Outcome.SUCCESS;
    }

    public String approveVoucher(){

        if (!isPending()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Voucher.message.notPending");
            return Outcome.REDISPLAY;
        }

        if (isVoucherStale()) {
            return handleVoucherConcurrency(null);
        }

        if (!validateVoucherBalance()) {
            return Outcome.REDISPLAY;
        }

        voucher.setState(VoucherState.APR.toString());

        try {
            voucherAccoutingService.approveVoucher(voucher);
            voucherAccoutingService.approveInvoicesVoucher(voucher);
        } catch (Exception e) {
            if (isOptimisticLock(e)) {
                return handleVoucherConcurrency(e);
            }
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Voucher.message.approveAccountingEntry");
        return APPROVED_OUTCOME;
    }

    public boolean isPending() {
        boolean result = false;
        if (voucher != null)
            if (VoucherState.PEN.toString().equals(voucher.getState()))
                result = true;

        return result;
    }

    public boolean isApproved() {
        return VoucherState.APR.toString().equals(voucher.getState());
    }

    public boolean isAnnuled() {
        return VoucherState.ANL.toString().equals(voucher.getState());
    }

    public Boolean isFiscalCredit(VoucherDetail voucherDetail){
        if (voucherDetail == null || voucherDetail.getAccount() == null) {
            return false;
        }
        CompanyConfiguration configuration =  getCompanyConfiguration();
        if (configuration == null) {
            return false;
        }
        //return voucherDetail.getAccount().equals("1420710000");
        return voucherDetail.getAccount().equals(configuration.getNationalCurrencyVATFiscalCreditAccountCode());
    }

    public void assignVoucherDetail(CashAccount cashAccount){
        VoucherDetail voucherDetail = new VoucherDetail();
        voucherDetail.setCashAccount(cashAccount);
        voucherDetail.setAccount(cashAccount.getAccountCode());
        voucherDetails.add(voucherDetail);
    }

    /*public void assignCashAccountFiscalCredit(){
        this.account = cashAccountService.findByAccountCode("1420710000");
        assignCashAccountVoucherDetail();
    }*/

    public void assignCashAccountDefault(String accountCode){
        this.account = cashAccountService.findByAccountCode(accountCode);
        assignCashAccountVoucherDetail();
    }


    public void assignCashAccountVoucherDetail(){

        if (account != null){
            //if (account.getAccountCode().equals("1420710000")){ /** MODIFYID Credito Fiscal **/
            if (account.getAllowIva().equals(Boolean.TRUE)){
                /** Cuenta con IVA (Credito Fiscal): en vez de agregar una fila editable,
                    se abre el modal para registrar la factura. */
                setFiscalCredit(true);
                openNewInvoice();
            }else {
                assignInputVoucherDetail();
            }
        }
    }

    /**
     * Prepara el modal para registrar una factura NUEVA de credito fiscal. La cuenta CF
     * es la del boton pulsado ({@code this.account}). Solo inicializa datos en memoria;
     * la factura y su linea contable se agregan recien al Aceptar el modal.
     */
    public void openNewInvoice(){
        this.fiscalCreditAccount = this.account;
        this.invoiceEditing = false;
        this.invoiceOperationSucceeded = false;

        PurchaseDocument invoice = new PurchaseDocument();
        invoice.setDate(voucher != null ? voucher.getDate() : null);
        invoice.setControlCode("0");
        invoice.setAmount(BigDecimal.ZERO);
        invoice.setExempt(BigDecimal.ZERO);
        invoice.setRates(BigDecimal.ZERO);
        invoice.setNoTaxCredit(BigDecimal.ZERO);
        invoice.setDiscounts(BigDecimal.ZERO);
        this.invoiceInEdit = invoice;
    }

    /**
     * Abre el modal para editar una factura ya cargada. Se edita el mismo objeto en
     * memoria; al Aceptar se recalcula su linea de credito fiscal. No toca la BD.
     */
    public void editInvoice(PurchaseDocument purchaseDocument){
        this.invoiceEditing = true;
        this.invoiceOperationSucceeded = false;
        this.invoiceInEdit = purchaseDocument;

        /** El nombre a mostrar es transient: se reconstruye para facturas cargadas de BD. */
        if (purchaseDocument != null && purchaseDocument.getFinancesEntity() != null
                && (purchaseDocument.getFinancesEntityFullName() == null
                    || purchaseDocument.getFinancesEntityFullName().trim().isEmpty())) {
            purchaseDocument.setFinancesEntityFullName(purchaseDocument.getFinancesEntity().getFullName());
        }

        VoucherDetail linked = findFiscalCreditDetail(purchaseDocument);
        this.fiscalCreditAccount = (linked != null && linked.getCashAccount() != null)
                ? linked.getCashAccount() : this.account;
    }

    /**
     * Acepta la factura del modal. Valida los datos, calcula el credito fiscal (13%) y
     * actualiza la lista de facturas y su linea contable EN MEMORIA. No escribe en la
     * base de datos: todo se persiste de forma atomica al Guardar/Actualizar el asiento.
     */
    public void confirmInvoice(){
        invoiceOperationSucceeded = false;

        if (invoiceInEdit == null){
            return;
        }

        /** Razon social y NIT se toman de la entidad seleccionada. */
        if (invoiceInEdit.getFinancesEntity() != null){
            invoiceInEdit.setName(invoiceInEdit.getFinancesEntity().getAcronym());
            invoiceInEdit.setNit(invoiceInEdit.getFinancesEntity().getNitNumber());
        }

        if (invoiceInEdit.getFinancesEntity() == null
                || invoiceInEdit.getNumber() == null || invoiceInEdit.getNumber().trim().isEmpty()
                || invoiceInEdit.getDate() == null
                || invoiceInEdit.getAmount() == null){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.invoiceIncomplete");
            return;
        }

        if (isDuplicatedInvoice(invoiceInEdit)){
            addDuplicatedInvoiceMessage(invoiceInEdit.getNit(),
                                        invoiceInEdit.getNumber(),
                                        invoiceInEdit.getDate());
            return;
        }

        /**
         * Se calculan y guardan el importe neto (base para credito fiscal) y el IVA sobre
         * la misma factura, de modo que la tabla los muestre ya en memoria (antes de
         * guardar). Coincide con lo que recalcula createDocumentSimple al persistir.
         */
        BigDecimal fiscalCredit = applyInvoiceTotals(invoiceInEdit);

        if (invoiceEditing){
            /** Se recalcula la linea de credito fiscal ya asociada a la factura. */
            VoucherDetail linked = findFiscalCreditDetail(invoiceInEdit);
            if (linked != null){
                linked.setDebit(fiscalCredit);
            }
        }else{
            if (fiscalCreditAccount == null){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incomplete");
                return;
            }
            VoucherDetail voucherDetail = new VoucherDetail();
            voucherDetail.setCashAccount(fiscalCreditAccount);
            voucherDetail.setAccount(fiscalCreditAccount.getAccountCode());
            voucherDetail.setClient(this.client);
            voucherDetail.setProvider(this.provider);
            if (this.provider != null){
                voucherDetail.setProviderCode(this.provider.getProviderCode());
            }
            if (this.voucher != null){
                voucherDetail.setVoucher(voucher);
            }
            voucherDetail.setDebit(fiscalCredit);
            voucherDetail.setCredit(BigDecimal.ZERO);
            voucherDetail.setPurchaseDocument(invoiceInEdit);

            voucherDetails.add(voucherDetail);
            purchaseDocumentList.add(invoiceInEdit);
        }

        invoiceOperationSucceeded = true;
        invoiceInEdit = null;
        invoiceEditing = false;
        fiscalCreditAccount = null;
        clearAll();
    }

    /**
     * Calcula y aplica sobre la factura el importe neto (base de credito fiscal) y el IVA.
     *   importe neto = importe - (excento + tasas + sin CF + descuentos)
     *   IVA          = importe neto * VAT (13%)
     * Devuelve el IVA, que es el debito de la linea de credito fiscal. Es el mismo criterio
     * que aplica createDocumentSimple al persistir, para que pantalla y BD coincidan.
     */
    /**
     * Asigna el proveedor (FinancesEntity) elegido en el modal de busqueda a la factura en
     * edicion y actualiza NIT y Razon Social para mostrarlos debajo. Se llama cada vez que
     * se selecciona un proveedor, asi los dos campos reflejan siempre el ultimo elegido.
     */
    public void assignInvoiceFinancesEntity(FinancesEntity financesEntity){
        if (invoiceInEdit == null || financesEntity == null){
            return;
        }
        invoiceInEdit.setFinancesEntity(financesEntity);
        invoiceInEdit.setName(financesEntity.getAcronym());
        invoiceInEdit.setNit(financesEntity.getNitNumber());
        invoiceInEdit.setFinancesEntityFullName(financesEntity.getFullName());
    }

    /** Limpia el proveedor de la factura en edicion (y su NIT / Razon Social). */
    public void clearInvoiceFinancesEntity(){
        if (invoiceInEdit == null){
            return;
        }
        invoiceInEdit.setFinancesEntity(null);
        invoiceInEdit.setName(null);
        invoiceInEdit.setNit(null);
        invoiceInEdit.setFinancesEntityFullName(null);
    }

    private BigDecimal applyInvoiceTotals(PurchaseDocument purchaseDocument){
        BigDecimal deductions = BigDecimalUtil.sum(purchaseDocument.getExempt(),
                                                   purchaseDocument.getRates(),
                                                   purchaseDocument.getNoTaxCredit(),
                                                   purchaseDocument.getDiscounts());
        BigDecimal netAmount = BigDecimalUtil.subtract(purchaseDocument.getAmount(), deductions, 2);
        BigDecimal iva = BigDecimalUtil.multiply(netAmount, Constants.VAT, 2);

        purchaseDocument.setNetAmount(netAmount);
        purchaseDocument.setIva(iva);
        return iva;
    }

    /**
     * Ubica la linea contable de credito fiscal asociada a una factura. Compara por
     * referencia (facturas nuevas en memoria) y por id (facturas cargadas de la BD, donde
     * la factura de la lista y la del detalle pueden ser instancias distintas).
     */
    private VoucherDetail findFiscalCreditDetail(PurchaseDocument purchaseDocument){
        if (purchaseDocument == null){
            return null;
        }
        for (VoucherDetail voucherDetail : voucherDetails){
            PurchaseDocument linked = voucherDetail.getPurchaseDocument();
            if (linked == null){
                continue;
            }
            if (linked == purchaseDocument){
                return voucherDetail;
            }
            if (linked.getId() != null && linked.getId().equals(purchaseDocument.getId())){
                return voucherDetail;
            }
        }
        return null;
    }


    public void assignInputVoucherDetail(){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        try {
                VoucherDetail voucherDetail = new VoucherDetail();
                voucherDetail.setCashAccount(this.account);
                voucherDetail.setAccount(this.account.getAccountCode());
                voucherDetail.setClient(this.client);
                voucherDetail.setProvider(this.provider);
                voucherDetail.setPartner(this.partner);
                voucherDetail.setPartnerAccount(this.partnerAccount);

                if (this.provider != null) voucherDetail.setProviderCode(this.provider.getProviderCode());

                if (this.productItem != null){

                    System.out.println("====> Product: " + this.productItem.getFullName());
                    System.out.println("====> Cantidad: " + this.getQuantity());

                    voucherDetail.setProductItem(this.productItem);
                    voucherDetail.setProductItemCode(this.productItem.getProductItemCode());
                    voucherDetail.setQuantityArt(BigDecimalUtil.toBigDecimal(quantity));

                }

                voucherDetail.setDebit(this.debit);
                voucherDetail.setCredit(this.credit);
                voucherDetail.setDebitMe(this.debitMe);
                voucherDetail.setCreditMe(this.creditMe);

                if (voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.P)) {
                    voucherDetail.setCurrency(FinancesCurrencyType.P);
                    voucherDetail.setExchangeAmount(BigDecimal.ONE);
                }
                if (voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.D) || voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.M)){
                    voucherDetail.setCurrency(FinancesCurrencyType.D);
                    voucherDetail.setExchangeAmount(exchangeRate);
                }

                voucherDetails.add(voucherDetail);
                clearAll();
                setDebit(BigDecimal.ZERO);
                setCredit(BigDecimal.ZERO);
                setDebitMe(BigDecimal.ZERO);
                setCreditMe(BigDecimal.ZERO);

        } catch (NullPointerException e) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incomplete");
        }
    }

    public void clearAll(){
        clearAccount();
        clearClient();
        clearProvider();
        clearPartner();
        clearProductItem();
        clearPartnerAccount();
    }

    public void assignProductItemVoucherDetail(){

        /*try {*/
            CashAccount ctaCaja     = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH); /** todo **/
            CashAccount ctaIngreso  = cashAccountService.findByAccountCode(Constants.ACOUNT_OTHER_OPERATING_INCOME);

            VoucherDetail voucherCaja = new VoucherDetail();
            voucherCaja.setCashAccount(ctaCaja);
            voucherCaja.setAccount(ctaCaja.getAccountCode());
            voucherCaja.setDebit(BigDecimalUtil.multiply(productItem.getSalePrice(), BigDecimalUtil.toBigDecimal(quantity), 2));
            voucherCaja.setCredit(BigDecimal.ZERO);
            voucherCaja.setQuantityArt(BigDecimalUtil.toBigDecimal(quantity));

            VoucherDetail voucherIngreso = new VoucherDetail();
            voucherIngreso.setCashAccount(ctaIngreso);
            voucherIngreso.setAccount(ctaIngreso.getAccountCode());
            voucherIngreso.setQuantityArt(BigDecimalUtil.toBigDecimal(quantity));

            VoucherDetail voucherDetail = new VoucherDetail(); //Cta Almacen
            voucherDetail.setCashAccount(productItem.getWarehouse().getWarehouseCashAccount());
            voucherDetail.setAccount(productItem.getWarehouse().getWarehouseCashAccount().getAccountCode());
            voucherDetail.setQuantityArt(BigDecimalUtil.toBigDecimal(quantity));

            voucherDetail.setClient(this.client);
            voucherDetail.setProvider(this.provider);
            voucherDetail.setProductItem(productItem);

            if (this.provider != null)
                voucherDetail.setProviderCode(this.provider.getProviderCode());
            if (productItem != null)
                voucherDetail.setProductItemCode(productItem.getProductItemCode());

            voucherDetail.setDebit(BigDecimal.ZERO);
            voucherDetail.setCredit(BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(quantity), 2));

            voucherIngreso.setDebit(BigDecimal.ZERO);
            voucherIngreso.setCredit(BigDecimalUtil.subtract(voucherCaja.getDebit(), voucherDetail.getCredit(), 2));

            voucherDetails.add(voucherCaja);
            voucherDetails.add(voucherDetail);
            voucherDetails.add(voucherIngreso);

            clearAccount();
            clearClient();
            clearProvider();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);

        /*}catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }*/

    }

    public void assignPartnerAccountVoucherDetail(){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        try {

            System.out.println("---> Exchange Rate: " + exchangeRate);
            System.out.println("---> Partner Account: " + partnerAccount.getFullAccountName());
            System.out.println("---> Account Type: " + partnerAccount.getAccountType().getName());
            System.out.println("---> Account Type: " + partnerAccount.getAccountType().getCashAccountMe().getFullName());
            System.out.println("---> Account Type: " + partnerAccount.getAccountType().getCashAccountMn().getFullName());

            CashAccount ctaCajaMn = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH); /** todo **/
            CashAccount ctaCajaMe = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH_ME); /** todo **/

            /** Cuenta CAJA | Billetes Ext **/
            VoucherDetail voucherCaja = new VoucherDetail();

            /* revisar correctamente FCisc, FLech
            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.M)){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"No existe una configuracion en TIPOCUENTA para esta cuenta en Dolares MV");
                return;
            }*/

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D) || partnerAccount.getCurrency().equals(FinancesCurrencyType.M)){

                voucherCaja.setCashAccount(ctaCajaMe);
                voucherCaja.setAccount(ctaCajaMe.getAccountCode());

                voucherCaja.setDebitMe(getAmountDeposit());
                voucherCaja.setCreditMe(BigDecimal.ZERO);

                voucherCaja.setDebit(BigDecimalUtil.multiply(getAmountDeposit(), exchangeRate, 2));
                voucherCaja.setCredit(BigDecimal.ZERO);

                voucherCaja.setExchangeAmount(exchangeRate);
                voucherCaja.setCurrency(FinancesCurrencyType.D);

            }

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.P)) {
                voucherCaja.setCashAccount(ctaCajaMn);
                voucherCaja.setAccount(ctaCajaMn.getAccountCode());

                voucherCaja.setDebit(getAmountDeposit());
                voucherCaja.setCredit(BigDecimal.ZERO);

                voucherCaja.setDebitMe(BigDecimal.ZERO);
                voucherCaja.setCreditMe(BigDecimal.ZERO);

                voucherCaja.setExchangeAmount(BigDecimal.ONE);
                voucherCaja.setCurrency(FinancesCurrencyType.P);
            }


            /** todo: Cuentas deben existir si no error Null **/
            VoucherDetail voucherSaving = new VoucherDetail();
            voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMn());
            voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMn().getAccountCode());

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D)) {
                voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMe());
                voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMe().getAccountCode());
            }

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.M)) {
                voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMv());
                voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMv().getAccountCode());
            }

            /** ----- **/
            voucherSaving.setPartnerAccount(partnerAccount);

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D) || partnerAccount.getCurrency().equals(FinancesCurrencyType.M)) {

                voucherSaving.setDebitMe(BigDecimal.ZERO);
                voucherSaving.setCreditMe(getAmountDeposit());

                voucherSaving.setDebit(BigDecimal.ZERO);
                voucherSaving.setCredit(BigDecimalUtil.multiply(getAmountDeposit(), exchangeRate, 2));

                voucherSaving.setExchangeAmount(exchangeRate);
                voucherSaving.setCurrency(FinancesCurrencyType.D);
            }

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.P)) {
                voucherSaving.setDebit(BigDecimal.ZERO);
                voucherSaving.setCredit(getAmountDeposit());

                voucherSaving.setDebitMe(BigDecimal.ZERO);
                voucherSaving.setCreditMe(BigDecimal.ZERO);

                voucherSaving.setExchangeAmount(BigDecimal.ONE);
                voucherSaving.setCurrency(FinancesCurrencyType.P);
            }

            voucherDetails.add(voucherCaja);
            voucherDetails.add(voucherSaving);

            clearAccount();
            clearClient();
            clearProvider();
            clearPartnerAccount();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }

    public void cashWithdrawalAccountVoucherDetail(){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }

        try {
            /** todo: Cuentas deben existir si no error Null **/
            VoucherDetail voucherSaving = new VoucherDetail();
            voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMn());
            voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMn().getAccountCode());

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D)) {
                voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMe());
                voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMe().getAccountCode());
            }

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.M)) {
                voucherSaving.setCashAccount(partnerAccount.getAccountType().getCashAccountMv());
                voucherSaving.setAccount(partnerAccount.getAccountType().getCashAccountMv().getAccountCode());
            }

            voucherSaving.setPartnerAccount(partnerAccount);

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D) || partnerAccount.getCurrency().equals(FinancesCurrencyType.M)) {

                voucherSaving.setDebitMe(getAmountDeposit());
                voucherSaving.setCreditMe(BigDecimal.ZERO);
                voucherSaving.setDebit(BigDecimalUtil.multiply(getAmountDeposit(), exchangeRate, 2));
                voucherSaving.setCredit(BigDecimal.ZERO);

                voucherSaving.setExchangeAmount(exchangeRate);
            }

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.P)) {

                voucherSaving.setDebit(getAmountDeposit());
                voucherSaving.setCredit(BigDecimal.ZERO);
                voucherSaving.setDebitMe(BigDecimal.ZERO);
                voucherSaving.setCreditMe(BigDecimal.ZERO);

                voucherSaving.setExchangeAmount(BigDecimal.ONE);
            }


            /** **/
            CashAccount ctaCajaMn = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH); /** todo **/
            CashAccount ctaCajaMe = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH_ME); /** todo **/
            VoucherDetail voucherCaja = new VoucherDetail();
            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.D) || partnerAccount.getCurrency().equals(FinancesCurrencyType.M)) {

                voucherCaja.setCashAccount(ctaCajaMe);
                voucherCaja.setAccount(ctaCajaMe.getAccountCode());

                voucherCaja.setDebitMe(BigDecimal.ZERO);
                voucherCaja.setCreditMe(getAmountDeposit());
                voucherCaja.setDebit(BigDecimal.ZERO);
                voucherCaja.setCredit(BigDecimalUtil.multiply(getAmountDeposit(), exchangeRate, 2));

                voucherCaja.setExchangeAmount(exchangeRate);

            }

            if (partnerAccount.getCurrency().equals(FinancesCurrencyType.P)) {
                voucherCaja.setCashAccount(ctaCajaMn);
                voucherCaja.setAccount(ctaCajaMn.getAccountCode());

                voucherCaja.setDebit(BigDecimal.ZERO);
                voucherCaja.setCredit(getAmountDeposit());
                voucherCaja.setDebitMe(BigDecimal.ZERO);
                voucherCaja.setCreditMe(BigDecimal.ZERO);

                voucherCaja.setExchangeAmount(BigDecimal.ONE);
            }

            /** **/
            voucherDetails.add(voucherSaving);
            voucherDetails.add(voucherCaja);

            clearPartnerAccount();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }

    public void assignPartnerVoucherDetail(){
        try {
            System.out.println("---> Partner: " + partner.getFullName());

            CashAccount boxAccount          = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH); /** todo **/
            CashAccount contributionAccount = cashAccountService.findByAccountCode(Constants.ACCOUNT_CONTRIBUTION);

            VoucherDetail voucherBox = new VoucherDetail();
            voucherBox.setCashAccount(boxAccount);
            voucherBox.setAccount(boxAccount.getAccountCode());
            voucherBox.setDebit(getContribution());
            voucherBox.setCredit(BigDecimal.ZERO);


            VoucherDetail voucherContribution = new VoucherDetail();

            voucherContribution.setCashAccount(contributionAccount);
            voucherContribution.setAccount(contributionAccount.getAccountCode());
            voucherContribution.setPartner(this.partner);
            //voucherContribution.setClient(this.client);
            //voucherContribution.setProvider(this.provider);
            //voucherContribution.setPartnerAccount(partnerAccount);

            /*if (this.provider != null)
                voucherContribution.setProviderCode(this.provider.getProviderCode());*/

            voucherContribution.setDebit(BigDecimal.ZERO);
            voucherContribution.setCredit(getContribution());

            voucherDetails.add(voucherBox);
            voucherDetails.add(voucherContribution);

            clearPartner();
            clearAccount();
            clearClient();
            clearProvider();
            clearPartnerAccount();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }
    public void withdrawalPartnerVoucherDetail(){
        try {
            System.out.println("---> Partner: " + partner.getFullName());

            CashAccount boxAccount          = cashAccountService.findByAccountCode(Constants.ACCOUNT_GENERALCASH); /** todo **/
            CashAccount contributionAccount = cashAccountService.findByAccountCode(Constants.ACCOUNT_CONTRIBUTION);

            VoucherDetail voucherBox = new VoucherDetail();
            voucherBox.setCashAccount(boxAccount);
            voucherBox.setAccount(boxAccount.getAccountCode());
            voucherBox.setDebit(BigDecimal.ZERO);
            voucherBox.setCredit(getContribution());


            VoucherDetail voucherContribution = new VoucherDetail();

            voucherContribution.setCashAccount(contributionAccount);
            voucherContribution.setAccount(contributionAccount.getAccountCode());
            voucherContribution.setPartner(this.partner);

            voucherContribution.setDebit(getContribution());
            voucherContribution.setCredit(BigDecimal.ZERO);

            voucherDetails.add(voucherContribution);
            voucherDetails.add(voucherBox);

            clearPartner();

            clearAccount();
            clearClient();
            clearProvider();
            clearPartnerAccount();
            setDebit(BigDecimal.ZERO);
            setCredit(BigDecimal.ZERO);
        }catch (NullPointerException e){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"Voucher.message.incomplete");
        }

    }


    public void moveVoucherDetailUp(VoucherDetail voucherDetail) {
        swapVoucherDetail(voucherDetails.indexOf(voucherDetail), -1);
    }

    public void moveVoucherDetailDown(VoucherDetail voucherDetail) {
        swapVoucherDetail(voucherDetails.indexOf(voucherDetail), 1);
    }

    private void swapVoucherDetail(int index, int offset) {
        int target = index + offset;
        if (index < 0 || target < 0 || target >= voucherDetails.size()) {
            return;
        }
        VoucherDetail moved = voucherDetails.remove(index);
        voucherDetails.add(target, moved);
    }

    public boolean isFirstVoucherDetail(VoucherDetail voucherDetail) {
        return voucherDetails.indexOf(voucherDetail) <= 0;
    }

    public boolean isLastVoucherDetail(VoucherDetail voucherDetail) {
        return voucherDetails.indexOf(voucherDetail) == voucherDetails.size() - 1;
    }

    /**
     * Quita una linea contable del asiento y, si tiene factura asociada, tambien la
     * factura. La eliminacion en BD solo ocurre para lo que ya estaba persistido (id no
     * nulo); lo que solo existe en memoria (asiento nuevo o factura recien cargada) se
     * quita sin tocar la base de datos.
     */
    public void removeVoucherDetail(VoucherDetail voucherDetail) {
        voucherDetails.remove(voucherDetail);

        PurchaseDocument invoice = voucherDetail.getPurchaseDocument();
        if (invoice != null) {
            removeInvoiceFromList(invoice);
        }

        /**
         * Bajas diferidas: si la linea ya estaba persistida (edicion de un asiento guardado)
         * NO se borra ahora; se marca y se elimina recien al Actualizar, en la misma
         * transaccion. La factura asociada viaja con el detalle (getPurchaseDocument) y se
         * borra junto a el, respetando las claves foraneas cruzadas. Si la linea solo existe
         * en memoria (asiento nuevo o recien cargada), se descarta sin tocar la BD.
         */
        if (voucherDetail.getId() != null) {
            voucherDetailsToRemove.add(voucherDetail);
        }
    }

    /**
     * Completa el Debe de la fila indicada con el monto que falta para cuadrar el asiento
     * (Debe total = Haber total). Se usa en la ultima fila para cerrar el asiento en un clic.
     */
    public void completeDebit(VoucherDetail voucherDetail) {
        if (voucherDetail == null) {
            return;
        }
        BigDecimal current = voucherDetail.getDebit() != null ? voucherDetail.getDebit() : BigDecimal.ZERO;
        BigDecimal otherDebit = getTotalsDebit().subtract(current);
        BigDecimal needed = getTotalsCredit().subtract(otherDebit);
        if (needed.compareTo(BigDecimal.ZERO) < 0) {
            needed = BigDecimal.ZERO;
        }
        voucherDetail.setDebit(needed.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Completa el Haber de la fila indicada con el monto que falta para cuadrar el asiento
     * (Haber total = Debe total).
     */
    public void completeCredit(VoucherDetail voucherDetail) {
        if (voucherDetail == null) {
            return;
        }
        BigDecimal current = voucherDetail.getCredit() != null ? voucherDetail.getCredit() : BigDecimal.ZERO;
        BigDecimal otherCredit = getTotalsCredit().subtract(current);
        BigDecimal needed = getTotalsDebit().subtract(otherCredit);
        if (needed.compareTo(BigDecimal.ZERO) < 0) {
            needed = BigDecimal.ZERO;
        }
        voucherDetail.setCredit(needed.setScale(2, RoundingMode.HALF_UP));
    }

    /** Quita una factura de la lista, por referencia y por id (instancias distintas). */
    private void removeInvoiceFromList(PurchaseDocument invoice) {
        if (invoice == null) {
            return;
        }
        purchaseDocumentList.remove(invoice);
        if (invoice.getId() != null) {
            Iterator<PurchaseDocument> iterator = purchaseDocumentList.iterator();
            while (iterator.hasNext()) {
                PurchaseDocument other = iterator.next();
                if (invoice.getId().equals(other.getId())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Elimina una fila de factura (Documento de compra). Sirve tambien cuando la factura
     * quedo "huerfana" (p.ej. bloqueada por duplicada): en ese caso no tiene linea de
     * credito fiscal asociada ni esta persistida, y antes no habia forma de quitarla.
     *  - Si tiene linea de credito fiscal asociada, se quita esa linea (que a su vez
     *    elimina la factura y su registro en BD).
     *  - Si esta suelta, se quita de la lista y solo se borra de BD si ya estaba persistida.
     */
    /**
     * Libera el proveedor (R.Social) de una fila de factura para volver a elegirlo.
     * Una vez seleccionado, la fila muestra el proveedor bloqueado; con esto se puede
     * cambiar sin riesgo de perder la referencia por un caracter de mas.
     */
    public void clearPurchaseDocumentProvider(PurchaseDocument purchaseDocument) {
        if (purchaseDocument != null) {
            purchaseDocument.setFinancesEntity(null);
            purchaseDocument.setFinancesEntityFullName(null);
            purchaseDocument.setName(null);
            purchaseDocument.setNit(null);
        }
    }

    public void removePurchaseDocument(PurchaseDocument purchaseDocument){
        if (purchaseDocument == null) {
            return;
        }

        VoucherDetail linkedDetail = findFiscalCreditDetail(purchaseDocument);
        if (linkedDetail != null) {
            removeVoucherDetail(linkedDetail);
            return;
        }

        /** Factura sin linea contable asociada. Diferida igual que los detalles. */
        removeInvoiceFromList(purchaseDocument);
        if (purchaseDocument.getId() != null) {
            purchaseDocumentsToRemove.add(purchaseDocument);
        }
    }

    /** Marca la factura a eliminar; la confirmacion se hace en un modal antes de quitarla. */
    public void prepareRemoveInvoice(PurchaseDocument purchaseDocument){
        this.invoiceToRemove = purchaseDocument;
    }

    /** Elimina la factura previamente seleccionada (tras confirmar en el modal). */
    public void removeSelectedInvoice(){
        if (invoiceToRemove != null){
            removePurchaseDocument(invoiceToRemove);
            invoiceToRemove = null;
        }
    }

    public boolean incomplete(PurchaseDocument purchaseDocument){

        boolean result = false;

        VoucherDetail voucherDetail = purchaseDocumentService.getVoucherDetail(purchaseDocument);

        if (voucherDetail == null)
            result = true;


        System.out.println("------> INCOMPLETE?: " + result);

        return result;

    }

    public void assignProvider(Provider provider) {
        setProvider(provider);
    }

    /**
     * Prepara la seleccion de proveedor para el ENCABEZADO. Se dispara al abrir el modal
     * desde el campo de proveedor del encabezado (selectAction), reseteando la fila objetivo
     * para que la eleccion aplique al encabezado y no a una fila que quedo marcada antes.
     */
    public void prepareHeaderProvider() {
        this.voucherDetailForProvider = null;
    }

    /** Abre la seleccion de proveedor para una FILA (linea contable) especifica. */
    public void openRowProvider(VoucherDetail voucherDetail) {
        this.voucherDetailForProvider = voucherDetail;
    }

    /**
     * Asigna el proveedor elegido: a la fila objetivo si hay una marcada, o al encabezado
     * si no. La descripcion de la linea muestra el proveedor via getFullCashAccount().
     */
    public void assignProviderSmart(Provider provider) {
        if (voucherDetailForProvider != null) {
            if (provider != null) {
                voucherDetailForProvider.setProvider(provider);
                voucherDetailForProvider.setProviderCode(provider.getProviderCode());
            }
            voucherDetailForProvider = null;
        } else {
            setProvider(provider);
        }
    }

    /** Quita el proveedor de una fila (linea contable). En memoria hasta Guardar/Actualizar. */
    public void clearRowProvider(VoucherDetail voucherDetail) {
        if (voucherDetail != null) {
            voucherDetail.setProvider(null);
            voucherDetail.setProviderCode(null);
        }
    }

    /* todo */
    private void cleanMovementDetailFields() {

    }
    /* todo */
    public void cleanMainFields() {

        cleanMovementDetailFields();
    }

    public BigDecimal getTotalsDebit(){
        BigDecimal total = new BigDecimal("0.00");
        for (VoucherDetail voucherDetail : voucherDetails) {
            if(voucherDetail.getDebit() != null)
                total = total.add(voucherDetail.getDebit());
        }
        //System.out.println("....getTotalsDebit:::: " + total);
        return total;
    }

    public BigDecimal getTotalInvoice(){
        BigDecimal total = new BigDecimal("0.00");
        for (PurchaseDocument purchaseDocument : purchaseDocumentList) {
            if(purchaseDocument.getAmount() != null)
                total = total.add(purchaseDocument.getAmount());
        }
        System.out.println("....TOTAL INVOICE:::: " + total);
        return total;
    }

    public BigDecimal getTotalsCredit(){
        BigDecimal total = new BigDecimal("0.00");
        for (VoucherDetail voucherDetail : voucherDetails) {
            if(voucherDetail.getCredit() != null)
                total = total.add(voucherDetail.getCredit());
        }
        //System.out.println("....getTotalsCredit:::: " + total);
        return total;
    }

    public List<CashAccount> getCashAccounts() {
        return cashAccounts;
    }

    public void setCashAccounts(List<CashAccount> cashAccounts) {
        this.cashAccounts = cashAccounts;
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

    public void convertToNationalCurrency(VoucherDetail voucherDetail){

        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            if (voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.D) || voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.M)){
                exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
                voucherDetail.setDebit(BigDecimalUtil.multiply(voucherDetail.getDebitMe(), exchangeRate, 2));
                voucherDetail.setCredit(BigDecimalUtil.multiply(voucherDetail.getCreditMe(), exchangeRate, 2));
            }
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }
    }

    public void convertToForeignCurrency(VoucherDetail voucherDetail){
        BigDecimal exchangeRate = BigDecimal.ZERO;
        try {
            if (voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.D) || voucherDetail.getCashAccount().getCurrency().equals(FinancesCurrencyType.M)){
                exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.toString());
                voucherDetail.setDebitMe(BigDecimalUtil.divide(voucherDetail.getDebit(), exchangeRate, 2));
                voucherDetail.setCreditMe(BigDecimalUtil.divide(voucherDetail.getCredit(), exchangeRate, 2));
            }
        }catch (FinancesExchangeRateNotFoundException e){
            addFinancesExchangeRateNotFoundExceptionMessage();
        }catch (FinancesCurrencyNotFoundException e){
            addFinancesCurrencyNotFoundMessage();
        }
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }


    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public VoucherAccoutingService getVoucherAccoutingService() {
        return voucherAccoutingService;
    }

    public void setVoucherAccoutingService(VoucherAccoutingService voucherAccoutingService) {
        this.voucherAccoutingService = voucherAccoutingService;
    }

    public List<VoucherDetail> getVoucherDetails() {
        return voucherDetails;
    }

    public void setVoucherDetails(List<VoucherDetail> voucherDetails) {
        this.voucherDetails = voucherDetails;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Client getClient() {
        System.out.println("Get Client");
        return client;
    }

    public void setClient(Client client) {
        System.out.println("Set Client");
        this.client = client;
    }

    public void assignData(){
        System.out.println("---> assignData . . . . " + voucherDetailAction.getInstance().getClientFullName());
        //System.out.println("---> assignData client... " + client);
    }

    public void printData(){
        System.out.println("-> CLIENTE : " + client);
        System.out.println("-> PROVEEDOR : " + provider);
        System.out.println("-> ACCOUNT : " + account);
    }

    /**
     * El select() previo estaba solo para que el reporte encontrara los totales en este
     * action. Ahora los calcula del comprobante que recibe, asi que cargar el asiento en
     * la pantalla de edicion para imprimirlo dejo de tener sentido.
     */
    public void generateReport(Voucher instance){
        try{
            voucherReportAction.generateReport(instance);
        } catch (NullPointerException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "Voucher.message.incomplete");
        }

    }

    public void assignProviderAndFinancesEntity(Provider provider) {
        setProvider(provider);
        purchaseDocumentAction.assignFinancesEntity(provider.getEntity());
    }

    public List<CashAccount> accountsUtil() {
        return cashAccountService.findCashAccountsUtil();
    }


    public CashAccount getAccount() {
        return account;
    }

    public void setAccount(CashAccount account) {
        this.account = account;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public void setClientFullName(String clientFullName) {
        this.clientFullName = clientFullName;
    }

    public String getProviderFullName() {
        return providerFullName;
    }

    public void setProviderFullName(String providerFullName) {
        this.providerFullName = providerFullName;
    }

    public void clearAccount() {
        setAccount(null);
    }

    public void clearPartner() {
        setPartner(null);
        setContribution(null);
    }

    public void clearClient(){
        setClient(null);
    }

    public void clearProductItem(){
        setProductItem(null);
    }

    public void clearPartnerAccount(){
        setPartnerAccount(null);
        setAmountDeposit(null);
    }

    public void clearProvider(){
        setProvider(null);
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Account getPartnerAccount() {
        return partnerAccount;
    }

    public void setPartnerAccount(Account partnerAccount) {
        this.partnerAccount = partnerAccount;
        /**
         * El texto de la cuenta de ahorro se materializa ACA, no en la vista.
         * <p/>
         * Account.getFullAccountName() atraviesa partner, que es @ManyToOne(LAZY). Si la
         * vista lo resuelve al renderizar, cualquier camino de error que haya marcado la
         * transaccion para rollback deja el proxy sin sesion y la pantalla revienta con
         * LazyInitializationException en vez de mostrar el mensaje -- pasaba con el
         * catch(Exception) generico de create() y update(). Este setter corre en
         * UPDATE_MODEL, con el contexto de persistencia vivo, asi que el texto queda
         * resuelto antes de que nada pueda fallar.
         */
        this.partnerAccountLabel = partnerAccount != null ? partnerAccount.getFullAccountName() : null;
    }

    public String getPartnerAccountLabel() {
        return partnerAccountLabel;
    }

    public Boolean getFiscalCredit() {
        return fiscalCredit;
    }

    public void setFiscalCredit(Boolean fiscalCredit) {
        this.fiscalCredit = fiscalCredit;
    }

    public List<PurchaseDocument> getPurchaseDocumentList() {
        return purchaseDocumentList;
    }

    public void setPurchaseDocumentList(List<PurchaseDocument> purchaseDocumentList) {
        this.purchaseDocumentList = purchaseDocumentList;
    }

    public PurchaseDocument getInvoiceInEdit() {
        return invoiceInEdit;
    }

    public void setInvoiceInEdit(PurchaseDocument invoiceInEdit) {
        this.invoiceInEdit = invoiceInEdit;
    }

    public boolean isInvoiceEditing() {
        return invoiceEditing;
    }

    public boolean isInvoiceOperationSucceeded() {
        return invoiceOperationSucceeded;
    }

    public PurchaseDocument getInvoiceToRemove() {
        return invoiceToRemove;
    }

    public BigDecimal getAmountDeposit() {
        return amountDeposit;
    }

    public void setAmountDeposit(BigDecimal amountDeposit) {
        this.amountDeposit = amountDeposit;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public void assignPartner(Partner partner){
        setPartner(partner);
    }

    public BigDecimal getContribution() {
        return contribution;
    }

    public void setContribution(BigDecimal contribution) {
        this.contribution = contribution;
    }

    private void addFinancesCurrencyNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FixedAssets.FinancesCurrencyNotFoundException");
    }

    private void addFinancesExchangeRateNotFoundExceptionMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "FixedAssets.FinancesExchangeRateNotFoundException");
    }

    public BigDecimal getDebitMe() {
        return debitMe;
    }

    public void setDebitMe(BigDecimal debitMe) {
        this.debitMe = debitMe;
    }

    public BigDecimal getCreditMe() {
        return creditMe;
    }

    public void setCreditMe(BigDecimal creditMe) {
        this.creditMe = creditMe;
    }

    public Boolean getCurrencyCondition() {
        return currencyCondition;
    }

    public void setCurrencyCondition(Boolean currencyCondition) {
        this.currencyCondition = currencyCondition;
    }

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

    private CompanyConfiguration getCompanyConfiguration(){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            return companyConfiguration;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");
            return null;
        }
    }
}
