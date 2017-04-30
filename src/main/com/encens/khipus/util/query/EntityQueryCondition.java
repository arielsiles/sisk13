package com.encens.khipus.util.query;

/**
 * @author
 * @version 3.4
 */
public abstract class EntityQueryCondition implements EntityQueryConditionElement {
    abstract boolean evaluateConditionValue();

    abstract String compile();
}
