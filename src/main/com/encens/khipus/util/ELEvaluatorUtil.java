package com.encens.khipus.util;

import org.jboss.seam.el.EL;

import javax.el.ELContext;
import javax.el.ValueExpression;

/**
 * @author
 * @version 2.9
 */
public class ELEvaluatorUtil {
    public static final ELEvaluatorUtil i = new ELEvaluatorUtil();

    public Object getValue(String elExpression) {
        if (!ValidatorUtil.isBlankOrNull(elExpression)) {
            ELContext elContext = EL.createELContext();
            ValueExpression valueExpression = getValueExpression(elExpression);
            if (valueExpression != null) {
                return valueExpression.getValue(elContext);
            }
        }
        return null;
    }

    public void setValue(String elExpression, Object object) {
        ELContext elContext = EL.createELContext();
        ValueExpression valueExpression = getValueExpression(elExpression);
        if (valueExpression != null) {
            valueExpression.setValue(elContext, object);
        }
    }

    public ValueExpression getValueExpression(String elExpression) {
        if (!elExpression.startsWith("#")) {
            elExpression = "#{" + elExpression + "}";
        }
        ELContext elContext = EL.createELContext();
        return EL.EXPRESSION_FACTORY.createValueExpression(elContext, elExpression, Object.class);
    }
}
