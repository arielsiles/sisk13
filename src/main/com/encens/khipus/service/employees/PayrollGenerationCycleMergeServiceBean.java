package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.util.employees.payroll.fiscal.FiscalPayrollMergeProcessor;
import com.encens.khipus.util.employees.payroll.tributary.TributaryPayrollMergeProcessor;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 3.4
 */
@Stateless
@Name("payrollGenerationCycleMergeService")
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class PayrollGenerationCycleMergeServiceBean extends GenericServiceBean implements PayrollGenerationCycleMergeService {

    @Resource
    private UserTransaction userTransaction;

    @PersistenceContext(unitName = "khipus")
    private EntityManager em;

    /*
    * The method's goal it's compute the merge values between TributaryPayroll and CategoryTributaryPayroll,
    * this merged result(sum or avg) it's stored in TributaryPayroll.
    **/
    @SuppressWarnings({"unchecked"})
    public void merge(GeneratedPayroll generatedPayroll) throws ConcurrencyException, EntryDuplicatedException {
        PayrollGenerationCycle payrollGenerationCycle = generatedPayroll.getGestionPayroll().getPayrollGenerationCycle();
        if (payrollGenerationCycle != null) {
            Map<Long, TributaryPayroll> mergedMap = getMergedMap(payrollGenerationCycle);
            List<CategoryTributaryPayroll> categoryTributaryPayrollList = getEntityManager().createNamedQuery("CategoryTributaryPayroll.loadByGeneratedPayroll")
                    .setParameter("generatedPayroll", generatedPayroll).getResultList();
            try {
                // find the max size between mergeMap and categoryTributaryPayrollList for set the TransactionTimeout
                userTransaction.setTransactionTimeout(Math.max(mergedMap.size(), categoryTributaryPayrollList.size()) * 60);
                userTransaction.begin();
                Long lastMax = 1l;
                for (CategoryTributaryPayroll categoryTributaryPayroll : categoryTributaryPayrollList) {
                    TributaryPayroll tributaryPayroll = mergedMap.get(categoryTributaryPayroll.getEmployee().getId());
                    tributaryPayroll = new TributaryPayrollMergeProcessor(payrollGenerationCycle, tributaryPayroll, categoryTributaryPayroll).merge();
                    if (tributaryPayroll.getId() != null) {
                        getEntityManager().merge(tributaryPayroll);
                    } else {
                        lastMax = Math.max(lastMax, nextTributaryPayrollNumber(payrollGenerationCycle));
                        tributaryPayroll.setNumber(lastMax);
                        getEntityManager().persist(tributaryPayroll);
                        lastMax++;
                    }
                    FiscalPayroll fiscalPayroll = new FiscalPayrollMergeProcessor(payrollGenerationCycle, tributaryPayroll.getFiscalPayroll(), categoryTributaryPayroll.getCategoryFiscalPayroll())
                            .merge();
                    if (fiscalPayroll.getId() != null) {
                        getEntityManager().merge(fiscalPayroll);
                    } else {
                        fiscalPayroll.setNumber(tributaryPayroll.getNumber());
                        fiscalPayroll.setTributaryPayroll(tributaryPayroll);
                        getEntityManager().persist(fiscalPayroll);
                    }
                }
                userTransaction.commit();
            } catch (OptimisticLockException e) {
                e.printStackTrace();
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("An unexpected error have happened rolling back", e1);
                }
                throw new ConcurrencyException(e);
            } catch (PersistenceException e) {
                e.printStackTrace();
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("An unexpected error have happened rolling back", e1);
                }
                throw new EntryDuplicatedException(e);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("An unexpected error have happened ...", e);
                try {
                    userTransaction.rollback();
                } catch (SystemException e1) {
                    log.error("An unexpected error have happened rolling back", e1);
                }
                throw new RuntimeException(e);
            }
        }
    }


    @SuppressWarnings({"unchecked"})
    private Map<Long, TributaryPayroll> getMergedMap(PayrollGenerationCycle payrollGenerationCycle) {
        List<TributaryPayroll> tributaryPayrollList = getEntityManager()
                .createNamedQuery("TributaryPayroll.loadByPayrollGenerationCycle")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle)
                .getResultList();
        Map<Long, TributaryPayroll> mapResult = new HashMap<Long, TributaryPayroll>();
        for (TributaryPayroll tributaryPayroll : tributaryPayrollList) {
            mapResult.put(tributaryPayroll.getEmployee().getId(), tributaryPayroll);
        }
        return mapResult;
    }

    private Long nextTributaryPayrollNumber(PayrollGenerationCycle payrollGenerationCycle) {
        Long countResult = (Long) getEntityManager().createNamedQuery("TributaryPayroll.maxNumberByPayrollGenerationCycle")
                .setParameter("payrollGenerationCycle", payrollGenerationCycle).getSingleResult();
        return countResult == null ? 1l : countResult + 1;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
