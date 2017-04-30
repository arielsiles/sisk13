package com.encens.khipus.service.employees;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.2
 */
@Local
public interface ChristmasSummaryReportService {
    Long countByCostCenter(Long generatedPayrollId, String costCenterCode);

    BigDecimal calculateLiquidByCostCenter(Long generatedPayrollId, String costCenterCode);

    BigDecimal calculateLiquidWithBankAccountPaymentTypeCurrency(Long generatedPayrollId, Long currencyId);

    BigDecimal calculateLiquidWithCheckPaymentType(Long generatedPayrollId);
}
