package com.encens.khipus.service.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditTransaction;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.finances.Voucher;

import java.util.Date;

/**
 * Credit transaction services interface
 *
 * @author:
 */
public interface CreditTransactionService {
    void create(Credit credit, Invoice invoice) throws EntryDuplicatedException;
    Date findLastPayment(Credit credit);
    Date findLastPaymentForInterest(Credit credit);
    void createCreditTransactionPayout(Credit credit, CreditTransaction creditTransaction);
    public void updateTransaction(CreditTransaction creditTransaction, Voucher voucher);
}
