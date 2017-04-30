package com.encens.khipus.service.finances;

import javax.ejb.Local;
import java.util.Date;
import java.util.Map;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: CashAvailableReportService.java  24-nov-2010 17:21:24$
 */
@Local
public interface CashAvailableReportService {
    Map<String, Object> calculateCashAvailableCrossTabInfoData(Date currentDate);
}
