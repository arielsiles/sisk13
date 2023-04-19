package com.encens.khipus.action.accounting;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.CustomerOrderTypeEnum;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.UserCashBoxService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.SalaryMovementProducerService;
import com.encens.khipus.util.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("accountingCreditSaleAction")
@Scope(ScopeType.CONVERSATION)
public class AccountingCreditSaleAction extends GenericAction {

    private Date accountingDate = new Date();

    @In
    private UserService userService;
    @In
    private User currentUser;
    @In
    private SaleService saleService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    protected FacesMessages facesMessages;
    @In
    private UserCashBoxService userCashBoxService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private SalaryMovementProducerService salaryMovementProducerService;
    @In
    private CashAccountService cashAccountService;


    @End
    public String accountingCreditSales(){
        User user = getUser(currentUser.getId());

        generateDiscountForCreditSales(); // Generando descuentos en acopio por ventas a credito (Veterinaria, Lacteos)

        List<CustomerOrder> customerOrderList = saleService.getPendingCustomerOrderList(accountingDate);

        for (CustomerOrder customerOrder : customerOrderList){
            System.out.println(">>-->>>-->>>---> Ventas a credito: " + DateUtils.format(accountingDate, "dd/MM/yyyy") + " - " + customerOrder.getCode() + " - " + customerOrder.getTotalAmount());

            /** Venta sin facturacion **/
            if (customerOrder.getMovement() == null){
                /** Reposiciones, degustación y refrigerios **/
                if (    (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REPLACEMENT)   ||
                        customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.TASTING)        ||
                        customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REFRESHMENT)    ||
                        customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.TRANSFER))  ){

                    customerOrder.setState(SaleStatus.CONTABILIZADO); /** Solo se fija el estado, se contabiliza a fin de periodo, CV **/

                }else {
                    Voucher voucher = accountingCreditSaleWithoutInvoice(customerOrder);
                    customerOrder.setVoucher(voucher);
                }

            }else { /** Ventas facturadas **/

                /** Ventas con porcentaje de comision (Eg. Sedem) **/
                if (customerOrder.getAdditionalDiscountValue().doubleValue() > 0){
                    Voucher voucher = accountingCreditSaleWithCommission(customerOrder);
                    customerOrder.setVoucher(voucher);
                }

                /** Ventas normales facturadas **/
                if ((customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.NORMAL) ||
                     customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.MILK)   ||
                     customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.VETERINARY) ||
                     customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REGULARIZE)) && customerOrder.getAdditionalDiscountValue().doubleValue() == 0 ){

                    if (customerOrder.getMovement() !=  null){
                        Voucher voucher = accountingCreditSale(customerOrder);
                        customerOrder.setVoucher(voucher);
                    }
                }
            }
            customerOrder.setState(SaleStatus.CONTABILIZADO);
            saleService.updateCustomerOrder(customerOrder);
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "CustomerOrder.info.accountingSaleProcessSucceed");
        return Outcome.SUCCESS;
    }

    private void generateDiscountForCreditSales(){

        User user = getUser(currentUser.getId());
        List<CustomerOrder> customerOrderList = saleService.getPendingCustomerOrderList(accountingDate, CustomerOrderTypeEnum.MILK);

        for (CustomerOrder customerOrder : customerOrderList){
            salaryMovementProducerService.createSalaryMovementProducer(customerOrder);
        }

        List<CustomerOrder> customerOrderList2 = saleService.getPendingCustomerOrderList(accountingDate, CustomerOrderTypeEnum.VETERINARY);
        for (CustomerOrder customerOrder : customerOrderList2){
            salaryMovementProducerService.createSalaryMovementProducer(customerOrder);
        }

    }


    private Voucher accountingCreditSaleWithCommission(CustomerOrder customerOrder){

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        CashBox cashBox = userCashBoxService.findByUser(currentUser);

        String invoiceGloss = " ";
        // Comment, uncomment para fijar factura en la glosa
        if (customerOrder.getMovement() != null)
            invoiceGloss = invoiceGloss + "(F-" + customerOrder.getMovement().getNumber() + ") ";

        Voucher voucher = VoucherBuilder.newGeneralVoucher( null,
                MessageUtils.getMessage("Voucher.creditSale.gloss") + " " + customerOrder.getCode() + invoiceGloss + customerOrder.getClient().getFullName());


        /** For creditPrimarySaleProduct **/
        //BigDecimal saleProductIVAValue = BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), companyConfiguration.getIvaTaxValue());
        //BigDecimal saleProductValue = BigDecimalUtil.subtract(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), saleProductIVAValue);

        //BigDecimal commissionValue_ = BigDecimalUtil.toBigDecimal(customerOrder.getCommissionValue());
        //BigDecimal commissionIVA_ = BigDecimalUtil.multiply(commissionValue, companyConfiguration.getIvaTaxValue());

        BigDecimal discountValue = BigDecimalUtil.sum(customerOrder.getProductDiscountValue(), customerOrder.getAdditionalDiscountValue());
        BigDecimal subTotalValue = BigDecimalUtil.sum(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), discountValue);

        BigDecimal subTotalIvaValue = BigDecimalUtil.multiply(subTotalValue, companyConfiguration.getIvaTaxValue());

        BigDecimal discountIVA = BigDecimalUtil.multiply(discountValue, companyConfiguration.getIvaTaxValue()); // 13% discount
        BigDecimal netCommissionValue = BigDecimalUtil.subtract(discountValue, discountIVA);                    // 87% discount


        BigDecimal itTaxValue = BigDecimalUtil.multiply(subTotalValue, companyConfiguration.getItTaxValue());
        BigDecimal receivableValue = BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount());


        BigDecimal debFiscalValue = BigDecimalUtil.multiply(receivableValue, companyConfiguration.getIvaTaxValue());

        BigDecimal totalDebit = BigDecimalUtil.sum(receivableValue, netCommissionValue, discountIVA, itTaxValue);
        BigDecimal saleProductTotal = BigDecimalUtil.subtract(totalDebit, debFiscalValue, itTaxValue, discountIVA);

        /** Debits **/
        VoucherDetail debitReceivable = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                cashBox.getType().getCashAccountReceivable(), receivableValue, FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail debitCommision = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                companyConfiguration.getCommissionSalesCashAccount(), netCommissionValue, FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail debitFiscalCreditIVACommision = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(), discountIVA, FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail debitTaxTransaction = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                companyConfiguration.getNationalCurrencyVATFiscalCreditTransientAccount(), itTaxValue, FinancesCurrencyType.D, BigDecimal.ONE);

        /** Credits **/
        VoucherDetail creditPrimarySaleProduct = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                cashBox.getType().getCashAccountIncome(), saleProductTotal, FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditDebitFiscal = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getFiscalDebitLiability(), debFiscalValue, FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditItTaxForPaying = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxPayable(), itTaxValue, FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditFiscalCreditIVACommision = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(), discountIVA, FinancesCurrencyType.D, BigDecimal.ONE);


        debitReceivable.setClient(customerOrder.getClient());
        creditDebitFiscal.setMovement(customerOrder.getMovement());
        voucher.setDocumentType(Constants.NE_VOUCHER_DOCTYPE);
        voucher.getDetails().add(debitReceivable);
        voucher.getDetails().add(debitCommision);
        voucher.getDetails().add(debitFiscalCreditIVACommision);
        voucher.getDetails().add(debitTaxTransaction);
        voucher.getDetails().add(creditPrimarySaleProduct);
        voucher.getDetails().add(creditDebitFiscal);
        voucher.getDetails().add(creditItTaxForPaying);
        voucher.getDetails().add(creditFiscalCreditIVACommision);

        if (customerOrder.getMovement() != null){
            voucher.setInvoiceNumber(customerOrder.getMovement().getNumber());
            voucher.setMovement(customerOrder.getMovement());
        }

        voucherAccoutingService.saveVoucher(voucher);

        return voucher;
    }


    /**
     * Contabiliza ventas a credito normales
     * @param customerOrder
     * @return
     */
    private Voucher accountingCreditSale(CustomerOrder customerOrder){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        //CashBox cashBox = userCashBoxService.findByUser(currentUser);
        CashBox cashBox = userCashBoxService.findByUser(customerOrder.getUser()); /**  **/

        Voucher voucher = VoucherBuilder.newGeneralVoucher( null, MessageUtils.getMessage("Voucher.creditSale.gloss") + " " +
                customerOrder.getCode() + " " + customerOrder.getClient().getFullName());

        VoucherDetail debitReceivable = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                cashBox.getType().getCashAccountReceivable(), BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail debitTransactionTax = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxExpense(),
                BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), Constants.IT_RETENTION_B), FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditTransactionTax = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getTransactionTaxPayable(), debitTransactionTax.getDebit(), FinancesCurrencyType.D, BigDecimal.ONE);

        VoucherDetail creditFiscalDebitIVA = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                companyConfiguration.getFiscalDebitLiability(), BigDecimalUtil.toBigDecimal(customerOrder.getTax()), FinancesCurrencyType.D, BigDecimal.ONE);

        BigDecimal amount = BigDecimalUtil.sum(debitReceivable.getDebit(), debitTransactionTax.getDebit());
        amount = BigDecimalUtil.subtract(amount, creditTransactionTax.getCredit(), creditFiscalDebitIVA.getCredit());

        VoucherDetail creditPrimarySaleProduct = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                cashBox.getType().getCashAccountIncome(), amount, FinancesCurrencyType.D, BigDecimal.ONE);

        debitReceivable.setClient(customerOrder.getClient());

        if (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REGULARIZE)){
            CashAccount cashAccountRegularize = cashAccountService.findByAccountCode(customerOrder.getClient().getRegularizeAccount());
            debitReceivable = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                    cashAccountRegularize, BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), FinancesCurrencyType.D, BigDecimal.ONE);
            debitReceivable.setClient(null);
        }

        creditFiscalDebitIVA.setMovement(customerOrder.getMovement());
        voucher.setDocumentType(Constants.NE_VOUCHER_DOCTYPE);
        voucher.getDetails().add(debitReceivable);
        voucher.getDetails().add(debitTransactionTax);
        voucher.getDetails().add(creditTransactionTax);
        voucher.getDetails().add(creditFiscalDebitIVA);
        voucher.getDetails().add(creditPrimarySaleProduct);

        if (customerOrder.getMovement() != null){
            voucher.setInvoiceNumber(customerOrder.getMovement().getNumber());
            voucher.setMovement(customerOrder.getMovement());
        }

        voucherAccoutingService.saveVoucher(voucher);

        return voucher;
    }

    private Voucher accountingCreditSaleWithoutInvoice(CustomerOrder customerOrder){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        //CashBox cashBox = userCashBoxService.findByUser(currentUser);
        CashBox cashBox = userCashBoxService.findByUser(customerOrder.getUser());

                Voucher voucher = VoucherBuilder.newGeneralVoucher( null, MessageUtils.getMessage("Voucher.creditSale.gloss") + " " +
                customerOrder.getCode() + " " + customerOrder.getClient().getFullName());

        System.out.println("--------> cashBox.getType().getCashAccountReceivable(): " + cashBox.getType().getCashAccountReceivable());
        System.out.println("--------> customerOrder.getTotalAmount(): " + customerOrder.getTotalAmount());

        VoucherDetail debitReceivable = VoucherDetailBuilder.newDebitVoucherDetail(null, null,
                cashBox.getType().getCashAccountReceivable(), BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), FinancesCurrencyType.D, BigDecimal.ONE);

        BigDecimal amount = BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount());

        VoucherDetail creditPrimarySaleProduct = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                cashBox.getType().getCashAccountIncome(), amount, FinancesCurrencyType.D, BigDecimal.ONE);

        debitReceivable.setClient(customerOrder.getClient());

        if (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REGULARIZE)){
            CashAccount cashAccountRegularize = cashAccountService.findByAccountCode(customerOrder.getClient().getRegularizeAccount());
            creditPrimarySaleProduct = VoucherDetailBuilder.newCreditVoucherDetail(null, null,
                    cashAccountRegularize, amount, FinancesCurrencyType.D, BigDecimal.ONE);
        }

        voucher.setDocumentType(Constants.NE_VOUCHER_DOCTYPE);
        voucher.getDetails().add(debitReceivable);
        voucher.getDetails().add(creditPrimarySaleProduct);

        voucherAccoutingService.saveVoucher(voucher);

        return voucher;
    }

    private User getUser(Long id) {
        try {
            return userService.findById(User.class, id);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    public Date getAccountingDate() {
        return accountingDate;
    }

    public void setAccountingDate(Date accountingDate) {
        this.accountingDate = accountingDate;
    }
}
