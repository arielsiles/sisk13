package com.encens.khipus.interceptor;

import com.encens.khipus.util.BusinessUnitValidatorUtil;
import com.encens.khipus.util.ELEvaluatorUtil;
import com.encens.khipus.validator.BusinessUnitValidator;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Interceptor that implements the BusinessUnit access control for the <code>SessionUser</code>.
 * <p/>
 * Intercept every crud method that are annotated by <code>@com.encens.khipus.interceptor.BusinessUnitRestriction</code>
 * and verifies the <code>BusinessUnit</code> relationships in the Entity instance that will be persisted, if the
 * <code>SessionUser</code> cannot access some <code>BusinessUnit</code> relationship a <code>BusinessUnitAccessException</code>
 * its thrown.
 *
 * @author
 * @version 2.23
 * @see BusinessUnitValidator
 */

public class BusinessUnitRestrictInterceptor {
    private static final LogProvider log = Logging.getLogProvider(BusinessUnitRestrictInterceptor.class);

    /**
     * Checks if the method are annotated by <code>@com.encens.khipus.interceptor.BusinessUnitRestriction</code>.
     * <p/>
     * In the postValidation first the <code>InvocationContext.proceed()</code> method its called and after of this
     * the BusinessUnit access validation logic its executed.
     * <p/>
     * The postValidation are enabled or disabled in <code>@com.encens.khipus.interceptor.BusinessUnitRestriction</code>
     * annotation, by default it is disabled.
     *
     * @param invocationContext The <code>InvocationContext</code> object.
     * @return Object instance to proceed.
     * @throws Exception If the <code>BusinessUnitValidator.isValid()</code> method returns false for
     *                   some <code>BusinessUnit</code>.
     */
    @AroundInvoke
    public Object checkBusinessUnit(InvocationContext invocationContext) throws Exception {

        if (invocationContext.getMethod().isAnnotationPresent(BusinessUnitRestriction.class)) {
            log.debug("Validating BusinessUnit access for class: "
                    + invocationContext.getTarget().getClass().getName()
                    + ", method: "
                    + invocationContext.getMethod().getName());

            BusinessUnitRestriction restriction =
                    invocationContext.getMethod().getAnnotation(BusinessUnitRestriction.class);

            Object proceed = null;

            if (restriction.postValidation()) {
                proceed = invocationContext.proceed();
            }

            Object instance = ELEvaluatorUtil.i.getValue(restriction.value());

            BusinessUnitValidatorUtil.i.validateBusinessUnit(instance);

            if (null != proceed) {
                return proceed;
            }
        }

        return invocationContext.proceed();
    }
}
