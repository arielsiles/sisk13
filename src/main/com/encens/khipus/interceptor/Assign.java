package com.encens.khipus.interceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Assign
 *
 * @author
 * @version 2.4
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Assign {
    String value() default "";
}
