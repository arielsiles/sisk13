package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Account;
import com.encens.khipus.model.customers.AccountState;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.customers.SavingType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.VoucherDetail;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Credit service implementation class
 *
 * @author
 */
@Stateless
@Name("accountService")
@AutoCreate
public class AccountServiceBean implements AccountService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public void createAccount(Account account){
        em.persist(account);
        em.flush();
    }

    public void updateAccount(Account account){
        em.merge(account);
        em.flush();
    }

    @Override
    public List<VoucherDetail> getAccountDetailList(Account account){

        List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();

        try {
            voucherDetails = (List<VoucherDetail>) em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " where voucherDetail.partnerAccount = :account " +
                    " and voucherDetail.voucher.state <> 'ANL' " +
                    " order by voucherDetail.voucher.date asc ")
                    .setParameter("account", account)
                    .getResultList();
            
        }catch (NoResultException e){
            return null;
        }
        return voucherDetails;
    }

    public List<VoucherDetail> getPartnerDetailList(Partner partner){

        List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();
        try {
            voucherDetails = (List<VoucherDetail>) em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " where voucherDetail.partner =:partner " +
                    " and voucherDetail.account =:cashAccountCode " +
                    " and voucherDetail.voucher.state <> 'ANL' " +
                    " order by voucherDetail.voucher.date asc ")
                    .setParameter("partner", partner)
                    .setParameter("cashAccountCode", "3110100000")
                    .getResultList();

        }catch (NoResultException e){
            return null;
        }
        return voucherDetails;
    }

    public BigDecimal calculateAccountBalance(Account account, Date startDate, Date endDate){
        BigDecimal result = BigDecimal.ZERO;
        String cashAccount = account.getAccountType().getCashAccountMn().getAccountCode();

        if (account.getCurrency().equals(FinancesCurrencyType.D))
            cashAccount = account.getAccountType().getCashAccountMe().getAccountCode();
        if (account.getCurrency().equals(FinancesCurrencyType.M))
            cashAccount = account.getAccountType().getCashAccountMv().getAccountCode();

        if (account.getCurrency().equals(FinancesCurrencyType.P)) {
            result = (BigDecimal) em.createQuery("select sum(voucherDetail.credit) - sum(voucherDetail.debit) from VoucherDetail voucherDetail " +
                    " where voucherDetail.partnerAccount =:account " +
                    " and voucherDetail.voucher.date between :startDate and :endDate " +
                    " and voucherDetail.account =:cashAccount " +
                    " and voucherDetail.voucher.state <> :state")
                    .setParameter("account", account)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("cashAccount", cashAccount)
                    .setParameter("state", "ANL")
                    .getSingleResult();
        }

        if (account.getCurrency().equals(FinancesCurrencyType.D) || account.getCurrency().equals(FinancesCurrencyType.M)){
            result = (BigDecimal) em.createQuery("select sum(voucherDetail.creditMe) - sum(voucherDetail.debitMe) from VoucherDetail voucherDetail " +
                    " where voucherDetail.partnerAccount =:account " +
                    " and voucherDetail.voucher.date between :startDate and :endDate " +
                    " and voucherDetail.account =:cashAccount " +
                    " and voucherDetail.voucher.state <> :state")
                    .setParameter("account", account)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("cashAccount", cashAccount)
                    .setParameter("state", "ANL")
                    .getSingleResult();
        }

        if (result == null) result = BigDecimal.ZERO;

        return result;
    }

    public List<VoucherDetail> getMovementAccountBetweenDates(Account account, Date startDate, Date endDate){

        List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();
        try {
            voucherDetails = (List<VoucherDetail>) em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " where voucherDetail.partnerAccount = :account " +
                    " and voucherDetail.voucher.date between :startDate and :endDate " +
                    " and voucherDetail.voucher.state <> :state " +
                    " order by voucherDetail.voucher.date asc ")
                    .setParameter("account", account)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("state", "ANL")
                    .getResultList();
        }catch (NoResultException e){
            return null;
        }
        return voucherDetails;
    }

    public List<Account> getAccountList(){
        List<Account> accountList = new ArrayList<Account>();
        accountList = (List<Account>) em.createQuery("select account from Account account " +
                " where account.accountState = :state" +
                " and account.id = 2065 ")
                .setParameter("state", AccountState.ACTIVE)
                .getResultList();
        return accountList;
    }

    public List<Account> getSavingsAccounts(SavingType savingType){
        List<Account> accountList = new ArrayList<Account>();
        accountList = (List<Account>) em.createQuery("select account from Account account " +
                " where account.accountState =:state" +
                " and account.accountType.savingType =:savingType"
                /*+ " and account.id in (2061) "*/ )
                .setParameter("state", AccountState.ACTIVE)
                .setParameter("savingType", savingType)
                .getResultList();
        return accountList;
    }

    public List<Account> getSavingsAccounts(SavingType savingType, FinancesCurrencyType currencyType){
        List<Account> accountList = new ArrayList<Account>();
        accountList = (List<Account>) em.createQuery("select account from Account account " +
                " where account.accountState =:state" +
                " and account.currency =:currency" +
                " and account.accountType.savingType =:savingType" /*+" and account.id in (2065, 2069) "*/)
                .setParameter("state", AccountState.ACTIVE)
                .setParameter("currency", currencyType)
                .setParameter("savingType", savingType)
                .getResultList();
        return accountList;
    }

    @Override
    public List<Account> getAccountList(Partner partner) {
        List<Account> accountList = new ArrayList<Account>();

        accountList = (List<Account>) em.createQuery("select account from Account account" +
                " where account.partner =:partner ")
                .setParameter("partner", partner)
                .getResultList();

        return accountList;
    }

}
