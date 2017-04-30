package com.encens.khipus.util;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * ELEvaluator
 *
 * @author
 * @version 2.22
 */
@Name("elEvaluator")
@Scope(ScopeType.EVENT)
public class ELEvaluator {
    public void evaluateMethodBinding(String elExpression) {
        ELEvaluatorUtil.i.getValue(elExpression);
    }

    public Object getValue(String elExpression) {
        return ELEvaluatorUtil.i.getValue(elExpression);
    }
}
