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
import com.encens.khipus.service.finances.UserCashBoxService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.*;
import org.jboss.seam.ScopeType;
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

    public String accountingCreditSales(){
        User user = getUser(currentUser.getId());
        List<CustomerOrder> customerOrderList = saleService.getCustomerOrderList(user, accountingDate);

        for (CustomerOrder customerOrder : customerOrderList){
            System.out.println(">>-->>>-->>>---> Ventas a credito: " + DateUtils.format(accountingDate, "dd/MM/yyyy") + " - " + customerOrder.getCode() + " - " + customerOrder.getTotalAmount());

            /** Ventas con porcentaje de comision (Eg. Sedem) **/
            if (customerOrder.getCommissionPercentage() > 0){

                Voucher voucher = accountingCreditSaleWithCommission(customerOrder);
                customerOrder.setVoucher(voucher);
            }

            /** Ventas normales **/
            if (    customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.NORMAL) &&
                    !customerOrder.getState().equals(SaleStatus.CONTABILIZADO)  &&
                    customerOrder.getCommissionPercentage() == 0

                    ){

                Voucher voucher = accountingCreditSale(customerOrder);
                customerOrder.setVoucher(voucher);
            }

            /** Reposiciones, degustación y refrigerios **/
            if (    (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REPLACEMENT)  ||
                     customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.TASTING)  ||
                     customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.REFRESHMENT)) &&
                    !customerOrder.getState().equals(SaleStatus.CONTABILIZADO)  ){

            }

            /** Descuentos: Lacteo, Veterinario **/
            if (    (customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.MILK)        ||
                     customerOrder.getCustomerOrderType().getType().equals(CustomerOrderTypeEnum.VETERINARY)) &&
                    !customerOrder.getState().equals(SaleStatus.CONTABILIZADO)  ){

                /** Todo: Realizar el descuento respectivo **/

            }

            customerOrder.setAccounted(Boolean.TRUE);
            customerOrder.setState(SaleStatus.CONTABILIZADO);
            saleService.updateCustomerOrder(customerOrder);
        }

        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "CustomerOrder.infofo.accountingSaleProcessSucceed");
        return Outcome.SUCCESS;
    }


    private Voucher accountingCreditSaleWithCommission(CustomerOrder customerOrder){

        return null;
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

        CashBox cashBox = userCashBoxService.findByUser(currentUser);

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
        creditFiscalDebitIVA.setMovement(customerOrder.getMovement());
        voucher.setDocumentType(Constants.NE_VOUCHER_DOCTYPE);
        voucher.getDetails().add(debitReceivable);
        voucher.getDetails().add(debitTransactionTax);
        voucher.getDetails().add(creditTransactionTax);
        voucher.getDetails().add(creditFiscalDebitIVA);
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
