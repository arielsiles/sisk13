package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.joda.time.DateTime;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
@Stateless
@Name("academicPayrollSummaryService")
@AutoCreate
public class AcademicPayrollSummaryServiceBean implements AcademicPayrollSummaryService {
    @Logger
    private Log log;

    @In
    private GenericService genericService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public AcademicPayrollSummaryServiceBean() {
    }

    public Long countByCostCenter(Long generatedPayrollId, String costCenterCode) {
        Long countResult = null;

        countResult = (Long) em.createNamedQuery("GeneralPayroll.countByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return countResult;
    }

    public Long countByGeneratedPayroll(Long generatedPayrollId) {
        Long countResult = null;

        countResult = (Long) em.createNamedQuery("GeneralPayroll.countByGeneratedPayrollId").
                setParameter("generatedPayrollId", generatedPayrollId).
                getSingleResult();

        return countResult;
    }

    public BigDecimal sumTotalIncomeByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumTotalIncomeByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal sumTotalDiscountByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumTotalDiscountByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal sumLiquidByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumLiquidByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal sumLiquidByPaymentType(Long generatedPayrollId, PaymentType paymentType) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumLiquidByPaymentType").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("paymentType", paymentType).
                getSingleResult();
        return sumResult;
    }

    public BigDecimal sumLiquidByBankAccountPaymentTypeAndCurrency(Long generatedPayrollId, Long currencyId) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumLiquidByPaymentTypeAndCurrency").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("paymentType", PaymentType.PAYMENT_BANK_ACCOUNT).
                setParameter("defaultAccount", Boolean.TRUE).
                setParameter("currencyId", currencyId).
                getSingleResult();

        return sumResult;
    }

    public GeneratedPayroll getPreviousMonthGeneratedPayroll(JobCategory jobCategory, Gestion gestion, Month month, Long businessUnidId) {
        GeneratedPayroll previousGeneratedPayroll = null;

        DateTime dateTime = new DateTime(gestion.getYear(), month.getValueAsPosition(), 15, 0, 0, 0, 0);
        DateTime previousMonthDateTime = dateTime.minusMonths(1);
        Month previousMonth = Month.getMonth(previousMonthDateTime.getMonthOfYear());

        //find gestion
        try {
            Gestion previousGestion = (Gestion) em.createNamedQuery("Gestion.findByYearCompanyId").
                    setParameter("year", previousMonthDateTime.getYear()).
                    setParameter("companyId", Constants.defaultCompanyId).
                    getSingleResult();

            previousGeneratedPayroll = (GeneratedPayroll) em.createNamedQuery("GeneratedPayroll.findByJobCategoryGestionMonthGeneratedPayrollTypeBusinessUnitId").
                    setParameter("jobCategory", jobCategory).
                    setParameter("gestion", previousGestion).
                    setParameter("month", previousMonth).
                    setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).
                    setParameter("businessUnitId", businessUnidId).
                    getSingleResult();
        } catch (Exception e) {
            log.debug("Error in find previous generated payroll... " + e);
        }

        return previousGeneratedPayroll;
    }

    public BigDecimal sumLiquidByGeneratedPayroll(Long generatedPayrollId) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("GeneralPayroll.sumLiquidByGeneratedPayrollId").
                setParameter("generatedPayrollId", generatedPayrollId).
                getSingleResult();

        return sumResult;
    }

}
