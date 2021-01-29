package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.Date;
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

    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private CreditTransactionService creditTransactionService;

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

    public Credit findCreditById(Long creditId){

        return (Credit) em.createNamedQuery("Credit.findCreditById")
                .setParameter("creditId", creditId).getSingleResult();

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

    @Override
    public BigDecimal calculateTotalPaidCapital(Credit credit, Date date) {

        System.out.println("............-----> calculando capital pagado al .... : " + DateUtils.format(date, "dd/MM/yyyy"));
        BigDecimal result = BigDecimal.ZERO;

        List<CreditTransaction> transactions = em.createNamedQuery("CreditTransaction.transactionsBefore")
                .setParameter("credit", credit)
                .setParameter("date", date)
                .getResultList();

        for (CreditTransaction ct : transactions){
            result = BigDecimalUtil.sum(result, ct.getCapital());
        }

        System.out.println("--------....----> PAGADO: " + result);

        return result;
    }

    public List<Credit> getAllCredits(){

        List<Credit> creditList = em.createNamedQuery("Credit.findAllCredits").getResultList();

        return creditList;
    }

    public List<Credit> getUnfinishedCredits(){

        List<Credit> creditList = em.createNamedQuery("Credit.findUnfinishedCredits")
                .setParameter("creditStateFIN", CreditState.FIN)
                .getResultList();
        return creditList;
    }

    public List<Credit> getCredits(CreditState creditState, CreditType creditType){

        List<Credit> creditList = em.createNamedQuery("Credit.findCreditsByStateAndType")
                .setParameter("creditState", creditState)
                .setParameter("creditType", creditType)
                .getResultList();

        return creditList;
    }


    public void changeCreditState(Credit credit, CreditState state){
        System.out.println("------> Cambiando Estado de Credito a: " + state.getResourceKey());
        credit.setState(state);
        em.merge(credit);
        em.flush();

    }

    public List<Credit> getCreditList(Partner partner){
        List<Credit> creditList = em.createQuery("select c from Credit c where c.partner =:partner")
                .setParameter("partner", partner)
                .getResultList();
        return  creditList;
    }

    @Override
    public void updateCredit(Credit credit) {
        em.merge(credit);
        em.flush();
    }

    public Object[] getAmountNewCredits(Long productiveZoneId, Date startDate, Date endDate){

        Object[] result = {0, BigDecimal.ZERO}; //Por defecto, si no existe creditos para el GAB. [Cant, Monto]
        List<Object[]> resultList = em.createQuery(
                " SELECT " +
                        " productiveZone.id," +
                        " count(credit.id)," +
                        " sum(credit.amount)" +
                        " FROM Credit credit " +
                        " LEFT JOIN credit.partner partner " +
                        " LEFT JOIN partner.productiveZone productiveZone" +
                        " WHERE credit.grantDate between :startDate and :endDate " +
                        " AND productiveZone.id = :productiveZoneId " +
                        " GROUP BY productiveZone.id, productiveZone.name ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productiveZoneId", productiveZoneId)
                .getResultList();

        if (resultList.size() > 0){
            for (Object[] objects : resultList) {
                result[0] = objects[1];
                result[1] = objects[2];
            }
        }
        return result;
    }
    public Object[] getCreditRecovery(Long productiveZoneId, Date startDate, Date endDate){
        Object[] result = {0, BigDecimal.ZERO, BigDecimal.ZERO}; //Por defecto. [Cant, Capital, Interes]
        List<Object[]> resultList = em.createQuery(
                " SELECT " +
                        " productiveZone.id," +
                        " count(credit.id), " +
                        " sum(creditTransaction.capital), " +
                        " sum(creditTransaction.interest)" +
                        " FROM Credit credit " +
                        " LEFT JOIN credit.creditTransactionList creditTransaction " +
                        " LEFT JOIN credit.partner partner " +
                        " LEFT JOIN partner.productiveZone productiveZone " +
                        " WHERE creditTransaction.date between :startDate and :endDate " +
                        " AND productiveZone.id = :productiveZoneId " +
                        " GROUP BY productiveZone.id, productiveZone.name ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productiveZoneId", productiveZoneId)
                .getResultList();

        if (resultList.size() > 0){
            for (Object[] objects : resultList) {
                result[0] = objects[1];
                result[1] = objects[2];
                result[2] = objects[3];
            }
        }
        return result;
    }

    @Override
    public String findCreditGuarantors(Credit credit) {

        String result = "";

        System.out.println("................credit: " + credit.getId());
        System.out.println("................credit: " + credit.getPartner().getFullName());

        List<Guarantor> guarantors = em.createQuery("select guarantor from Guarantor guarantor where guarantor.credit.id =:creditId")
                .setParameter("creditId", credit.getId())
                .getResultList();

        System.out.println("..........guarantors.size(): " + guarantors.size());

        for ( int i=0 ; i < guarantors.size() ; i++){
            System.out.println("............ i: " + i + " - " + guarantors.get(i).getPartner().getFullName());
            result = result + guarantors.get(i).getPartner().getFullName();

            if (i+1 < guarantors.size() )
                result = result + " - ";
        }

        System.out.println("..........RESULT: " + result);

        return result;
    }

    @Override
    public void approveTransfer(Credit credit) {

        Credit originCredit = credit.getOriginCredit();

        String gloss = MessageUtils.getMessage("Credit.transfer.gloss.message");
        gloss = gloss + " " + originCredit.getPartner().getFullName();

        CreditTransaction creditTransaction = new CreditTransaction();
        creditTransaction.setCapital(originCredit.getCapitalBalance());
        creditTransaction.setInterest(BigDecimal.ZERO);
        creditTransaction.setDeferredQuota(BigDecimal.ZERO);
        creditTransaction.setCriminalInterest(BigDecimal.ZERO);
        creditTransaction.setAmount(originCredit.getCapitalBalance());
        creditTransaction.setDifference(BigDecimal.ZERO);
        creditTransaction.setGloss(gloss);
        creditTransaction.setDate(new Date());
        creditTransaction.setCreationDate(new Date());
        creditTransaction.setDays(0);
        creditTransaction.setCapitalBalance(BigDecimal.ZERO);
        creditTransaction.setCreditTransactionType(CreditTransactionType.TRA);
        creditTransaction.setCredit(originCredit);
        //creditTransaction.setVoucher(voucher);

        CreditTransaction creditTransactionNew = new CreditTransaction();
        creditTransactionNew.setCapital(originCredit.getCapitalBalance());
        creditTransactionNew.setInterest(BigDecimal.ZERO);
        creditTransactionNew.setDeferredQuota(BigDecimal.ZERO);
        creditTransactionNew.setCriminalInterest(BigDecimal.ZERO);
        creditTransactionNew.setAmount(originCredit.getCapitalBalance());
        creditTransactionNew.setDifference(BigDecimal.ZERO);
        creditTransactionNew.setGloss(gloss);
        creditTransactionNew.setDate(new Date());
        creditTransactionNew.setCreationDate(new Date());
        creditTransactionNew.setDays(0);
        creditTransactionNew.setCapitalBalance(originCredit.getCapitalBalance());
        creditTransactionNew.setCreditTransactionType(CreditTransactionType.TRA);
        creditTransactionNew.setCredit(credit);

        em.persist(creditTransaction);
        em.persist(creditTransactionNew);
        em.flush();


        Voucher voucher = new Voucher();
        voucher.setDocumentType(Constants.CT_VOUCHER_DOCTYPE);
        voucher.setGloss(gloss);

        CreditType creditTypeOrigin = credit.getOriginCredit().getCreditType();

        String cashAccountOrigin = creditTypeOrigin.getCurrentAccountCode(); // Vig
        if (originCredit.getState().equals(CreditState.VEN))
            cashAccountOrigin = creditTypeOrigin.getExpiredAccountCode();
        if (originCredit.getState().equals(CreditState.EJE))
            cashAccountOrigin = creditTypeOrigin.getExecutedAccountCode();


        VoucherDetail voucherDetailDebit = new VoucherDetail();
        voucherDetailDebit.setAccount(credit.getCreditType().getCurrentAccountCode());
        voucherDetailDebit.setDebit(originCredit.getCapitalBalance());
        voucherDetailDebit.setCredit(BigDecimal.ZERO);
        voucherDetailDebit.setCreditPartner(credit);

        VoucherDetail voucherDetailCredit = new VoucherDetail();
        voucherDetailCredit.setAccount(cashAccountOrigin);
        voucherDetailCredit.setDebit(BigDecimal.ZERO);
        voucherDetailCredit.setCredit(originCredit.getCapitalBalance());
        voucherDetailCredit.setCreditPartner(originCredit);

        voucher.getDetails().add(voucherDetailDebit);
        voucher.getDetails().add(voucherDetailCredit);

        originCredit.setLastState(originCredit.getState());
        originCredit.setState(CreditState.FIN);
        originCredit.setCapitalBalance(BigDecimal.ZERO);

        credit.setDelivered(true);

        voucherAccoutingService.saveVoucher(voucher);

        creditTransactionService.updateTransaction(creditTransaction, voucher);
        creditTransactionService.updateTransaction(creditTransactionNew, voucher);

    }

}
