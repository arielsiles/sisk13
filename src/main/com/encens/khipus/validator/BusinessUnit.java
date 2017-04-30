package com.encens.khipus.validator;

import org.hibernate.validator.ValidatorClass;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to enable the validation on <code>BusinessUnit</code> relationship fields in the entities.
 *
 * @author
 * @version 2.23
 */
@Documented
@ValidatorClass(BusinessUnitValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface BusinessUnit {
    String message() default "{BusinessUnit.error.access}";
}
