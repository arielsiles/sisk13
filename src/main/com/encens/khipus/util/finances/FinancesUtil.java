package com.encens.khipus.util.finances;

import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;

/**
 * FinancesUtil
 *
 * @author
 * @version 2.2
 */
public final class FinancesUtil {
    private FinancesUtil() {
    }

    public static String addSchema(String string) {
        return !ValidatorUtil.isBlankOrNull(string) ? Constants.FINANCES_SCHEMA + "." + string : string;
    }
}
