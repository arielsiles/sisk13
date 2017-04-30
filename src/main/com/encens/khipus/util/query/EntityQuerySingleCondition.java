package com.encens.khipus.util.query;

import com.encens.khipus.util.ELEvaluatorUtil;
import com.encens.khipus.util.ValidatorUtil;

/**
 * @author
 * @version 3.4
 */
public class EntityQuerySingleCondition extends EntityQueryCondition {
    private static final String SHARP = "#";
    private String condition;
    private String expressionLanguageConditionValue;

    public EntityQuerySingleCondition(String condition) {
        this.condition = condition;
        if (condition.contains(SHARP)) {
            int start = condition.indexOf(SHARP);
            expressionLanguageConditionValue = condition.substring(start);
        }
    }

    /**
     * Evaluates expressionLanguageConditionValue in order to add the condition or not
     *
     * @return true if the expressionLanguageConditionValue is not null
     */
    public boolean evaluateConditionValue() {
        return !ValidatorUtil.isBlankOrNull(String.valueOf(ELEvaluatorUtil.i.getValue(expressionLanguageConditionValue)));
    }

    @Override
    String compile() {
        return evaluateConditionValue() ? condition : "";
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getExpressionLanguageConditionValue() {
        return expressionLanguageConditionValue;
    }

    public void setExpressionLanguageConditionValue(String expressionLanguageConditionValue) {
        this.expressionLanguageConditionValue = expressionLanguageConditionValue;
    }
}
