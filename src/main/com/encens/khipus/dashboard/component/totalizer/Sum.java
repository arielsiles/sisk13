package com.encens.khipus.dashboard.component.totalizer;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author
 * @version 2.7
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface Sum {
    String fieldResultName();
}
