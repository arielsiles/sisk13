package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.PaymentType;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
@Local
public interface ManagersPayrollSummaryService {


    Long countByCostCenter(Long generatedPayrollId, String costCenterCode);

    BigDecimal sumTotalIncomeByCostCenter(Long generatedPayrollId, String costCenterCode);

    BigDecimal sumTotalDiscountByCostCenter(Long generatedPayrollId, String costCenterCode);

    BigDecimal sumLiquidByCostCenter(Long generatedPayrollId, String costCenterCode);

    BigDecimal sumLiquidByPaymentType(Long generatedPayrollId, PaymentType paymentType);

    BigDecimal sumLiquidByBankAccountPaymentTypeAndCurrency(Long generatedPayrollId, Long currencyId);

    GeneratedPayroll getPreviousMonthGeneratedPayroll(JobCategory jobCategory, Gestion gestion, Month month, Long businessUnidId);

    Long countByGeneratedPayroll(Long generatedPayrollId);

    BigDecimal sumLiquidByGeneratedPayroll(Long generatedPayrollId);
}
