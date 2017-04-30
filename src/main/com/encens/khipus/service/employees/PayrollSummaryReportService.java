package com.encens.khipus.service.employees;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * service to calculate payroll summary values
 *
 * @author
 * @version $Id: PayrollSummaryReportService.java  28-ene-2010 14:29:21$
 */
@Local
public interface PayrollSummaryReportService {

    BigDecimal calculateLiquidWithCheckPaymentType(Long generatedPayrollId, Long jobCategoryId);

    BigDecimal calculateLiquidWithBankAccountPaymentTypeCurrency(Long generatedPayrollId, Long jobCategoryId, Long currencyId);

}
