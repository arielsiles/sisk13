package com.encens.khipus.util;

import com.encens.khipus.model.admin.BusinessUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.23
 */
public class SessionUserUtil {
    private static final Long EMPTY_BUSINESS_ID = (long) -1;

    public static SessionUserUtil i = new SessionUserUtil();

    private SessionUserUtil() {
    }

    public List<Long> getSessionUserBusinessUnitIds(List<BusinessUnit> businessUnits) {
        List<Long> ids = new ArrayList<Long>();

        if (null != businessUnits && !businessUnits.isEmpty()) {
            for (BusinessUnit businessUnit : businessUnits) {
                ids.add(businessUnit.getId());
            }
        } else {
            ids.add(EMPTY_BUSINESS_ID);
        }

        return ids;
    }
}
