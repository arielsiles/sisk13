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
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean existsAccountCode(String code, Long excludedId) {
        if (code == null || code.trim().length() == 0) {
            return false;
        }
        /** Sin parametros nulos en el where: Hibernate 3 los resuelve de forma despareja. */
        String jpql = "select count(account) from Account account where account.code = :code";
        if (excludedId != null) {
            jpql += " and account.id <> :excludedId";
        }
        Query query = em.createQuery(jpql).setParameter("code", code.trim());
        if (excludedId != null) {
            query.setParameter("excludedId", excludedId);
        }
        Long count = (Long) query.getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Incremento atomico de una serie de codigos de DPF.
     * <p/>
     * No pasa por SequenceGeneratorService a proposito. Ese generador lee, suma y graba
     * desde Java, y confia en un reintento por OptimisticLockException que aca no puede
     * dispararse: ni <code>gensecuencia</code> ni la entidad Sequence tienen version. Dos
     * altas simultaneas podrian leer el mismo valor y llevarse el mismo codigo.
     * <p/>
     * El <code>valor = valor + 1</code> resuelve el incremento dentro del motor y deja
     * tomado el candado de fila de InnoDB hasta que la transaccion del alta confirma: la
     * segunda espera y se lleva el numero siguiente. Si el alta se cae, el incremento se
     * deshace con ella y no queda un hueco en la numeracion.
     * <p/>
     * Tampoco se arregla el generador compartido: lo usan compras, almacenes, activos fijos
     * y produccion, y este codigo no necesita meterse en ese camino.
     */
    public long nextAccountCodeNumber(String sequenceName) {
        int updated = em.createNativeQuery(
                "update gensecuencia set valor = valor + 1 where nombre = :sequenceName")
                .setParameter("sequenceName", sequenceName)
                .executeUpdate();
        if (updated == 0) {
            return 0;
        }
        Number value = (Number) em.createNativeQuery(
                "select valor from gensecuencia where nombre = :sequenceName")
                .setParameter("sequenceName", sequenceName)
                .getSingleResult();
        return value != null ? value.longValue() : 0;
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

    public List<Account> getSavingsAccountsByPeriod(SavingType savingType, Date startDate, Date endDate) {
        /**
         * No se filtra por capital: una cuenta con capital cero es un dato a corregir y
         * tiene que salir a la luz, no desaparecer del calculo en silencio.
         * <p/>
         * Quedan afuera las ANNULLED, que nunca fueron una operacion real, y las PENDING,
         * que todavia no se aprobaron: su capital contable es cero legitimamente y sin
         * excluirlas apareceria una fila en cero todos los meses, justo la senal que este
         * calculo reserva para los datos que hay que corregir.
         */
        return (List<Account>) em.createQuery("select account from Account account " +
                " where account.accountType.savingType =:savingType" +
                "   and account.accountState not in (:excludedStates)" +
                "   and account.openingDate <=:endDate" +
                "   and account.expirationDate >=:startDate" +
                " order by account.currency, account.code")
                .setParameter("savingType", savingType)
                .setParameter("excludedStates", Arrays.asList(AccountState.ANNULLED, AccountState.PENDING))
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .getResultList();
    }

    public List<Object[]> getAccountLedgerMovements(List<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return new ArrayList<Object[]>();
        }
        return (List<Object[]>) em.createQuery("select voucherDetail.partnerAccount.id," +
                "       voucherDetail.voucher.date," +
                "       voucherDetail.debit, voucherDetail.credit," +
                "       voucherDetail.debitMe, voucherDetail.creditMe" +
                "  from VoucherDetail voucherDetail" +
                " where voucherDetail.partnerAccount.id in (:accountIds)" +
                "   and voucherDetail.voucher.state <> 'ANL'")
                .setParameter("accountIds", accountIds)
                .getResultList();
    }

    @Override
    public List<Account> getAccountList(Partner partner) {
        List<Account> accountList = new ArrayList<Account>();

        /**
         * Alimenta el selector de cuentas destino de transferencias: no van las anuladas
         * ni los DPF pendientes de aprobacion, cuya apertura todavia no se contabilizo.
         */
        accountList = (List<Account>) em.createQuery("select account from Account account" +
                " where account.partner =:partner " +
                "   and account.accountState not in (:excludedStates) ")
                .setParameter("partner", partner)
                .setParameter("excludedStates", Arrays.asList(AccountState.ANNULLED, AccountState.PENDING))
                .getResultList();

        return accountList;
    }

}
