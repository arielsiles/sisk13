package com.encens.khipus.service.warehouse;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: ProductItemReportService.java  11-mar-2010 19:49:55$
 */
public interface ProductItemReportService {
    BigDecimal sumProductItemUnitaryBalanceInventory(String productItemCode, String companyNumber);
}
