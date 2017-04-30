package com.encens.khipus.util;

import com.encens.khipus.exception.admin.BusinessUnitAccessException;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.validator.BusinessUnitValidator;

import java.util.List;

/**
 * @author
 * @version 2.23
 */
public class BusinessUnitValidatorUtil {
    public static BusinessUnitValidatorUtil i = new BusinessUnitValidatorUtil();

    private BusinessUnitValidatorUtil() {
    }

    public void validateBusinessUnit(Object entity) {
        List<BusinessUnit> businessUnits = BusinessUnitFieldStore.i.getValuesToValidate(entity);

        if (!businessUnits.isEmpty()) {
            BusinessUnitValidator validator = new BusinessUnitValidator();

            for (BusinessUnit businessUnit : businessUnits) {
                if (null == businessUnit) {
                    continue;
                }

                if (!validator.isValid(businessUnit)) {
                    throw new BusinessUnitAccessException("The SessionUser cannot work on BusinessUnit id: "
                            + businessUnit.getId(),
                            businessUnit.getOrganization().getName());
                }
            }
        }
    }

    public void validateBusinessUnit(BusinessUnit businessUnit) {
        BusinessUnitValidator validator = new BusinessUnitValidator();

        if (null != businessUnit && !validator.isValid(businessUnit)) {
            throw new BusinessUnitAccessException("The SessionUser cannot work on BusinessUnit id: "
                    + businessUnit.getId(),
                    businessUnit.getOrganization().getName());
        }
    }
}
