package com.encens.khipus.interceptor;

import com.encens.khipus.exception.finances.FinancesUserException;
import com.encens.khipus.model.admin.User;
import org.jboss.seam.contexts.Contexts;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * @author
 * @version 2.1.2
 */

public class FinancesUserInterceptor {

    @AroundInvoke
    public Object checkFinanceUser(InvocationContext invocationContext) throws Exception {

        if (Contexts.getSessionContext() != null) {
            User currentUser = (User) Contexts.getSessionContext().get("currentUser");

            if (currentUser != null && (null == currentUser.getFinancesUser() || !currentUser.getFinancesUser())) {
                throw new FinancesUserException("The current user is not enabled as finances user.");
            }
        }

        return invocationContext.proceed();
    }
}
