package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.finances.PaymentType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * Session bean Service to get payroll report summary data
 *
 * @author
 * @version $Id: PayrollSummaryReportServiceBean.java  27-ene-2010 19:39:28$
 */

@Stateless
@Name("payrollSummaryReportService")
@AutoCreate
public class PayrollSummaryReportServiceBean implements PayrollSummaryReportService {
    @Logger
    private Log log;

    @In
    private GenericService genericService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public PayrollSummaryReportServiceBean() {
    }

    public BigDecimal calculateLiquidWithCheckPaymentType(Long generatedPayrollId, Long jobCategoryId) {
        return calculateLiquidByPaymentType(generatedPayrollId, jobCategoryId, PaymentType.PAYMENT_WITH_CHECK);
    }

    public BigDecimal calculateLiquidWithBankAccountPaymentTypeCurrency(Long generatedPayrollId, Long jobCategoryId, Long currencyId) {
        return calculateLiquidByPaymentTypeAndCurrency(generatedPayrollId, jobCategoryId, PaymentType.PAYMENT_BANK_ACCOUNT, currencyId);
    }

    /**
     * calculate payroll liquid sum by payment type
     *
     * @param generatedPayrollId generated payroll id
     * @param jobCategoryId
     * @param paymentType
     * @return BigDecimal
     */
    private BigDecimal calculateLiquidByPaymentType(Long generatedPayrollId, Long jobCategoryId, PaymentType paymentType) {
        BigDecimal sumResult = null;

        JobCategory jobCategory = findJobCategory(jobCategoryId);

        if (PayrollGenerationType.GENERATION_BY_SALARY.equals(jobCategory.getPayrollGenerationType())) {
            //calculate from planillaadministrativos table
            sumResult = (BigDecimal) em.createNamedQuery("ManagersPayroll.sumLiquidByPaymentType").
                    setParameter("generatedPayrollId", generatedPayrollId).
                    setParameter("paymentType", paymentType).
                    getSingleResult();
        } else if (PayrollGenerationType.GENERATION_BY_TIME.equals(jobCategory.getPayrollGenerationType())) {
            //calculate from planillageneral table
            sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumLiquidByPaymentType").
                    setParameter("generatedPayrollId", generatedPayrollId).
                    setParameter("paymentType", paymentType).
                    getSingleResult();
        } else if (PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(jobCategory.getPayrollGenerationType())) {
            //calculate from planillageneral table
            sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumLiquidByPaymentType").
                    setParameter("generatedPayrollId", generatedPayrollId).
                    setParameter("paymentType", paymentType).
                    getSingleResult();
        }

        return sumResult;
    }

    /**
     * calculate payroll liquid sum with criteria filters
     *
     * @param generatedPayrollId
     * @param jobCategoryId
     * @param paymentType
     * @param currencyId
     * @return BigDecimal
     */
    private BigDecimal calculateLiquidByPaymentTypeAndCurrency(Long generatedPayrollId, Long jobCategoryId, PaymentType paymentType, Long currencyId) {
        BigDecimal sumResult = null;

        JobCategory jobCategory = findJobCategory(jobCategoryId);

        if (PayrollGenerationType.GENERATION_BY_SALARY.equals(jobCategory.getPayrollGenerationType())) {
            //calculate from planillaadministrativos table
            sumResult = (BigDecimal) em.createNamedQuery("ManagersPayroll.sumLiquidByPaymentTypeAndCurrency").
                    setParameter("generatedPayrollId", generatedPayrollId).
                    setParameter("paymentType", paymentType).
                    setParameter("defaultAccount", Boolean.TRUE).
                    setParameter("currencyId", currencyId).
                    getSingleResult();
        } else if (PayrollGenerationType.GENERATION_BY_TIME.equals(jobCategory.getPayrollGenerationType())) {
            //calculate from planillageneral table
            sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumLiquidByPaymentTypeAndCurrency").
                    setParameter("generatedPayrollId", generatedPayrollId).
                    setParameter("paymentType", paymentType).
                    setParameter("defaultAccount", Boolean.TRUE).
                    setParameter("currencyId", currencyId).
                    getSingleResult();
        } else if (PayrollGenerationType.GENERATION_BY_PERIODSALARY.equals(jobCategory.getPayrollGenerationType())) {
            //calculate from planillageneral table
            sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumLiquidByPaymentTypeAndCurrency").
                    setParameter("generatedPayrollId", generatedPayrollId).
                    setParameter("paymentType", paymentType).
                    setParameter("defaultAccount", Boolean.TRUE).
                    setParameter("currencyId", currencyId).
                    getSingleResult();
        }

        return sumResult;
    }


    private JobCategory findJobCategory(Long jobCategoryId) {
        JobCategory jobCategory = null;
        try {
            jobCategory = genericService.findById(JobCategory.class, jobCategoryId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found job category with id:" + jobCategoryId, e);
        }
        return jobCategory;
    }

}
