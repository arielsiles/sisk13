package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.util.Constants;
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

    public List<CashAccount> findCashAccountListByType(CashAccountType accountType){
        try {
            return em.createNamedQuery("CashAccount.findCashAccountListByType")
                    .setParameter("accountType", accountType)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<CashAccount>();
        }
    }

    @Override
    public Boolean createCashAccount(CashAccount cashAccount) {

        cashAccount.setCompanyNumber(Constants.defaultCompanyNumber);

        try {
            em.createNativeQuery("insert into arcgms (cuenta, descri, cta_raiz, no_cia, tipo, activa, moneda, exije_cc, ind_mov) " +
                    "values(:cuenta, :descri, :cta_raiz, :no_cia, :tipo, :activa, :moneda, :exije_cc, :ind_mov)")
                    .setParameter("cuenta", cashAccount.getAccountCode())
                    .setParameter("descri", cashAccount.getDescription())
                    .setParameter("cta_raiz", cashAccount.getRootCashAccount().getAccountCode())
                    .setParameter("no_cia", Constants.defaultCompanyNumber)
                    .setParameter("tipo", cashAccount.getAccountType().toString())
                    .setParameter("activa", 'S')
                    .setParameter("moneda", cashAccount.getCurrency().toString())
                    .setParameter("exije_cc", 'N')
                    .setParameter("ind_mov", cashAccount.getMovementAccount() ? 'S' : 'N')
                    .executeUpdate();
            return Boolean.TRUE;
        } catch (Exception e){
            return Boolean.FALSE;
        }
    }

}
