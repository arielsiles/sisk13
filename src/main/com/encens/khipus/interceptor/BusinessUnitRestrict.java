package com.encens.khipus.interceptor;

import javax.interceptor.Interceptors;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Interceptor annotation to make the BusinessUnit access control for the <code>SessionUser</code>.
 *
 * @author
 * @version 2.23
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Interceptors(BusinessUnitRestrictInterceptor.class)
public @interface BusinessUnitRestrict {
}
