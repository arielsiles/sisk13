package com.encens.khipus.interceptor;

import javax.interceptor.Interceptors;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * @author
 * @version 2.1.2
 */
@Target(TYPE)
@Retention(RUNTIME)
@Interceptors(FinancesUserInterceptor.class)
public @interface FinancesUser {
}
