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
        String expenseType = null;
        if (cashAccount.getExpenseType() != null) {
            expenseType = cashAccount.getExpenseType().toString();
        }

        String rootAccountCode = cashAccount.getRootCashAccount() != null ? cashAccount.getRootCashAccount().getAccountCode() : null;
        String level3AccountCode = cashAccount.getCashAccountLeve3() != null ? cashAccount.getCashAccountLeve3().getAccountCode() : null;

        try {
            em.createNativeQuery("insert into arcgms (cuenta, descri, cta_raiz, cta_niv3, cn_nivel, no_cia, tipo, tipo_gasto, activa, moneda, exije_cc, ind_mov, permiso_inv, permite_iva) " +
                    "values(:cuenta, :descri, :cta_raiz, :cta_niv3, :cn_nivel, :no_cia, :tipo, :tipo_gasto, :activa, :moneda, :exije_cc, :ind_mov, :permiso_inv, :permite_iva)")
                    .setParameter("cuenta", cashAccount.getAccountCode())
                    .setParameter("descri", cashAccount.getDescription())
                    .setParameter("cta_raiz", rootAccountCode)
                    .setParameter("cta_niv3", level3AccountCode)
                    .setParameter("cn_nivel", cashAccount.getAccountLevel())
                    .setParameter("no_cia", Constants.defaultCompanyNumber)
                    .setParameter("tipo", cashAccount.getAccountType().toString())
                    .setParameter("tipo_gasto", expenseType)
                    .setParameter("activa", 'S')
                    .setParameter("moneda", cashAccount.getCurrency().toString())
                    .setParameter("exije_cc", 'N')
                    .setParameter("ind_mov", cashAccount.getMovementAccount() ? 'S' : 'N')
                    .setParameter("permiso_inv", cashAccount.getHasWarehousePermission() ? 'S' : 'N')
                    .setParameter("permite_iva", 'N')
                    .executeUpdate();
            return Boolean.TRUE;
        } catch (Exception e){
            return Boolean.FALSE;
        }
    }

    @Override
    public List<CashAccount> findCashAccountsUtil() {
        List<CashAccount> result;
        try {
            result = em.createNamedQuery("CashAccount.findAccountsUtil")
                    .setParameter("util", Boolean.TRUE)
                    .getResultList();
        } catch (NoResultException e) {
            result = new ArrayList<CashAccount>();
        }

        for (CashAccount cashAccount : result) {
            System.out.println("===> Account util: " + cashAccount.getFullName());
        }


        return result;
    }

}
