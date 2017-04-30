package com.encens.khipus.util.fixedassets;

import com.encens.khipus.model.finances.FinancesModulePK;
import com.encens.khipus.util.Constants;

/**
 * @author
 * @version 2.1
 */

public class FixedAssetDefaultConstants {
    public static final String MODULE_CODE = "AF";
    public static final String DESCRIPTION = "ACTIVOS FIJOS";
    public static final Integer DEPRECIATION_FUNCTION_DIVIDER = 1200;

    private FixedAssetDefaultConstants() {
    }

    public static FinancesModulePK getFixedAssetModulePK() {
        return new FinancesModulePK(Constants.defaultCompanyNumber, MODULE_CODE);
    }
}

