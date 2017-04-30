package com.encens.khipus.util.warehouse;

import com.encens.khipus.model.finances.FinancesModulePK;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.0
 */
public class WarehouseDefaultConstants {
    public static final String MODULE_CODE = "INV";

    private WarehouseDefaultConstants() {
    }

    public static FinancesModulePK getWarehouseFinancesModulePK() {
        return new FinancesModulePK(Constants.defaultCompanyNumber, MODULE_CODE);
    }
}
