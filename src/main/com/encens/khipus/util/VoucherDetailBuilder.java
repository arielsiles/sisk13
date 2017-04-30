package com.encens.khipus.util;

import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.VoucherDetail;

import java.math.BigDecimal;

/**
 * VoucherDetailBuilder
 *
 * @author
 * @version 2.0
 */
public class VoucherDetailBuilder {
    private VoucherDetailBuilder() {
    }

    public static VoucherDetail newDebitVoucherDetail(String businessUnitCode, String costCenterCode, CashAccount cashAccount,
                                                      BigDecimal amount, FinancesCurrencyType currency, BigDecimal exchangeAmount) {
        return new VoucherDetail(cashAccount.getHasCostCenter() ? businessUnitCode : null, cashAccount.getHasCostCenter() ? costCenterCode : null, cashAccount.getAccountCode(), amount, new BigDecimal(0), currency, exchangeAmount);
    }

    public static VoucherDetail newCreditVoucherDetail(String businessUnitCode, String costCenterCode, CashAccount cashAccount,
                                                       BigDecimal amount, FinancesCurrencyType currency, BigDecimal exchangeAmount) {
        return new VoucherDetail(cashAccount.getHasCostCenter() ? businessUnitCode : null, cashAccount.getHasCostCenter() ? costCenterCode : null, cashAccount.getAccountCode(), new BigDecimal(0), amount, currency, exchangeAmount);
    }

}
