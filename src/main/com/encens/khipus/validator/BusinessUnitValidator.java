package com.encens.khipus.validator;

import com.encens.khipus.action.SessionUser;
import org.hibernate.validator.Validator;
import org.jboss.seam.Component;

import java.io.Serializable;

/**
 * Validates if the <code>SessionUser</code> can be work with the <code>BusinessUnit</code> object.
 *
 * @author
 * @version 2.22
 */

public class BusinessUnitValidator implements Validator<BusinessUnit>, Serializable {

    public boolean isValid(Object object) {
        SessionUser sessionUser = getSessionUser();
        return sessionUser == null || sessionUser.getBusinessUnitIds().contains(((com.encens.khipus.model.admin.BusinessUnit) object).getId());
    }

    public void initialize(BusinessUnit businessUnit) {
    }

    private SessionUser getSessionUser() {
        return (SessionUser) Component.getInstance("sessionUser");
    }
}
