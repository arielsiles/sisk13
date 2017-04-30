package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Month;
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
 * @author
 * @version 3.4
 */
@Stateless
@Name("laboralProfessorPayrollSummaryService")
@AutoCreate
public class LaboralProfessorPayrollSummaryServiceBean implements LaboralProfessorPayrollSummaryService {
    @Logger
    private Log log;

    @In
    private GenericService genericService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private AcademicPayrollSummaryService academicPayrollSummaryService;

    public LaboralProfessorPayrollSummaryServiceBean() {
    }

    public Long countByCostCenter(Long generatedPayrollId, String costCenterCode) {
        Long countResult = null;

        countResult = (Long) em.createNamedQuery("FiscalProfessorPayroll.countByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return countResult;
    }

    public BigDecimal sumTotalIncomeByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumTotalIncomeByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal sumTotalDiscountByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumTotalDiscountByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal sumLiquidByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumLiquidByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal sumLiquidByPaymentType(Long generatedPayrollId, PaymentType paymentType) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumLiquidByPaymentType").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("paymentType", paymentType).
                getSingleResult();
        return sumResult;
    }

    public BigDecimal sumLiquidByBankAccountPaymentTypeAndCurrency(Long generatedPayrollId, Long currencyId) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumLiquidByPaymentTypeAndCurrency").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT).
                setParameter("defaultAccount", Boolean.TRUE).
                setParameter("currencyId", currencyId).
                getSingleResult();

        return sumResult;
    }

    public GeneratedPayroll getPreviousMonthGeneratedPayroll(JobCategory jobCategory, Gestion gestion, Month month, Long businessUnitId) {
        return academicPayrollSummaryService.getPreviousMonthGeneratedPayroll(jobCategory, gestion, month, businessUnitId);
    }

    public BigDecimal sumLiquidByGeneratedPayroll(Long generatedPayrollId) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("FiscalProfessorPayroll.sumLiquidByGeneratedPayrollId").
                setParameter("generatedPayrollId", generatedPayrollId).
                getSingleResult();

        return sumResult;
    }

}
