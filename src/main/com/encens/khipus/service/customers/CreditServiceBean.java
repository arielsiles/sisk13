package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditTransaction;
import com.encens.khipus.model.customers.CreditTransactionType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Credit service implementation class
 *
 * @author
 */
@Stateless
@Name("creditService")
@AutoCreate
public class CreditServiceBean implements CreditService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public Credit findByEntity(Entity entity) {

        try {
            return (Credit) em.createNamedQuery("Credit.findByEntity")
                    .setParameter("entity", entity).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public BigDecimal getActualCreditBalance(Credit credit) {

        List<CreditTransaction> transactions = em.createNamedQuery("CreditTransaction.transactions")
                .setParameter("credit", credit).getResultList();
        BigDecimal totalTransactions = new BigDecimal(0.0);
        for (CreditTransaction transaction : transactions) {
            totalTransactions = totalTransactions.add(transaction.getAmount());
        }
        return credit.getAmount().subtract(totalTransactions);
    }

    public BigDecimal getTotalPaidCapital(Credit credit){

        List<CreditTransaction> transactions = em.createNamedQuery("CreditTransaction.findIncomePayments")
                .setParameter("credit", credit).setParameter("creditTransactionType", CreditTransactionType.ING).getResultList();

        BigDecimal totalPaidCapital = new BigDecimal(0.0);
        for (CreditTransaction transaction : transactions) {
            totalPaidCapital = totalPaidCapital.add(transaction.getCapital());
        }
        return totalPaidCapital;
    }

    public List<Credit> getAllCredits(){

        List<Credit> creditList = em.createNamedQuery("Credit.findAllCredits").getResultList();

        return creditList;
    }

    public void changeCreditState(Credit credit, CreditState state){
        System.out.println("------> Cambiando Estado de Credito a: " + state.getResourceKey());
        credit.setState(state);
        em.merge(credit);
        em.flush();

    }

}
