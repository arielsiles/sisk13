package com.encens.khipus.interceptor;

import javax.interceptor.Interceptors;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * PostCreateAssign
 *
 * @author
 */
@Target(TYPE)
@Retention(RUNTIME)
@Interceptors(PostCreateAssignInterceptor.class)
public @interface PostCreateAssign {
}
