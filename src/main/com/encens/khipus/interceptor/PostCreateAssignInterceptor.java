package com.encens.khipus.interceptor;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.util.ELEvaluatorUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.el.EL;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;

/**
 * PostCreateAssignInterceptor
 *
 * @author
 * @version 2.4
 */
public class PostCreateAssignInterceptor {
    @AroundInvoke
    public Object postCreate(InvocationContext invocationContext) throws Exception {
        Object proceedResult = invocationContext.proceed();
        if ((invocationContext.getMethod().isAnnotationPresent(Assign.class) ||
                invocationContext.getMethod().isAnnotationPresent(Assignments.class)) &&
                Outcome.SUCCESS.equals(proceedResult)) {
            Object targetObject = invocationContext.getTarget();
            if (targetObject instanceof GenericAction) {
                GenericAction genericAction = (GenericAction) targetObject;
                for (Annotation annotation : invocationContext.getMethod().getDeclaredAnnotations()) {
                    if (annotation instanceof Assign) {
                        performAssignment(genericAction, (Assign) annotation);
                    } else if (annotation instanceof Assignments) {
                        Assignments assignments = ((Assignments) annotation);
                        if (!ValidatorUtil.isEmptyOrNull(assignments.value())) {
                            for (int i = 0; i < assignments.value().length; i++) {
                                performAssignment(genericAction, assignments.value()[i]);
                            }
                        }
                    }
                }
                genericAction.createInstance();
                return Outcome.REDISPLAY;
            }
        }
        return proceedResult;
    }

    private void performAssignment(GenericAction genericAction, Assign assign) {
        if (assign != null && !ValidatorUtil.isBlankOrNull(assign.value())) {
            ELContext elContext = EL.createELContext();
            try {
                ValueExpression valueExpression = ELEvaluatorUtil.i.getValueExpression(assign.value());
                if (valueExpression != null) {
                    if (genericAction.getEntityClass().equals(valueExpression.getType(elContext))) {
                        valueExpression.setValue(elContext, genericAction.getInstance());
                    } else {
                        throw new IllegalArgumentException("The ELExpression expected class " + valueExpression.getType(elContext) + " type must be a instance of " + genericAction.getEntityClass());
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("It was not possible to assign the value to the expression provided", e);
            }
        }
    }
}
