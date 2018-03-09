package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.CashAccount;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * CashAccountServiceBean
 *
 * @author
 * @version 2.0
 */
@Name("cashAccountService")
@Stateless
@AutoCreate
public class CashAccountServiceBean implements CashAccountService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public Boolean existsAccount(String accountCode) {
        try {
            return em.createNamedQuery("CashAccount.findByAccountCode")
                    .setParameter("accountCode", accountCode).getSingleResult() != null;
        } catch (EntityNotFoundException e) {
        }
        return false;
    }

    public CashAccount findByAccountCode(String accountCode) {
        try {
            return (CashAccount) em.createNamedQuery("CashAccount.findByAccountCode")
                    .setParameter("accountCode", accountCode).getSingleResult();
        } catch (EntityNotFoundException e) {
        }
        return null;
    }

    public Boolean isActiveAccount(String accountCode) {
        try {
            return em.createNamedQuery("CashAccount.findByActiveAccount")
                    .setParameter("accountCode", accountCode)
                    .setParameter("active", Boolean.TRUE).getSingleResult() != null;
        } catch (EntityNotFoundException e) {
        }
        return false;
    }

    public List<CashAccount> findCashAccountList(){
        try {
            return em.createNamedQuery("CashAccount.findCashAccountList").getResultList();
        } catch (NoResultException e) {
            return new ArrayList<CashAccount>();
        }
    }

}
