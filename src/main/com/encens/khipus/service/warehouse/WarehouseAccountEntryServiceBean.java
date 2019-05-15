package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.production.ProductionPlanningAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.warehouse.WarehouseAccountCashNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.purchases.PurchaseOrderPayment;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentState;
import com.encens.khipus.model.purchases.PurchaseOrderPaymentType;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.finances.*;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.purchases.GlossGeneratorService;
import com.encens.khipus.util.*;
import com.encens.khipus.util.warehouse.WarehouseUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.5.2.2
 */
@Stateless
@Name("warehouseAccountEntryService")
@AutoCreate
public class WarehouseAccountEntryServiceBean extends GenericServiceBean implements WarehouseAccountEntryService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private VoucherService voucherService;

    @In
    private VoucherAccoutingService voucherAccoutingService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private AdvancePaymentService advancePaymentService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private GlossGeneratorService glossGeneratorService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    private RotatoryFundService rotatoryFundService;

    @In
    private User currentUser;

    @In
    private MovementDetailService movementDetailService;

    @In
    private WarehouseService warehouseService;

    @In
    private CashAccountService cashAccountService;

    @In
    private FinancesPkGeneratorService financesPkGeneratorService;

    /* For advance payments of warehouse and fixedAssets */

    public void createAdvancePaymentAccountEntry(PurchaseOrderPayment purchaseOrderPayment) throws CompanyConfigurationNotFoundException {
        Voucher voucher = null;
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        String executorUnitCode = purchaseOrderPayment.getPurchaseOrder().getExecutorUnit().getExecutorUnitCode();
        String costCenterCode = purchaseOrderPayment.getPurchaseOrder().getCostCenter().getCode();

        CashAccount payCashAccount = null;
        if (PurchaseOrderPaymentKind.ADVANCE_PAYMENT.equals(purchaseOrderPayment.getPurchaseOrderPaymentKind())) {
            payCashAccount = FinancesCurrencyType.D.equals(purchaseOrderPayment.getPayCurrency()) ?
                    companyConfiguration.getAdvancePaymentForeignCurrencyAccount() : companyConfiguration.getAdvancePaymentNationalCurrencyAccount();
        } else if (PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT.equals(purchaseOrderPayment.getPurchaseOrderPaymentKind())) {
            payCashAccount = purchaseOrderPayment.getPurchaseOrder().getProvider().getPayableAccount();
        }

        if (payCashAccount == null) {
            throw new CompanyConfigurationNotFoundException("The system configuration (payCashAccountCode) for current company haven't been configured");
        }

        BigDecimal bankExchangeRate = FinancesCurrencyType.D.equals(purchaseOrderPayment.getSourceCurrency()) ?
                purchaseOrderPayment.getExchangeRate() : BigDecimal.ONE;
        BigDecimal payExchangeRate = FinancesCurrencyType.D.equals(payCashAccount.getCurrency()) ?
                purchaseOrderPayment.getExchangeRate() : BigDecimal.ONE;
        BigDecimal payAmount = FinancesCurrencyType.D.equals(payCashAccount.getCurrency()) ?
                BigDecimalUtil.multiply(purchaseOrderPayment.getPayAmount(), purchaseOrderPayment.getExchangeRate()) : purchaseOrderPayment.getPayAmount();

        BigDecimal voucherAmountNationalAmount = FinancesCurrencyType.D.equals(purchaseOrderPayment.getSourceCurrency()) ?
                purchaseOrderPayment.getSourceAmount().multiply(purchaseOrderPayment.getExchangeRate()) : purchaseOrderPayment.getSourceAmount();

        if (PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(purchaseOrderPayment.getPaymentType())) {
            Long sequenceNumber = sequenceGeneratorService.nextValue(Constants.ADVANCEPAYMENT_DOCUMENT_SEQUENCE);
            voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                    Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                    Constants.BANKACCOUNT_VOUCHERTYPE_DEBITNOTE_DOCTYPE,
                    Constants.ADVANCEPAYMENT_DOCNUMBER_PREFFIX + sequenceNumber,
                    purchaseOrderPayment.getBankAccountNumber(),
                    purchaseOrderPayment.getSourceAmount(),
                    purchaseOrderPayment.getSourceCurrency(),
                    bankExchangeRate,
                    purchaseOrderPayment.getDescription());
            /* TODO may be the beneficiary should be included in the voucher*/
        } else if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPayment.getPaymentType())) {
            voucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                    Constants.CHECK_VOUCHERTYPE_FORM,
                    Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                    purchaseOrderPayment.getBankAccountNumber(),
                    purchaseOrderPayment.getBeneficiaryName(),
                    purchaseOrderPayment.getSourceAmount(),
                    purchaseOrderPayment.getSourceCurrency(),
                    bankExchangeRate,
                    purchaseOrderPayment.getPurchaseOrder().getExecutorUnit(),
                    purchaseOrderPayment.getDescription());
        } else if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(purchaseOrderPayment.getPaymentType())) {
            voucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, purchaseOrderPayment.getDescription());
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    purchaseOrderPayment.getCashBoxCashAccount(),
                    voucherAmountNationalAmount,
                    purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                    bankExchangeRate));
            voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());
        } else if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(purchaseOrderPayment.getPaymentType())) {
            voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, purchaseOrderPayment.getDescription());
            CashAccount rotatoryFundCashAccount = rotatoryFundService.matchCashAccount(purchaseOrderPayment.getRotatoryFund());
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    rotatoryFundCashAccount,
                    voucherAmountNationalAmount,
                    rotatoryFundCashAccount.getCurrency(),
                    bankExchangeRate));
        }

        if (voucher != null) {
            if (purchaseOrderPayment.getAccountingEntryDefaultDate() != null) {
                voucher.setDate(purchaseOrderPayment.getAccountingEntryDefaultDate());
                voucher.setExpirationDate(purchaseOrderPayment.getAccountingEntryDefaultDate());
            }
            if (!ValidatorUtil.isBigDecimal(purchaseOrderPayment.getAccountingEntryDefaultUserNumber())) {
                voucher.setUserNumber(purchaseOrderPayment.getAccountingEntryDefaultUserNumber());
            }

            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    payCashAccount,
                    payAmount,
                    payCashAccount.getCurrency(),
                    payExchangeRate));

            BigDecimal balanceAmount = BigDecimalUtil.subtract(payAmount, voucherAmountNationalAmount);
            if (balanceAmount.doubleValue() > 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount,
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            } else if (balanceAmount.doubleValue() < 0) {
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                        companyConfiguration.getBalanceExchangeRateAccount(),
                        balanceAmount.abs(),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            }

            //voucher.setTransactionNumber(financesPkGeneratorService.getNextTmpenc()); Error
            voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
            voucherService.create(voucher);
            purchaseOrderPayment.setTransactionNumber(voucher.getTransactionNumber());
            if (!getEntityManager().contains(purchaseOrderPayment)) {
                getEntityManager().merge(purchaseOrderPayment);
            }
            getEntityManager().flush();
        }
    }

    /* when a warehouse purchase order is been liquidated
    *  warehouse vs (advance and liquidation payments)*/

    public void createEntryAccountForLiquidatedPurchaseOrder(PurchaseOrder purchaseOrder, BigDecimal defaultExchangeRate)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {
        BigDecimal sumAdvancePaymentAmount = advancePaymentService.sumAllPaymentAmountsByKind(purchaseOrder, PurchaseOrderPaymentKind.ADVANCE_PAYMENT);
        BigDecimal sumLiquidationPaymentAmount = advancePaymentService.sumAllPaymentAmountsByKind(purchaseOrder, PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);

        if (BigDecimalUtil.isZeroOrNull(sumAdvancePaymentAmount)) {
            sumAdvancePaymentAmount = BigDecimal.ZERO;
        }
        if (BigDecimalUtil.isZeroOrNull(sumLiquidationPaymentAmount)) {
            sumLiquidationPaymentAmount = BigDecimal.ZERO;
        }


        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        if (ValidatorUtil.isBlankOrNull(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode())) {
            throw new CompanyConfigurationNotFoundException("The system configuration (warehouseNationalCurrencyAccountCode) for current company haven't been configured");
        }

        String executorUnitCode = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
        String costCenterCode = purchaseOrder.getCostCenter().getCode();

        String gloss = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder,
                MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"), MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym"));

        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
        if (CollectionDocumentType.INVOICE.equals(purchaseOrder.getDocumentType())) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                    BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT_COMPLEMENT),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getNationalCurrencyVATFiscalCreditTransientAccount(),
                    BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                    purchaseOrder.getTotalAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }

        BigDecimal totalCreditAmount = BigDecimal.ZERO;

        if (BigDecimalUtil.isPositive(sumAdvancePaymentAmount)) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getAdvancePaymentNationalCurrencyAccount(),
                    sumAdvancePaymentAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            totalCreditAmount = BigDecimalUtil.sum(totalCreditAmount, sumAdvancePaymentAmount);
        }

        if (BigDecimalUtil.isPositive(sumLiquidationPaymentAmount)) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    purchaseOrder.getProvider().getPayableAccount(),
                    sumLiquidationPaymentAmount,
                    purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), defaultExchangeRate)));
            totalCreditAmount = BigDecimalUtil.sum(totalCreditAmount, sumLiquidationPaymentAmount);
        }

        BigDecimal balanceAmount = BigDecimalUtil.subtract(purchaseOrder.getTotalAmount(), totalCreditAmount);

        if (balanceAmount.doubleValue() > 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else if (balanceAmount.doubleValue() < 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount.abs(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }
        //voucher.setTransactionNumber(financesPkGeneratorService.getNextTmpenc()); Error
        voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
        //voucherService.create(voucher);
        voucherAccoutingService.saveVoucher(voucher);
    }

    public Voucher createEntryAccountForValidatePurchaseOrder(PurchaseOrder purchaseOrder, BigDecimal defaultExchangeRate) throws   CompanyConfigurationNotFoundException,
                                                                                                                                    FinancesCurrencyNotFoundException,
                                                                                                                                    FinancesExchangeRateNotFoundException {
        BigDecimal sumAdvancePaymentAmount = advancePaymentService.sumAllPaymentAmountsByKind(purchaseOrder, PurchaseOrderPaymentKind.ADVANCE_PAYMENT);
        //BigDecimal sumLiquidationPaymentAmount = advancePaymentService.sumAllPaymentAmountsByKind(purchaseOrder, PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);
        BigDecimal sumLiquidationPaymentAmount = advancePaymentService.sumAllPaymentAmountsByKindPurchaseOrder(purchaseOrder, PurchaseOrderPaymentKind.LIQUIDATION_PAYMENT);

        if (BigDecimalUtil.isZeroOrNull(sumAdvancePaymentAmount)) {
            sumAdvancePaymentAmount = BigDecimal.ZERO;
        }
        if (BigDecimalUtil.isZeroOrNull(sumLiquidationPaymentAmount)) {
            sumLiquidationPaymentAmount = BigDecimal.ZERO;
        }


        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        if (ValidatorUtil.isBlankOrNull(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode())) {
            throw new CompanyConfigurationNotFoundException("The system configuration (warehouseNationalCurrencyAccountCode) for current company haven't been configured");
        }

        String executorUnitCode = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
        String costCenterCode = purchaseOrder.getCostCenter().getCode();

        String gloss = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder,
                MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"), MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym"));

        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
        if (CollectionDocumentType.INVOICE.equals(purchaseOrder.getDocumentType())) {
            if(purchaseOrder.getWithBill().compareTo(Constants.WITH_BILL) == 0){
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                        BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT_COMPLEMENT),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(),
                        BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            }else{
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                    BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT_COMPLEMENT),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getNationalCurrencyVATFiscalCreditTransientAccount(),
                    BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            }
        } else {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                    purchaseOrder.getTotalAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }

        BigDecimal totalCreditAmount = BigDecimal.ZERO;

        if (BigDecimalUtil.isPositive(sumAdvancePaymentAmount)) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getAdvancePaymentNationalCurrencyAccount(),
                    sumAdvancePaymentAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            totalCreditAmount = BigDecimalUtil.sum(totalCreditAmount, sumAdvancePaymentAmount);
        }

        if (BigDecimalUtil.isPositive(sumLiquidationPaymentAmount)) {
            VoucherDetail voucherDetail = VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    purchaseOrder.getProvider().getPayableAccount(),
                    sumLiquidationPaymentAmount,
                    purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), defaultExchangeRate));
            voucherDetail.setProviderCode(purchaseOrder.getProviderCode());
            voucher.addVoucherDetail(voucherDetail);
            totalCreditAmount = BigDecimalUtil.sum(totalCreditAmount, sumLiquidationPaymentAmount);
        }else{
            VoucherDetail voucherDetail = VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    purchaseOrder.getProvider().getPayableAccount(),
                    purchaseOrder.getTotalAmount(),
                    purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), defaultExchangeRate));
            voucherDetail.setProviderCode(purchaseOrder.getProviderCode());
            voucher.addVoucherDetail(voucherDetail);
            totalCreditAmount = BigDecimalUtil.sum(totalCreditAmount, purchaseOrder.getTotalAmount());
        }


        BigDecimal balanceAmount = BigDecimalUtil.subtract(purchaseOrder.getTotalAmount(), totalCreditAmount);

        if (balanceAmount.doubleValue() > 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        } else if (balanceAmount.doubleValue() < 0) {
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                    companyConfiguration.getBalanceExchangeRateAccount(),
                    balanceAmount.abs(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }
        //voucher.setTransactionNumber(financesPkGeneratorService.getNextTmpenc()); Error
        voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
        voucher.setProviderCode(purchaseOrder.getProviderCode());
        voucher.setDocumentType(Constants.IA_VOUCHER_DOCTYPE);

        //voucherService.create(voucher);
        voucherAccoutingService.saveVoucher(voucher);
        return voucher;
    }

    /** 2019 **/
    public Voucher createEntryAccountForPurchaseOrder(WarehouseVoucher warehouseVoucher) throws CompanyConfigurationNotFoundException,
                                                                                                FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {
        PurchaseOrder purchaseOrder = warehouseVoucher.getPurchaseOrder();
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

        if (ValidatorUtil.isBlankOrNull(companyConfiguration.getWarehouseNationalCurrencyTransientAccountCode())) {
            throw new CompanyConfigurationNotFoundException("The system configuration (warehouseNationalCurrencyAccountCode) for current company haven't been configured");
        }

        String executorUnitCode = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
        String costCenterCode   = purchaseOrder.getCostCenter().getCode();
        String gloss = glossGeneratorService.generatePurchaseOrderGloss(purchaseOrder, MessageUtils.getMessage("WarehousePurchaseOrder.warehouses"),
                                                                                                                MessageUtils.getMessage("WarehousePurchaseOrder.orderNumberAcronym"));
        Voucher voucher = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());

        /** Asocia Cuenta de Alm con producto **/
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByVoucher(warehouseVoucher);
        for (MovementDetail detail : movementDetailList){
            voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                    detail.getAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE,
                    detail.getProductItemCode(), detail.getQuantity()));

            totalDebitAmount = BigDecimalUtil.sum(totalDebitAmount, detail.getAmount(), 2);
        }


        if (CollectionDocumentType.INVOICE.equals(purchaseOrder.getDocumentType())) {
            if(purchaseOrder.getWithBill().compareTo(Constants.WITH_BILL) == 0){

                BigDecimal amountVAT = BigDecimalUtil.multiply(purchaseOrder.getTotalAmount(), Constants.VAT);
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(),
                        amountVAT,
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));

                totalDebitAmount = BigDecimalUtil.sum(totalDebitAmount, amountVAT);
            }
        }


        BigDecimal totalCreditAmount = purchaseOrder.getTotalAmount();
        VoucherDetail voucherDetail = new VoucherDetail();

        if (purchaseOrder.getPayConditions().getName().equals(Constants.CONDITION_CREDIT)) {
            voucherDetail = VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    purchaseOrder.getProvider().getPayableAccount(),
                    totalCreditAmount,
                    purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), BigDecimal.ONE));
        }
        if (purchaseOrder.getPayConditions().getName().equals(Constants.CONDITION_CASH)) {
            voucherDetail = VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnitCode,
                    costCenterCode,
                    companyConfiguration.getGeneralCashAccountNational(),
                    totalCreditAmount,
                    purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), BigDecimal.ONE));
        }

        voucherDetail.setProviderCode(purchaseOrder.getProviderCode());
        voucher.addVoucherDetail(voucherDetail);

        BigDecimal balanceAmount = BigDecimalUtil.subtract(totalDebitAmount, totalCreditAmount);

        System.out.println("---> Total Debit =  " + totalDebitAmount);
        System.out.println("---> Total Credit =  " + totalCreditAmount);


        /** En caso de haber diferencias, se ajusta en el articulo **/
        if (balanceAmount.doubleValue() > 0) { // Debit major
            System.out.println("----->>>----->>> DIFF: " + balanceAmount);
            for (VoucherDetail detail : voucher.getDetails()){
                if (detail.getProductItemCode() != null && detail.getDebit().doubleValue() > 0){
                    detail.setDebit(BigDecimalUtil.subtract(detail.getDebit(), balanceAmount, 2));
                    break;
                }
            }
        } else if (balanceAmount.doubleValue() < 0) {
            System.out.println("----->>>----->>> DIFF: " + balanceAmount);
            for (VoucherDetail detail : voucher.getDetails()){
                if (detail.getProductItemCode() != null && detail.getDebit().doubleValue() > 0){
                    detail.setDebit(BigDecimalUtil.sum(detail.getDebit(), BigDecimalUtil.abs(balanceAmount), 2));
                    break;
                }
            }
        }

        voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
        voucher.setProviderCode(purchaseOrder.getProviderCode());
        voucher.setDocumentType(Constants.IA_VOUCHER_DOCTYPE);

        warehouseVoucher.setVoucher(voucher);
        voucherAccoutingService.saveVoucher(voucher);
        return voucher;
    }
    /* when a fixedAsset or warehouse purchases order has been liquidated whith only check
    *  (bank or cashbox) vs provider */

    public void setPurchaseOrderForPaymentCheck(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment,String transactionNumber)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {
        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {

            purchaseOrderPayment.setPayCurrency(FinancesCurrencyType.P);
            purchaseOrderPayment.setState(PurchaseOrderPaymentState.APPROVED);
            purchaseOrderPayment.setCreationDate(new Date());
            purchaseOrderPayment.setRegisterEmployee(currentUser);
            purchaseOrderPayment.setApprovalDate(new Date());
            purchaseOrderPayment.setApprovedByEmployee(currentUser);
            purchaseOrderPayment.setPurchaseOrder(purchaseOrder);
            if (BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getExchangeRate())) {
                purchaseOrderPayment.setExchangeRate(BigDecimal.ONE);
            }

            purchaseOrderPayment.setTransactionNumber(transactionNumber);
            getEntityManager().persist(purchaseOrderPayment);
            getEntityManager().flush();
        }
    }

    public String createEntryAccountPurchaseOrderForPaymentCheck(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment
                                                              ,BigDecimal totalSourceAmount,BigDecimal totalPayAmount)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {

        String transactionNumber = "";
        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(totalPayAmount)
                && !BigDecimalUtil.isZeroOrNull(totalSourceAmount)) {
            Voucher voucher = null;
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

            purchaseOrderPayment.setPayCurrency(FinancesCurrencyType.P);
            purchaseOrderPayment.setState(PurchaseOrderPaymentState.APPROVED);
            purchaseOrderPayment.setCreationDate(new Date());
            purchaseOrderPayment.setRegisterEmployee(currentUser);
            purchaseOrderPayment.setApprovalDate(new Date());
            purchaseOrderPayment.setApprovedByEmployee(currentUser);
            purchaseOrderPayment.setPurchaseOrder(purchaseOrder);
            if (BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getExchangeRate())) {
                purchaseOrderPayment.setExchangeRate(BigDecimal.ONE);
            }

            String executorUnitCode = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
            String costCenterCode = purchaseOrder.getCostCenter().getCode();
            BigDecimal bankExchangeRate = purchaseOrderPayment.getExchangeRate();
            BigDecimal payExchangeRate = purchaseOrderPayment.getExchangeRate();

            BigDecimal voucherAmountNationalAmount = BigDecimalUtil.multiply(totalSourceAmount, payExchangeRate);

            if (PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(purchaseOrderPayment.getPaymentType())) {
                Long sequenceNumber = sequenceGeneratorService.nextValue(Constants.FIXEDASSET_PAYMENT_DOCUMENT_SEQUENCE);
                voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                        Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                        Constants.CP_VOUCHER_DOCTYPE,
                        Constants.FIXEDASSET_PAYMENT_DOCNUMBER_PREFFIX + sequenceNumber,
                        purchaseOrderPayment.getBankAccountNumber(),
                        totalSourceAmount,
                        purchaseOrderPayment.getSourceCurrency(),
                        bankExchangeRate,
                        purchaseOrderPayment.getDescription());
            } else if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPayment.getPaymentType())) {
                voucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                        Constants.CHECK_VOUCHERTYPE_FORM,
                        Constants.CP_VOUCHER_DOCTYPE,
                        purchaseOrderPayment.getBankAccountNumber(),
                        purchaseOrderPayment.getBeneficiaryName(),
                        totalSourceAmount,
                        purchaseOrderPayment.getSourceCurrency(),
                        bankExchangeRate,
                        purchaseOrderPayment.getCheckDestination(),
                        purchaseOrderPayment.getDescription());
            } else if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(purchaseOrderPayment.getPaymentType())) {
                System.out.println("...........>>> PAYMENT_CASHBOX: " + voucherAmountNationalAmount);
                voucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, purchaseOrderPayment.getDescription());
                voucher.setDocumentType(Constants.CP_VOUCHER_DOCTYPE);

                if(purchaseOrder.getWithBill().equals(Constants.WITHOUT_BILL)){
                    /** 1580110300 - MERCADERIAS EN TRANSITO **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                            voucherAmountNationalAmount,
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    Double iueRetention = voucherAmountNationalAmount.doubleValue() * Constants.IUE_RETENTION;
                    Double itRetention  = voucherAmountNationalAmount.doubleValue() * Constants.IT_RETENTION;
                    Double cashBoxValue = voucherAmountNationalAmount.doubleValue() - iueRetention - itRetention;

                    /** 2420310400 IUE **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getIueRetention(),
                            BigDecimalUtil.toBigDecimal(iueRetention),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 2420310500 IT **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getItRetention(),
                            BigDecimalUtil.toBigDecimal(itRetention),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 11101010100  Caja General **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getCashBoxCashAccount(),
                            BigDecimalUtil.toBigDecimal(cashBoxValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());
                }

                if(purchaseOrder.getWithBill().equals(Constants.WITH_BILL)){
                    Double cashBoxValue     = voucherAmountNationalAmount.doubleValue();
                    Double fiscalCreditIVA  = cashBoxValue * 0.13;
                    Double goodsTansitValue =  cashBoxValue - fiscalCreditIVA;

                    /** 1580110300 - MERCADERIAS EN TRANSITO **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                            BigDecimalUtil.toBigDecimal(goodsTansitValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 1420710000 - Credito Fiscal IVA **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(),
                            BigDecimalUtil.toBigDecimal(fiscalCreditIVA),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 11101010100  Caja General **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getCashBoxCashAccount(),
                            BigDecimalUtil.toBigDecimal(cashBoxValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                }

            } else if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(purchaseOrderPayment.getPaymentType())) {
                voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, purchaseOrderPayment.getDescription());
                CashAccount rotatoryFundCashAccount = rotatoryFundService.matchCashAccount(purchaseOrderPayment.getRotatoryFund());
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        rotatoryFundCashAccount,
                        voucherAmountNationalAmount,
                        rotatoryFundCashAccount.getCurrency(),
                        bankExchangeRate));
            }
            if (voucher != null) {
                voucher.setUserNumber(companyConfiguration.getDefaultTreasuryUser().getId());

                /** Para pago con cheque (otros) **/
                if (!PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(purchaseOrderPayment.getPaymentType())){
                    VoucherDetail voucherDetail = VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrder.getProvider().getPayableAccount(),
                            totalPayAmount,
                            purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                            financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), payExchangeRate));

                    System.out.println("=================> Provider CODE: " + purchaseOrder.getProviderCode());
                    voucherDetail.setProviderCode(purchaseOrder.getProviderCode());
                    voucher.addVoucherDetail(voucherDetail);
                }

                if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPayment.getPaymentType())){
                    System.out.println("=====> PAYMENT_WITH_CHECK");
                    //Cash Account, Credit 'Cuenta de Banco'
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail (
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getBankAccount().getCashAccount(),
                            totalPayAmount,
                            purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                            financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), payExchangeRate)));
                }

                BigDecimal balanceAmount = BigDecimalUtil.subtract(totalPayAmount, voucherAmountNationalAmount);
                /** Diferencias de cambio **/
                if (balanceAmount.doubleValue() > 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                            companyConfiguration.getBalanceExchangeRateAccount(),
                            balanceAmount,
                            FinancesCurrencyType.P,
                            BigDecimal.ONE));
                } else if (balanceAmount.doubleValue() < 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                            companyConfiguration.getBalanceExchangeRateAccount(),
                            balanceAmount.abs(),
                            FinancesCurrencyType.P,
                            BigDecimal.ONE));
                }
                //voucher.setTransactionNumber(financesPkGeneratorService.getNextTmpenc()); Error
                voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
                voucher.setProviderCode(purchaseOrder.getProviderCode());

                //voucherService.create(voucher);
                transactionNumber = voucher.getTransactionNumber();
                purchaseOrderPayment.setTransactionNumber(voucher.getTransactionNumber());
                getEntityManager().persist(purchaseOrderPayment);
                getEntityManager().flush();
                voucherAccoutingService.saveVoucher(voucher);
                purchaseOrderPayment.setVoucher(voucher);
                getEntityManager().flush();
            }
        }

        return transactionNumber;
    }

    public String createEntryAccountPurchaseOrderForPaymentWithCashBox(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment
            ,BigDecimal totalSourceAmount,BigDecimal totalPayAmount)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {

        String transactionNumber = "";
        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(totalPayAmount) && !BigDecimalUtil.isZeroOrNull(totalSourceAmount)) {
            Voucher voucher = null;
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

            purchaseOrderPayment.setPayCurrency(FinancesCurrencyType.P);
            purchaseOrderPayment.setState(PurchaseOrderPaymentState.APPROVED);
            purchaseOrderPayment.setCreationDate(new Date());
            purchaseOrderPayment.setRegisterEmployee(currentUser);
            purchaseOrderPayment.setApprovalDate(new Date());
            purchaseOrderPayment.setApprovedByEmployee(currentUser);
            purchaseOrderPayment.setPurchaseOrder(purchaseOrder);

            if (BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getExchangeRate())) {
                purchaseOrderPayment.setExchangeRate(BigDecimal.ONE);
            }

            String executorUnitCode     = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
            String costCenterCode       = purchaseOrder.getCostCenter().getCode();
            BigDecimal bankExchangeRate = purchaseOrderPayment.getExchangeRate();
            BigDecimal payExchangeRate  = purchaseOrderPayment.getExchangeRate();

            BigDecimal voucherAmountNationalAmount = BigDecimalUtil.multiply(totalSourceAmount, payExchangeRate);

            if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(purchaseOrderPayment.getPaymentType())) {
                System.out.println("...........>>> PAYMENT_CASHBOX: " + voucherAmountNationalAmount);
                voucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, purchaseOrderPayment.getDescription());
                voucher.setDocumentType(Constants.CP_VOUCHER_DOCTYPE);

                /** Pago Sin Factura (Recibo) y al Contado **/
                if(purchaseOrder.getWithBill().equals(Constants.WITHOUT_BILL) && purchaseOrder.getPayConditions().getName().equals(Constants.CONDITION_CASH)){
                    System.out.println("*** Pago Sin Factura (Recibo) y al Contado ***");
                    /** 1580110300 - MERCADERIAS EN TRANSITO **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                            voucherAmountNationalAmount,
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    Double iueRetention = voucherAmountNationalAmount.doubleValue() * Constants.IUE_RETENTION;
                    Double itRetention  = voucherAmountNationalAmount.doubleValue() * Constants.IT_RETENTION;
                    Double cashBoxValue = voucherAmountNationalAmount.doubleValue() - iueRetention - itRetention;

                    /** 2420310400 IUE **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getIueRetention(),
                            BigDecimalUtil.toBigDecimal(iueRetention),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 2420310500 IT **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getItRetention(),
                            BigDecimalUtil.toBigDecimal(itRetention),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 11101010100  Caja General **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getCashBoxCashAccount(),
                            BigDecimalUtil.toBigDecimal(cashBoxValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());
                }

                if(purchaseOrder.getWithBill().equals(Constants.WITH_BILL) && purchaseOrder.getPayConditions().getName().equals(Constants.CONDITION_CASH)){
                    Double cashBoxValue     = voucherAmountNationalAmount.doubleValue();
                    Double fiscalCreditIVA  = cashBoxValue * 0.13;
                    Double goodsTansitValue =  cashBoxValue - fiscalCreditIVA;

                    /** 1580110300 - MERCADERIAS EN TRANSITO **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                            BigDecimalUtil.toBigDecimal(goodsTansitValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 1420710000 - Credito Fiscal IVA **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(),
                            BigDecimalUtil.toBigDecimal(fiscalCreditIVA),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                    /** 11101010100  Caja General **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getCashBoxCashAccount(),
                            BigDecimalUtil.toBigDecimal(cashBoxValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                }

                if(purchaseOrder.getWithBill().equals(Constants.WITH_BILL) && purchaseOrder.getPayConditions().getName().equals(Constants.CONDITION_CREDIT)){

                    Double cashBoxValue     = voucherAmountNationalAmount.doubleValue();

                    VoucherDetail voucherDetail = VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrder.getProvider().getPayableAccount(),
                            totalPayAmount,
                            purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                            financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), payExchangeRate));
                    voucherDetail.setProviderCode(purchaseOrder.getProviderCode());
                    voucher.addVoucherDetail(voucherDetail);

                    /** 11101010100  Caja General **/
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getCashBoxCashAccount(),
                            BigDecimalUtil.toBigDecimal(cashBoxValue),
                            purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                            bankExchangeRate));
                    voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());

                }

            }

            if (voucher != null) {
                voucher.setUserNumber(companyConfiguration.getDefaultTreasuryUser().getId());

                /** Para pago con cheque (otros) **/
                if (!PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(purchaseOrderPayment.getPaymentType())){
                    VoucherDetail voucherDetail = VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrder.getProvider().getPayableAccount(),
                            totalPayAmount,
                            purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                            financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), payExchangeRate));

                    System.out.println("=================> Provider CODE: " + purchaseOrder.getProviderCode());
                    voucherDetail.setProviderCode(purchaseOrder.getProviderCode());
                    voucher.addVoucherDetail(voucherDetail);
                }

                if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPayment.getPaymentType())){
                    System.out.println("=====> PAYMENT_WITH_CHECK");
                    //Cash Account, Credit 'Cuenta de Banco'
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail (
                            executorUnitCode,
                            costCenterCode,
                            purchaseOrderPayment.getBankAccount().getCashAccount(),
                            totalPayAmount,
                            purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                            financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), payExchangeRate)));
                }

                BigDecimal balanceAmount = BigDecimalUtil.subtract(totalPayAmount, voucherAmountNationalAmount);
                /** Diferencias de cambio **/
                if (balanceAmount.doubleValue() > 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                            companyConfiguration.getBalanceExchangeRateAccount(),
                            balanceAmount,
                            FinancesCurrencyType.P,
                            BigDecimal.ONE));
                } else if (balanceAmount.doubleValue() < 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                            companyConfiguration.getBalanceExchangeRateAccount(),
                            balanceAmount.abs(),
                            FinancesCurrencyType.P,
                            BigDecimal.ONE));
                }
                //voucher.setTransactionNumber(financesPkGeneratorService.getNextTmpenc()); Error
                voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
                voucher.setProviderCode(purchaseOrder.getProviderCode());

                //voucherService.create(voucher);
                transactionNumber = voucher.getTransactionNumber();
                purchaseOrderPayment.setTransactionNumber(voucher.getTransactionNumber());
                getEntityManager().persist(purchaseOrderPayment);
                getEntityManager().flush();
                voucherAccoutingService.saveVoucher(voucher);
                purchaseOrderPayment.setVoucher(voucher);
                getEntityManager().flush();
            }
        }

        return transactionNumber;
    }

    /* when a fixedAsset or warehouse purchase order has been liquidated
    *  (bank or cashbox) vs provider */

    public void createEntryAccountForPurchaseOrderPayment(PurchaseOrder purchaseOrder, PurchaseOrderPayment purchaseOrderPayment)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {
        if (purchaseOrderPayment != null && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getPayAmount())
                && !BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getSourceAmount())) {
            Voucher voucher = null;
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

            purchaseOrderPayment.setPayCurrency(FinancesCurrencyType.P);
            purchaseOrderPayment.setState(PurchaseOrderPaymentState.APPROVED);
            purchaseOrderPayment.setCreationDate(new Date());
            purchaseOrderPayment.setRegisterEmployee(currentUser);
            purchaseOrderPayment.setApprovalDate(new Date());
            purchaseOrderPayment.setApprovedByEmployee(currentUser);
            purchaseOrderPayment.setPurchaseOrder(purchaseOrder);
            if (BigDecimalUtil.isZeroOrNull(purchaseOrderPayment.getExchangeRate())) {
                purchaseOrderPayment.setExchangeRate(BigDecimal.ONE);
            }

            String executorUnitCode = purchaseOrder.getExecutorUnit().getExecutorUnitCode();
            String costCenterCode = purchaseOrder.getCostCenter().getCode();
            BigDecimal bankExchangeRate = purchaseOrderPayment.getExchangeRate();
            BigDecimal payExchangeRate = purchaseOrderPayment.getExchangeRate();

            BigDecimal voucherAmountNationalAmount = BigDecimalUtil.multiply(purchaseOrderPayment.getSourceAmount(), payExchangeRate);

            if (PurchaseOrderPaymentType.PAYMENT_BANK_ACCOUNT.equals(purchaseOrderPayment.getPaymentType())) {
                Long sequenceNumber = sequenceGeneratorService.nextValue(Constants.FIXEDASSET_PAYMENT_DOCUMENT_SEQUENCE);
                voucher = VoucherBuilder.newBankAccountPaymentTypeVoucher(
                        Constants.BANKACCOUNT_VOUCHERTYPE_FORM,
                        //Constants.BANKACCOUNT_VOUCHERTYPE_DEBITNOTE_DOCTYPE,
                        Constants.CP_VOUCHER_DOCTYPE,
                        Constants.FIXEDASSET_PAYMENT_DOCNUMBER_PREFFIX + sequenceNumber,
                        purchaseOrderPayment.getBankAccountNumber(),
                        purchaseOrderPayment.getSourceAmount(),
                        purchaseOrderPayment.getSourceCurrency(),
                        bankExchangeRate,
                        purchaseOrderPayment.getDescription());
            } else if (PurchaseOrderPaymentType.PAYMENT_WITH_CHECK.equals(purchaseOrderPayment.getPaymentType())) {
                voucher = VoucherBuilder.newCheckPaymentTypeVoucher(
                        Constants.CHECK_VOUCHERTYPE_FORM,
                        //Constants.CHECK_VOUCHERTYPE_DOCTYPE,
                        Constants.CP_VOUCHER_DOCTYPE,
                        purchaseOrderPayment.getBankAccountNumber(),
                        purchaseOrderPayment.getBeneficiaryName(),
                        purchaseOrderPayment.getSourceAmount(),
                        purchaseOrderPayment.getSourceCurrency(),
                        bankExchangeRate,
                        purchaseOrderPayment.getCheckDestination(),
                        purchaseOrderPayment.getDescription());
            } else if (PurchaseOrderPaymentType.PAYMENT_CASHBOX.equals(purchaseOrderPayment.getPaymentType())) {
                voucher = VoucherBuilder.newGeneralVoucher(Constants.CASHBOX_PAYMENT_VOUCHER_FORM, purchaseOrderPayment.getDescription());
                voucher.setDocumentType(Constants.CP_VOUCHER_DOCTYPE);
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        purchaseOrderPayment.getCashBoxCashAccount(),
                        voucherAmountNationalAmount,
                        purchaseOrderPayment.getCashBoxCashAccount().getCurrency(),
                        bankExchangeRate));
                voucher.setEmployeeName(purchaseOrderPayment.getBeneficiaryName());
            } else if (PurchaseOrderPaymentType.PAYMENT_ROTATORY_FUND.equals(purchaseOrderPayment.getPaymentType())) {
                voucher = VoucherBuilder.newGeneralVoucher(Constants.RECEIVABLES_VOUCHER_FORM, purchaseOrderPayment.getDescription());
                CashAccount rotatoryFundCashAccount = rotatoryFundService.matchCashAccount(purchaseOrderPayment.getRotatoryFund());
                voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        rotatoryFundCashAccount,
                        voucherAmountNationalAmount,
                        rotatoryFundCashAccount.getCurrency(),
                        bankExchangeRate));
            }
            if (voucher != null) {
                voucher.setUserNumber(companyConfiguration.getDefaultTreasuryUser().getId());
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        executorUnitCode,
                        costCenterCode,
                        purchaseOrder.getProvider().getPayableAccount(),
                        purchaseOrderPayment.getPayAmount(),
                        purchaseOrder.getProvider().getPayableAccount().getCurrency(),
                        financesExchangeRateService.getExchangeRateByCurrencyType(purchaseOrder.getProvider().getPayableAccount().getCurrency(), payExchangeRate)));
                BigDecimal balanceAmount = BigDecimalUtil.subtract(purchaseOrderPayment.getPayAmount(), voucherAmountNationalAmount);
                if (balanceAmount.doubleValue() > 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                            executorUnitCode,
                            companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                            companyConfiguration.getBalanceExchangeRateAccount(),
                            balanceAmount,
                            FinancesCurrencyType.P,
                            BigDecimal.ONE));
                } else if (balanceAmount.doubleValue() < 0) {
                    voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                            executorUnitCode,
                            companyConfiguration.getExchangeRateBalanceCostCenter().getCode(),
                            companyConfiguration.getBalanceExchangeRateAccount(),
                            balanceAmount.abs(),
                            FinancesCurrencyType.P,
                            BigDecimal.ONE));
                }
                //voucher.setTransactionNumber(financesPkGeneratorService.getNextTmpenc()); Error
                voucher.setTransactionNumber(financesPkGeneratorService.getNextNoTransTmpenc());
                //voucherService.create(voucher);
                purchaseOrderPayment.setTransactionNumber(voucher.getTransactionNumber());
                getEntityManager().persist(purchaseOrderPayment);
                getEntityManager().flush();
                voucherAccoutingService.saveVoucher(voucher);
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void createAccountEntry(WarehouseVoucher warehouseVoucher, String[] gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException {

        System.out.println("0.----------> : createAccountEntry(...");

        if (warehouseVoucher.isTransfer()) {
            log.debug("The account entry should not be generated for transference vouchers.");
            return;
        }

        if (!existsControlValuedProductsItems(warehouseVoucher)) {
            log.debug("Unable to generate the account entry because the all productItems " +
                    "related with movement details are not enabled controlValued property.");
            return;
        }

        log.debug("Generating the account entry for warehouse voucher Nro: " + warehouseVoucher.getNumber());

        if (warehouseVoucher.isReception()) {
            System.out.println("------------------->: createAccountEntryForReception(warehouseVoucher,...");
            createAccountEntryForReception(warehouseVoucher,
                    warehouseVoucher.getExecutorUnit(),
                    warehouseVoucher.getCostCenterCode(),
                    gloss[0]);
        } else {
            if (warehouseVoucher.isExecutorUnitTransfer()) {
                System.out.println("------------------->: if (warehouseVoucher.isExecutorUnitTransfer())");
                createAccountEntryForExecutorUnitTransfer(warehouseVoucher, gloss);
            } else {
                if ((warehouseVoucher.isInput() || warehouseVoucher.isOutput()) && (warehouseVoucher.getDocumentType().isContraAccountDefinedByUser() ||
                                                                                    warehouseVoucher.getDocumentType().isContraAccountDefinedByDefault()))
                {
                    CashAccount contraAccount = warehouseVoucher.getDocumentType().isContraAccountDefinedByUser() ? warehouseVoucher.getContraAccount() : warehouseVoucher.getDocumentType().getContraAccount();

                    if (warehouseVoucher.isInput()) {
                        System.out.println("1---------------------->: createAccountEntryForInputsAndContraAccount(warehouseVoucher...");
                        createAccountEntryForInputsAndContraAccount(warehouseVoucher,
                                contraAccount,
                                warehouseVoucher.getExecutorUnit(),
                                warehouseVoucher.getCostCenterCode(),
                                gloss[0]);
                    } else {
                        System.out.println("2---------------------> : createAccountEntryForOutputsAndContraAccount(warehouseVoucher...");
                        createAccountEntryForOutputsAndContraAccount(warehouseVoucher,
                                contraAccount,
                                warehouseVoucher.getExecutorUnit(),
                                warehouseVoucher.getCostCenterCode(),
                                gloss[0]);
                    }
                } else {
                    MovementDetailType movementDetailType = WarehouseUtil.getMovementTye(warehouseVoucher.getDocumentType());

                    if (MovementDetailType.E.equals(movementDetailType)) {
                        System.out.println("3---------------------------->: createAccountEntryForInputs(warehouseVoucher...");
                        createAccountEntryForInputs(warehouseVoucher,
                                warehouseVoucher.getExecutorUnit(),
                                warehouseVoucher.getCostCenterCode(),
                                gloss[0]);
                    }

                    if (MovementDetailType.S.equals(movementDetailType)) {
                        System.out.println("4--------------------->: createAccountEntryForOutputs(warehouseVoucher...");
                        createAccountEntryForOutputs(warehouseVoucher,
                                warehouseVoucher.getExecutorUnit(),
                                warehouseVoucher.getCostCenterCode(),
                                gloss[0]);
                    }
                }
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public void createAccountEntryFromProductDelivery(WarehouseVoucher warehouseVoucher, String[] gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException {
        if (warehouseVoucher.isTransfer()) {
            log.debug("The account entry should not be generated for transference vouchers.");
            return;
        }

        if (!existsControlValuedProductsItems(warehouseVoucher)) {
            log.debug("Unable to generate the account entry because the all productItems " +
                    "related with movement details are not enabled controlValued property.");
            return;
        }

        log.debug("Generating the account entry for warehouse voucher Nro: " + warehouseVoucher.getNumber());

        if (warehouseVoucher.isReception()) {
            createAccountEntryForReception(warehouseVoucher,
                    warehouseVoucher.getExecutorUnit(),
                    warehouseVoucher.getCostCenterCode(),
                    gloss[0]);
        } else if (warehouseVoucher.isExecutorUnitTransfer()) {
            createAccountEntryForExecutorUnitTransfer(warehouseVoucher, gloss);
        } else if ((warehouseVoucher.isInput() || warehouseVoucher.isOutput()) &&
                (warehouseVoucher.getDocumentType().isContraAccountDefinedByUser() ||
                        warehouseVoucher.getDocumentType().isContraAccountDefinedByDefault())) {
            CashAccount contraAccount = warehouseVoucher.getDocumentType().isContraAccountDefinedByUser() ?
                    warehouseVoucher.getContraAccount() : warehouseVoucher.getDocumentType().getContraAccount();
            if (warehouseVoucher.isInput()) {
                createAccountEntryForInputsAndContraAccount(warehouseVoucher,
                        contraAccount,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            } else {
                createAccountEntryForOutputsAndContraAccount(warehouseVoucher,
                        contraAccount,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            }
        } else {
            MovementDetailType movementDetailType =
                    WarehouseUtil.getMovementTye(warehouseVoucher.getDocumentType());

            if (MovementDetailType.E.equals(movementDetailType)) {
                createAccountEntryForInputs(warehouseVoucher,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            }

            if (MovementDetailType.S.equals(movementDetailType)) {
                createAccountEntryForOutputsFromProductDelivery(warehouseVoucher,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public String createAccountEntryFromCollection(WarehouseVoucher warehouseVoucher, String[] gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException {
        String numTrasaction = "";
        if (warehouseVoucher.isTransfer()) {
            log.debug("The account entry should not be generated for transference vouchers.");
            return "";
        }

        if (!existsControlValuedProductsItems(warehouseVoucher)) {
            log.debug("Unable to generate the account entry because the all productItems " +
                    "related with movement details are not enabled controlValued property.");
            return "";
        }

        log.debug("Generating the account entry for warehouse voucher Nro: " + warehouseVoucher.getNumber());

        if (warehouseVoucher.isReception()) {
            numTrasaction = createAccountEntryForReceptionFromCollection(warehouseVoucher,
                    warehouseVoucher.getExecutorUnit(),
                    warehouseVoucher.getCostCenterCode(),
                    gloss[0]);
        } else if (warehouseVoucher.isExecutorUnitTransfer()) {
            createAccountEntryForExecutorUnitTransfer(warehouseVoucher, gloss);
        } else if ((warehouseVoucher.isInput() || warehouseVoucher.isOutput()) &&
                (warehouseVoucher.getDocumentType().isContraAccountDefinedByUser() ||
                        warehouseVoucher.getDocumentType().isContraAccountDefinedByDefault())) {
            CashAccount contraAccount = warehouseVoucher.getDocumentType().isContraAccountDefinedByUser() ?
                    warehouseVoucher.getContraAccount() : warehouseVoucher.getDocumentType().getContraAccount();
            if (warehouseVoucher.isInput()) {
                createAccountEntryForInputsAndContraAccount(warehouseVoucher,
                        contraAccount,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            } else {
                createAccountEntryForOutputsAndContraAccount(warehouseVoucher,
                        contraAccount,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            }
        } else {
            MovementDetailType movementDetailType =
                    WarehouseUtil.getMovementTye(warehouseVoucher.getDocumentType());

            if (MovementDetailType.E.equals(movementDetailType)) {
                createAccountEntryForInputs(warehouseVoucher,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            }

            if (MovementDetailType.S.equals(movementDetailType)) {
                createAccountEntryForOutputs(warehouseVoucher,
                        warehouseVoucher.getExecutorUnit(),
                        warehouseVoucher.getCostCenterCode(),
                        gloss[0]);
            }
        }
        return numTrasaction;
    }

    @Override
    public String createAccountEntryForReceptionProductionOrder(WarehouseVoucher warehouseVoucher,
                                                BusinessUnit executorUnit,
                                                String costCenterCode,
                                                String gloss,
                                                List<ProductionPlanningAction.AccountOrderProduction> accountOrderProductions)
            throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.INPUT_PROD_WAREHOUSE, gloss);
        voucherForGeneration.setUserNumber(companyConfiguration.getDefaultAccountancyUserProduction().getId());

        //String transactionNumber = financesPkGeneratorService.getNextTmpenc(); Error
        String transactionNumber = financesPkGeneratorService.getNextNoTransTmpenc();
        voucherForGeneration.setTransactionNumber(transactionNumber);

        Warehouse warehouse = warehouseService.findWarehouseByCode(warehouseVoucher.getWarehouseCode());
        BigDecimal total = BigDecimal.ZERO;

        for(ProductionPlanningAction.AccountOrderProduction accountOrderProduction :accountOrderProductions)
        {
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    accountOrderProduction.getExecutorUnit().getExecutorUnitCode(),
                    accountOrderProduction.getCostCenterCode(),
                    accountOrderProduction.getCashAccount(),
                    accountOrderProduction.getVoucherAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            total = total.add(accountOrderProduction.getVoucherAmount());
        }

        if(voucherAmount.doubleValue() != total.doubleValue())
        {
            voucherAmount = total;
            movementDetailService.updateAmountTotal(warehouseVoucher.getId().getTransactionNumber(),total);
        }

        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByVoucher(warehouseVoucher);

        for (MovementDetail detail : movementDetailList){
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    //companyConfiguration.getWarehouseNationalCurrencyTransientAccount2(),
                    cashAccountService.findByAccountCode(warehouse.getCashAccount()),
                    detail.getPurchasePrice(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE, detail.getProductItemCode(), detail.getQuantity()));
        }

        /*voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                //companyConfiguration.getWarehouseNationalCurrencyTransientAccount2(),
                cashAccountService.findByAccountCode(warehouse.getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE, productItemCode, quantity));*/

        voucherForGeneration.setDate(warehouseVoucher.getDate());
        //check transactionNumber
        voucherForGeneration.setDocumentType(Constants.PD_VOUCHER_DOCTYPE);
        warehouseVoucher.setVoucher(voucherForGeneration);
        voucherAccoutingService.saveVoucher(voucherForGeneration);

        return voucherForGeneration.getTransactionNumber();
    }

    private void createAccountEntryForReception(WarehouseVoucher warehouseVoucher,
                                                BusinessUnit executorUnit,
                                                String costCenterCode,
                                                String gloss)
            throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucherForGeneration.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());

        //String transactionNumber = financesPkGeneratorService.getNextTmpenc(); Error
        String transactionNumber = financesPkGeneratorService.getNextNoTransTmpenc();
        voucherForGeneration.setTransactionNumber(transactionNumber);

        if(warehouseVoucher.getPurchaseOrder() != null)
            voucherForGeneration.setProviderCode(warehouseVoucher.getPurchaseOrder().getProvider().getProviderCode());

        /** Asocia Cuenta de Alm con producto **/
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByVoucher(warehouseVoucher);
        for (MovementDetail detail : movementDetailList){

            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                    cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                    detail.getAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE,
                    detail.getProductItemCode(), detail.getQuantity()));
            totalAmount = BigDecimalUtil.sum(totalAmount, detail.getAmount(), 2);
        }

        for (MovementDetail detail : movementDetailList){
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    detail.getProductItem().getCashAccount() ,
                    detail.getAmount(),
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
        }

        /*voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                totalAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));*/

        warehouseVoucher.setVoucher(voucherForGeneration);
        voucherAccoutingService.saveVoucher(voucherForGeneration);

    }


    public void createAccountEntryForProductTransfer(WarehouseVoucher warehouseVoucherFrom, WarehouseVoucher warehouseVoucherTo, BusinessUnit executorUnit, String costCenterCode, String gloss)  throws CompanyConfigurationNotFoundException{

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucherFrom.getId().getCompanyNumber(), warehouseVoucherFrom.getState(), warehouseVoucherFrom.getId().getTransactionNumber());

        String cod_art_from = movementDetailService.getCodeByNoTrans(warehouseVoucherFrom.getId().getTransactionNumber());
        BigDecimal cant_art_from  = movementDetailService.getCantByNoTrans(warehouseVoucherFrom.getId().getTransactionNumber());

        String cod_art_to = movementDetailService.getCodeByNoTrans(warehouseVoucherTo.getId().getTransactionNumber());
        BigDecimal cant_art_to  = movementDetailService.getCantByNoTrans(warehouseVoucherTo.getId().getTransactionNumber());

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucherForGeneration.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());

        String transactionNumber = financesPkGeneratorService.getNextNoTransTmpenc();

        Long id_tmpenc = financesPkGeneratorService.newId_sf_tmpenc();
        String docNumber = financesPkGeneratorService.getNextNoTransByDocumentType("TR");


        System.out.println("-------------------------------> transactionNumber: " + transactionNumber);
        System.out.println("-------------------------------> newId_sf_tmpenc: " + id_tmpenc);

        voucherForGeneration.setTransactionNumber(transactionNumber);
        voucherForGeneration.setDocumentNumber(docNumber);

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                cashAccountService.findByAccountCode(warehouseVoucherTo.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE, cod_art_to, cant_art_to));

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                cashAccountService.findByAccountCode(warehouseVoucherFrom.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE, cod_art_from, cant_art_from));

        warehouseVoucherFrom.setVoucher(voucherForGeneration);
        warehouseVoucherTo.setVoucher(voucherForGeneration);


        /** **/
        Voucher voucher = voucherForGeneration;
        voucher.setId(id_tmpenc);

        System.out.println("-------- VOUCHER ENCABEZADO -------");
        System.out.println("ID: " + voucher.getId());
        System.out.println("Fecha: " + voucher.getDate());
        System.out.println("NoTrans: " + voucher.getTransactionNumber());
        System.out.println("Tipo Doc: " + voucher.getDocumentType());
        System.out.println("Descripcion: " + voucher.getDescription());
        System.out.println("Glosa: " + voucher.getGloss());

        em.persist(voucher);
        em.flush();

        System.out.println("-------- VOUCHER DETAILS -------");
        for (VoucherDetail voucherDetail : voucher.getDetails()) {
            voucherDetail.setId(financesPkGeneratorService.newId_sf_tmpdet());
            voucherDetail.setTransactionNumber(voucher.getTransactionNumber());
            voucherDetail.setVoucher(voucher);

            System.out.println("CUENTA: " + voucherDetail.getAccount() + " NOTRANS:" + voucherDetail.getTransactionNumber() + " D:" + voucherDetail.getDebit() + " H:" + voucherDetail.getCredit());

            em.persist(voucherDetail);
            em.flush();
        }

        /** **/
        //voucherAccoutingService.saveVoucher(voucherForGeneration);

    }

    private String createAccountEntryForReceptionFromCollection(WarehouseVoucher warehouseVoucher,
                                                              BusinessUnit executorUnit,
                                                              String costCenterCode,
                                                              String gloss)
            throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.IN_WAREHOUSE_MILK_COLLECTED_FORM, gloss);
        voucherForGeneration.setUserNumber(companyConfiguration.getDefaultAccountancyUserProduction().getId());

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                companyConfiguration.getWarehouseNationalCurrencyTransientAccount1(),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));
        //check transactionNumber
        //voucherService.create(voucherForGeneration);
        warehouseVoucher.setVoucher(voucherForGeneration);
        voucherAccoutingService.saveVoucher(voucherForGeneration);
        return voucherForGeneration.getTransactionNumber();
    }

    private void createAccountEntryForExecutorUnitTransfer(WarehouseVoucher warehouseVoucher,
                                                           String[] gloss)
            throws CompanyConfigurationNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        // Source
        Voucher sourceVoucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss[0]);
        sourceVoucherForGeneration.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
        sourceVoucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                warehouseVoucher.getExecutorUnit().getExecutorUnitCode(),
                warehouseVoucher.getCostCenterCode(),
                companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));
        sourceVoucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                warehouseVoucher.getExecutorUnit().getExecutorUnitCode(),
                warehouseVoucher.getCostCenterCode(),
                //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));
        //check transactionNumber
        //voucherService.create(sourceVoucherForGeneration);
        warehouseVoucher.setVoucher(sourceVoucherForGeneration);
        voucherAccoutingService.saveVoucher(sourceVoucherForGeneration);

        // Target
        Voucher targetVoucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss[1]);
        targetVoucherForGeneration.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
        targetVoucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                warehouseVoucher.getTargetExecutorUnit().getExecutorUnitCode(),
                warehouseVoucher.getTargetCostCenterCode(),
                companyConfiguration.getWarehouseNationalCurrencyTransientAccount(),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));
        targetVoucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                warehouseVoucher.getTargetExecutorUnit().getExecutorUnitCode(),
                warehouseVoucher.getTargetCostCenterCode(),
                //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));
        //check transactionNumber
        warehouseVoucher.setVoucher(targetVoucherForGeneration);
        voucherService.create(targetVoucherForGeneration);
        voucherAccoutingService.saveVoucher(targetVoucherForGeneration);
    }

    private void createAccountEntryForInputs(WarehouseVoucher warehouseVoucher,
                                             BusinessUnit executorUnit,
                                             String costCenterCode,
                                             String gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException, WarehouseAccountCashNotFoundException {
        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<MovementDetail> movementDetails = movementDetailService.findDetailByVoucherAndType(warehouseVoucher, MovementDetailType.E);

        BigDecimal total = BigDecimal.ZERO;
        for (MovementDetail movementDetail : movementDetails) {
            if (!movementDetail.getProductItem().getControlValued()) {
                log.debug("The movement detail with id=" +
                        movementDetail.getId() +
                        " was skipped because his product item does not have the controlValue field enabled.");
                continue;
            }

            if (BigDecimalUtil.isZeroOrNull(movementDetail.getAmount())) {
                log.debug("The movement detail with id=" +
                        movementDetail.getId() +
                        " was skipped because his amount its equal to zero.");
                continue;
            }

            BigDecimal detailAmount = BigDecimalUtil.roundBigDecimal(movementDetail.getAmount());
            total = BigDecimalUtil.sum(total, detailAmount);
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    movementDetail.getProductItemCashAccount(),
                    detailAmount,
                    movementDetail.getProductItemCashAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(movementDetail.getProductItemCashAccount().getCurrency(), BigDecimal.ONE)));
        }

        if (BigDecimalUtil.isPositive(total)) {
            voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                    cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                    total,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            //check transactionNumber
            //voucherService.create(voucherForGeneration);
            voucherAccoutingService.saveVoucher(voucherForGeneration);
        }
    }

    private void createAccountEntryForOutputs(WarehouseVoucher warehouseVoucher, BusinessUnit executorUnit, String costCenterCode, String gloss)
            throws CompanyConfigurationNotFoundException,FinancesCurrencyNotFoundException,FinancesExchangeRateNotFoundException {

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<MovementDetail> movementDetails = movementDetailService.findDetailByVoucherAndType(warehouseVoucher, MovementDetailType.S);

        String transactionNumber = financesPkGeneratorService.getNextNoTransTmpenc();
        voucherForGeneration.setTransactionNumber(transactionNumber);

        BigDecimal total = BigDecimal.ZERO;
        for (MovementDetail movementDetail : movementDetails) {
            if (!movementDetail.getProductItem().getControlValued()) {
                log.debug("The movement detail with id=" +
                        movementDetail.getId() +
                        " was skipped because his product item does not have the controlValue field enabled.");
                continue;
            }

            if (BigDecimalUtil.isZeroOrNull(movementDetail.getAmount())) {
                log.debug("The movement detail with id=" +
                        movementDetail.getId() +
                        " was skipped because his amount its equal to zero.");
                continue;
            }

            BigDecimal detailAmount = BigDecimalUtil.roundBigDecimal(movementDetail.getAmount());
            total = BigDecimalUtil.sum(total, detailAmount);

            CashAccount debitCashAccount = movementDetail.getProductItemCashAccount();

            /** Cuenta para Baja de productos **/
            if (warehouseVoucher.getDocumentType().getWarehouseVoucherType().equals(WarehouseVoucherType.B))
                debitCashAccount = companyConfiguration.getLowAccount();

            /** Cuenta para reprocesos **/
            if (warehouseVoucher.getDocumentType().getWarehouseVoucherType().equals(WarehouseVoucherType.W))
                debitCashAccount = companyConfiguration.getReworkAccount();

            System.out.println("======> aaaaa : " + movementDetail.getProductItem().getFullName() + " - " + detailAmount);

            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    debitCashAccount,
                    detailAmount,
                    movementDetail.getProductItemCashAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(movementDetail.getProductItemCashAccount().getCurrency(), BigDecimal.ONE)));
        }

        if (BigDecimalUtil.isPositive(total)) {
            voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());

            List<MovementDetail> movementDetailList = movementDetailService.findDetailListByVoucher(warehouseVoucher);
            for (MovementDetail detail : movementDetailList){
                voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                        executorUnit.getExecutorUnitCode(),
                        costCenterCode,
                        cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                        BigDecimalUtil.roundBigDecimal(detail.getAmount()),
                        FinancesCurrencyType.P,
                        BigDecimal.ONE, detail.getProductItemCode(), detail.getQuantity()));
            }



            System.out.println("--------> * " + voucherForGeneration.getTransactionNumber());

            //voucherService.create(voucherForGeneration);
            warehouseVoucher.setVoucher(voucherForGeneration);
            voucherAccoutingService.saveVoucher(voucherForGeneration);
        }
    }

    private void createAccountEntryForOutputsFromProductDelivery(WarehouseVoucher warehouseVoucher,
                                                                 BusinessUnit executorUnit,
                                                                 String costCenterCode,
                                                                 String gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {
        //todo:muy importante restablecer el cambio luego
        //Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.ORDER_VOUCHER_FORM, gloss);
        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.ORDER_VOUCHER_FORM, gloss);
        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        List<MovementDetail> movementDetails = movementDetailService.findDetailByVoucherAndType(warehouseVoucher, MovementDetailType.S);

        BigDecimal total = BigDecimal.ZERO;
        for (MovementDetail movementDetail : movementDetails) {
            if (!movementDetail.getProductItem().getControlValued()) {
                log.debug("The movement detail with id=" +
                        movementDetail.getId() +
                        " was skipped because his product item does not have the controlValue field enabled.");
                continue;
            }

            if (BigDecimalUtil.isZeroOrNull(movementDetail.getAmount())) {
                log.debug("The movement detail with id=" +
                        movementDetail.getId() +
                        " was skipped because his amount its equal to zero.");
                continue;
            }

            BigDecimal detailAmount = BigDecimalUtil.roundBigDecimal(movementDetail.getAmount());
            total = BigDecimalUtil.sum(total, detailAmount);
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    movementDetail.getProductItemCashAccount(),
                    detailAmount,
                    movementDetail.getProductItemCashAccount().getCurrency(),
                    financesExchangeRateService.getExchangeRateByCurrencyType(movementDetail.getProductItemCashAccount().getCurrency(), BigDecimal.ONE)));
        }

        if (BigDecimalUtil.isPositive(total)) {

            System.out.println(">>>>>>>>>>>>>>>>>>>>> CUENTA CONTABLE: " + companyConfiguration.getWarehouseNationalCurrencyTransientAccount2().getFullName());
            voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());
            voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    executorUnit.getExecutorUnitCode(),
                    costCenterCode,
                    cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                    //companyConfiguration.getWarehouseNationalCurrencyTransientAccount2(),
                    total,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));
            voucherForGeneration.setDate(warehouseVoucher.getDate());
            //check transactionNumber
            voucherService.create(voucherForGeneration);
        }
    }

    private void createAccountEntryForInputsAndContraAccount(WarehouseVoucher warehouseVoucher,
                                                             CashAccount contraAccount,
                                                             BusinessUnit executorUnit,
                                                             String costCenterCode,
                                                             String gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                contraAccount,
                voucherAmount,
                contraAccount.getCurrency(),
                financesExchangeRateService.getExchangeRateByCurrencyType(contraAccount.getCurrency(), BigDecimal.ONE)));
        //check transactionNumber
        //voucherService.create(voucherForGeneration);
        warehouseVoucher.setVoucher(voucherForGeneration);
        voucherAccoutingService.saveVoucher(voucherForGeneration);
    }

    private void createAccountEntryForOutputsAndContraAccount(WarehouseVoucher warehouseVoucher,
                                                              CashAccount contraAccount,
                                                              BusinessUnit executorUnit,
                                                              String costCenterCode,
                                                              String gloss)
            throws CompanyConfigurationNotFoundException,
            FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {

        CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        BigDecimal voucherAmount = movementDetailService.sumWarehouseVoucherMovementDetailAmount(warehouseVoucher.getId().getCompanyNumber(), warehouseVoucher.getState(), warehouseVoucher.getId().getTransactionNumber());

        Voucher voucherForGeneration = VoucherBuilder.newGeneralVoucher(Constants.WAREHOUSE_VOUCHER_FORM, gloss);
        voucherForGeneration.setUserNumber(companyConfigurationService.findDefaultAccountancyUserNumber());

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                contraAccount,
                voucherAmount,
                contraAccount.getCurrency(),
                financesExchangeRateService.getExchangeRateByCurrencyType(contraAccount.getCurrency(), BigDecimal.ONE)));

        voucherForGeneration.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                executorUnit.getExecutorUnitCode(),
                costCenterCode,
                //companyConfiguration.getWarehouseNationalCurrencyAccount(),
                cashAccountService.findByAccountCode(warehouseVoucher.getWarehouse().getCashAccount()),
                voucherAmount,
                FinancesCurrencyType.P,
                BigDecimal.ONE));
        //check transactionNumber
        //voucherService.create(voucherForGeneration);
        voucherAccoutingService.saveVoucher(voucherForGeneration);

    }

    @SuppressWarnings(value = "unchecked")
    private boolean existsControlValuedProductsItems(WarehouseVoucher warehouseVoucher) {
        List<MovementDetail> movementDetails = getEntityManager().createNamedQuery("MovementDetail.findByTransactionNumber").
                setParameter("companyNumber", warehouseVoucher.getId().getCompanyNumber()).
                setParameter("transactionNumber", warehouseVoucher.getId().getTransactionNumber()).getResultList();
        for (MovementDetail movementDetail : movementDetails) {
            if (movementDetail.getProductItem().getControlValued()) {
                return true;
            }
        }

        return false;
    }

    public List<WarehouseVoucher> getVouchersWithoutAccounting(Date startDate, Date endDate){

        List<WarehouseVoucher> warehouseVoucherList = em.createQuery("" +
                "select voucher from WarehouseVoucher voucher " +
                " where voucher.operation in (:TP, :BA, :DE) " +
                " and voucher.date between :startDate and :endDate " +
                " and voucher.state =:state " +
                " and voucher.voucher is null ")
                .setParameter("TP", VoucherOperation.TP)
                .setParameter("BA", VoucherOperation.BA)
                .setParameter("DE", VoucherOperation.DE)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("state", WarehouseVoucherState.APR)
                .getResultList();

        return warehouseVoucherList;
    }


}
