package com.encens.khipus.service.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditTransaction;
import com.encens.khipus.model.customers.CreditTransactionType;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.finances.Voucher;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Credit transaction service implementation class
 *
 * @author
 */


@Stateless
@Name("creditTransactionService")
@AutoCreate
public class CreditTransactionServiceBean implements CreditTransactionService {


    @In(value = "#{entityManager}")
    private EntityManager em;

    public void create(Credit credit, Invoice invoice) throws EntryDuplicatedException {
        try {

            CreditTransaction transaction = new CreditTransaction();
            transaction.setCredit(credit);
            /*transaction.setInvoice(invoice);*/
            transaction.setAmount(invoice.getTotalDiscount() != null ? invoice.getTotalAmount().subtract(invoice.getTotalDiscount()) : invoice.getTotalAmount());
            em.persist(transaction);
            em.flush();

        } catch (EntityExistsException e) {
            throw new EntryDuplicatedException();
        }
    }

    @Override
    public Date findLastPayment(Credit credit) {
        Date result;
        try {
            result =  (Date) em.createNamedQuery("CreditTransaction.findLastTransaction")
                    .setParameter("credit", credit)
                    .setParameter("transactionType", CreditTransactionType.ING)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

        if (result == null)
            result = credit.getFirstPayment();

        return result;
    }

    @Override
    public Date findLastPaymentForInterest(Credit credit) {
        Date result;
        try {
            result =  (Date) em.createNamedQuery("CreditTransaction.findLastTransaction")
                    .setParameter("credit", credit)
                    .setParameter("transactionType", CreditTransactionType.ING)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

        if (result == null)
            result = credit.getGrantDate();

        return result;
    }

    public void createCreditTransactionPayout(Credit credit, CreditTransaction creditTransaction){

        String gloss = creditTransaction.getGloss() + " " + credit.getCode();
        creditTransaction.setCapital(BigDecimal.ZERO);
        creditTransaction.setInterest(BigDecimal.ZERO);
        creditTransaction.setAmount(credit.getAmount());
        creditTransaction.setGloss(gloss);
        //creditTransaction.setDate(new Date());
        creditTransaction.setDays(0);
        creditTransaction.setCapitalBalance(credit.getAmount());
        creditTransaction.setCreditTransactionType(CreditTransactionType.EGR);
        creditTransaction.setCredit(credit);
        credit.setDelivered(true);
        em.persist(creditTransaction);
        em.flush();

    }

    public void updateTransaction(CreditTransaction creditTransaction, Voucher voucher){
        System.out.println("---------> Relacionando Voucher, CreditTransaction.... idvoucher:" + voucher.getId());
        creditTransaction.setVoucher(voucher);
        creditTransaction.setVoucherId(voucher.getId());

        em.merge(creditTransaction);
        em.merge(voucher);
        em.flush();

    }

}
