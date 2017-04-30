package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
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
 * Session bean Service to get christmas payroll report summary data
 *
 * @author
 * @version 3.2
 */
@Stateless
@Name("christmasSummaryReportService")
@AutoCreate
public class ChristmasSummaryReportServiceBean implements ChristmasSummaryReportService {
    @Logger
    private Log log;

    @In
    private GenericService genericService;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public ChristmasSummaryReportServiceBean() {
    }

    public Long countByCostCenter(Long generatedPayrollId, String costCenterCode) {
        Long countResult = null;

        countResult = (Long) em.createNamedQuery("ChristmasPayroll.countByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return countResult;
    }

    public BigDecimal calculateLiquidByCostCenter(Long generatedPayrollId, String costCenterCode) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("ChristmasPayroll.sumLiquidByCostCenter").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("costCenterCode", costCenterCode).
                getSingleResult();

        return sumResult;
    }

    public BigDecimal calculateLiquidWithBankAccountPaymentTypeCurrency(Long generatedPayrollId, Long currencyId) {
        return calculateLiquidByPaymentTypeAndCurrency(generatedPayrollId, PaymentType.PAYMENT_BANK_ACCOUNT, currencyId);
    }

    private BigDecimal calculateLiquidByPaymentTypeAndCurrency(Long generatedPayrollId, PaymentType paymentType, Long currencyId) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("ChristmasPayroll.sumLiquidByPaymentTypeAndCurrency").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("paymentType", paymentType).
                setParameter("defaultAccount", Boolean.TRUE).
                setParameter("currencyId", currencyId).
                getSingleResult();
        return sumResult;
    }

    public BigDecimal calculateLiquidWithCheckPaymentType(Long generatedPayrollId) {
        return calculateLiquidByPaymentType(generatedPayrollId, PaymentType.PAYMENT_WITH_CHECK);
    }

    private BigDecimal calculateLiquidByPaymentType(Long generatedPayrollId, PaymentType paymentType) {
        BigDecimal sumResult = null;

        sumResult = (BigDecimal) em.createNamedQuery("ChristmasPayroll.sumLiquidByPaymentType").
                setParameter("generatedPayrollId", generatedPayrollId).
                setParameter("paymentType", paymentType).
                getSingleResult();
        return sumResult;
    }

}
