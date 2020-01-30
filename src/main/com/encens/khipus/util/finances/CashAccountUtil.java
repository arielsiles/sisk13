package com.encens.khipus.util.finances;

import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * FinancesUtil
 *
 * @author
 * @version 2.2
 */
public final class CashAccountUtil {
    private CashAccountUtil() {
    }

    /**
     *
     * @param cashAccount
     * @param amount
     * @param debit
     * @param credit
     * @param exchangeRate ex: 6.96 | 0.00
     * @return
     */
    public static VoucherDetail createVoucherDetail(CashAccount cashAccount, BigDecimal amount, FinancesCurrencyType currencyTypeAmount, Boolean debit, Boolean credit, BigDecimal exchangeRate){
        VoucherDetail voucherDetail = new VoucherDetail();

        voucherDetail.setCashAccount(cashAccount);
        voucherDetail.setAccount(cashAccount.getAccountCode());

        /** Monto:Bs - Cta:Bs **/
        if (currencyTypeAmount.equals(FinancesCurrencyType.P) && cashAccount.getCurrency().equals(FinancesCurrencyType.P)){
            if (debit){
                voucherDetail.setDebit(amount);
                voucherDetail.setCredit(BigDecimal.ZERO);
                voucherDetail.setExchangeAmount(exchangeRate);
                voucherDetail.setDebitMe(BigDecimalUtil.multiply(voucherDetail.getDebit(), exchangeRate));
                voucherDetail.setCreditMe(BigDecimalUtil.multiply(voucherDetail.getDebit(), exchangeRate));
            }
            if (credit){
                voucherDetail.setDebit(BigDecimal.ZERO);
                voucherDetail.setCredit(amount);
                voucherDetail.setExchangeAmount(exchangeRate);
                voucherDetail.setDebitMe(BigDecimalUtil.multiply(voucherDetail.getDebit(), exchangeRate));
                voucherDetail.setCreditMe(BigDecimalUtil.multiply(voucherDetail.getDebit(), exchangeRate));
            }
        }

        /** Bs - $us **/
        if ( currencyTypeAmount.equals(FinancesCurrencyType.P)  && (cashAccount.getCurrency().equals(FinancesCurrencyType.D) || cashAccount.getCurrency().equals(FinancesCurrencyType.M))){
            if (debit){
                voucherDetail.setDebit(amount);
                voucherDetail.setCredit(BigDecimal.ZERO);
                voucherDetail.setExchangeAmount(exchangeRate);
                voucherDetail.setDebitMe(BigDecimalUtil.divide(amount, exchangeRate));
                voucherDetail.setCreditMe(BigDecimal.ZERO);
            }
            if (credit){
                voucherDetail.setDebit(BigDecimal.ZERO);
                voucherDetail.setCredit(amount);
                voucherDetail.setExchangeAmount(exchangeRate);
                voucherDetail.setDebitMe(BigDecimal.ZERO);
                voucherDetail.setCreditMe(BigDecimalUtil.divide(amount, exchangeRate));
            }
        }

        /** $us - $us **/
        if ( currencyTypeAmount.equals(FinancesCurrencyType.D)  && (cashAccount.getCurrency().equals(FinancesCurrencyType.D) || cashAccount.getCurrency().equals(FinancesCurrencyType.M))){
            if (debit){
                voucherDetail.setDebit(BigDecimalUtil.multiply(amount, exchangeRate));
                voucherDetail.setCredit(BigDecimal.ZERO);
                voucherDetail.setExchangeAmount(exchangeRate);
                voucherDetail.setDebitMe(amount);
                voucherDetail.setCreditMe(BigDecimal.ZERO);
            }
            if (credit){
                voucherDetail.setDebit(BigDecimal.ZERO);
                voucherDetail.setCredit(BigDecimalUtil.multiply(amount, exchangeRate));
                voucherDetail.setExchangeAmount(exchangeRate);
                voucherDetail.setDebitMe(BigDecimal.ZERO);
                voucherDetail.setCreditMe(amount);
            }
        }

        /** $us - Bs **/
        if (currencyTypeAmount.equals(FinancesCurrencyType.D)  && cashAccount.getCurrency().equals(FinancesCurrencyType.P)){
            if (debit){
                voucherDetail.setDebit(BigDecimalUtil.divide(amount, exchangeRate));
                voucherDetail.setCredit(BigDecimal.ZERO);
                voucherDetail.setExchangeAmount(BigDecimal.ZERO);
                voucherDetail.setDebitMe(BigDecimal.ZERO);
                voucherDetail.setCreditMe(BigDecimal.ZERO);
            }
            if (credit){
                voucherDetail.setDebit(BigDecimal.ZERO);
                voucherDetail.setCredit(BigDecimalUtil.divide(amount, exchangeRate));
                voucherDetail.setExchangeAmount(BigDecimal.ZERO);
                voucherDetail.setDebitMe(BigDecimal.ZERO);
                voucherDetail.setCreditMe(BigDecimal.ZERO);
            }
        }

        return  voucherDetail;
    }
}
