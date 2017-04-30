package com.encens.khipus.interceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author
 * @version 2.23
 */
@Target(METHOD)
@Retention(RUNTIME)

public @interface BusinessUnitRestriction {
    String value();

    boolean postValidation() default false;
}
