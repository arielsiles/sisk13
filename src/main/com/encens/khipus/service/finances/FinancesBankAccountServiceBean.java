package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.FinancesBankAccountPk;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * FinancesBankAccountServiceBean
 *
 * @author
 * @version 2.0
 */
@Name("financesBankAccountService")
@Stateless
@AutoCreate
public class FinancesBankAccountServiceBean implements FinancesBankAccountService {
    @In(value = "#{entityManager}")
    private EntityManager em;

    public List<FinancesBankAccount> findByCurrencyType(FinancesCurrencyType financesCurrencyType) {
        try {
            return em.createNamedQuery("FinancesBankAccount.findByCurrencyType")
                    .setParameter("currency", financesCurrencyType).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<FinancesBankAccount>();
    }

    public FinancesBankAccount findFinancesBankAccount(FinancesBankAccountPk financesBankAccountPk) {
        try {
            return (FinancesBankAccount) em.find(FinancesBankAccount.class, financesBankAccountPk);
        } catch (Exception e) {
        }
        return null;
    }
}
