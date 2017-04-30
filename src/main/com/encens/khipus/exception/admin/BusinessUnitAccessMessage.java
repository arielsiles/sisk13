package com.encens.khipus.exception.admin;

import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.Name;

/**
 * Find the <code>BusinessUnitAccessException</code> and building the message to are shown in the view.
 *
 * @author
 * @version 2.23
 */
@Name("businessUnitAccessMessage")
public class BusinessUnitAccessMessage {

    public String getMessage(Exception e) {
        BusinessUnitAccessException exception = getBusinessUnitAccessException(e);
        if (null != exception) {
            return MessageUtils.getMessage("BusinessUnit.error.access.list", exception.getBusinessUnitName());
        }

        return null;
    }

    private BusinessUnitAccessException getBusinessUnitAccessException(Exception e) {
        for (Exception cause = e; cause != null; cause = org.jboss.seam.util.Exceptions.getCause(cause)) {
            if (cause instanceof BusinessUnitAccessException) {
                return (BusinessUnitAccessException) cause;
            }
        }

        return null;
    }
}
