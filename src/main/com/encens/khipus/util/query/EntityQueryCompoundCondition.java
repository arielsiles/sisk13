package com.encens.khipus.util.query;

import com.encens.khipus.exception.employees.MalformedEntityQueryCompoundConditionException;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.ListUtil;
import com.encens.khipus.util.ValidatorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
public class EntityQueryCompoundCondition extends EntityQueryCondition {
    List<EntityQueryConditionElement> entityQueryConditionElementList = new ArrayList<EntityQueryConditionElement>();

    /**
     * Adds either Compound and Single EntityQueryCondition
     *
     * @param entityQueryCondition the condition to add
     * @throws MalformedEntityQueryCompoundConditionException
     *          thrown when the condition is malformed
     */
    public void addCondition(EntityQueryCondition entityQueryCondition) throws MalformedEntityQueryCompoundConditionException {
        if (ValidatorUtil.isEmptyOrNull(entityQueryConditionElementList)) {
            entityQueryConditionElementList.add(entityQueryCondition);
        } else if (ListUtil.i.getLastElement(entityQueryConditionElementList) instanceof EntityQueryConditionOperator) {
            entityQueryConditionElementList.add(entityQueryCondition);
        } else {
            throw new MalformedEntityQueryCompoundConditionException();
        }
    }

    /**
     * Adds an EntityQueryConditionOperator
     *
     * @param entityQueryConditionOperator the condition operator to add
     * @throws MalformedEntityQueryCompoundConditionException
     *          thrown when the condition is malformed
     */

    public void addConditionOperator(EntityQueryConditionOperator entityQueryConditionOperator) throws MalformedEntityQueryCompoundConditionException {
        if (!ValidatorUtil.isEmptyOrNull(entityQueryConditionElementList) &&
                ListUtil.i.getLastElement(entityQueryConditionElementList) instanceof EntityQueryCondition) {
            entityQueryConditionElementList.add(entityQueryConditionOperator);
        } else {
            throw new MalformedEntityQueryCompoundConditionException();
        }
    }

    /**
     * a condition is said to me malformed when there are no elements, or there is one element of type operator
     * and when there are more than one element and there are two elements of the same instance type or the last element is an operator
     *
     * @return true if the condition is well formed
     */
    public boolean isWellFormed() {
        EntityQueryConditionElement lastElement;
        if (ValidatorUtil.isEmptyOrNull(entityQueryConditionElementList)) {
            return false;
        } else {
            lastElement = entityQueryConditionElementList.get(0);
            if (entityQueryConditionElementList.size() == 1) {
                return lastElement instanceof EntityQueryCondition;
            } else {
                //more than one element
                int i = 1;
                while (i < entityQueryConditionElementList.size()) {
                    EntityQueryConditionElement currentElement = entityQueryConditionElementList.get(i);
                    // if there are two element of the same instance type malformed condition
                    if ((lastElement instanceof EntityQueryConditionOperator && currentElement instanceof EntityQueryConditionOperator)
                            || (lastElement instanceof EntityQueryCondition && currentElement instanceof EntityQueryCondition)) {
                        return false;
                    }
                    lastElement = currentElement;
                    i++;
                }
                return ListUtil.i.getLastElement(entityQueryConditionElementList) instanceof EntityQueryCondition;
            }
        }
    }

    /**
     * Evaluates if any expressionLanguageConditionValue is true in order to determine to add the compound condition of not
     *
     * @return true if any of the expressionLanguageConditionValue is not null
     */
    public boolean evaluateConditionValue() {
        boolean anySucceed = false;
        for (EntityQueryConditionElement entityQueryConditionElement : entityQueryConditionElementList) {
            if (entityQueryConditionElement instanceof EntityQueryCondition &&
                    ((EntityQueryCondition) entityQueryConditionElement).evaluateConditionValue()) {
                anySucceed = true;
            }
        }
        return anySucceed;
    }


    public List<EntityQueryConditionElement> getEntityQueryConditionElementList() {
        return entityQueryConditionElementList;
    }

    public void setEntityQueryConditionElementList(List<EntityQueryConditionElement> entityQueryConditionElementList) {
        this.entityQueryConditionElementList = entityQueryConditionElementList;
    }

    public String compile() {
        String result = "";
        if (isWellFormed() && evaluateConditionValue()) {
            for (int i = 0, entityQueryConditionElementListSize = entityQueryConditionElementList.size(); i < entityQueryConditionElementListSize; i += 2) {
                EntityQueryConditionElement entityQueryConditionElement = entityQueryConditionElementList.get(i);
                if (entityQueryConditionElement instanceof EntityQueryCondition && ((EntityQueryCondition) entityQueryConditionElement).evaluateConditionValue()) {
                    EntityQueryCondition condition = (EntityQueryCondition) entityQueryConditionElement;
                    if (i >= 2 && !(ValidatorUtil.isBlankOrNull(result))) {
                        EntityQueryConditionOperator conditionOperator = (EntityQueryConditionOperator) entityQueryConditionElementList.get(i - 1);
                        result += FormatUtils.wrapBySpace(conditionOperator.name());
                    }
                    result += condition.compile();
                }
            }
        }
        return ValidatorUtil.isBlankOrNull(result) ? result : FormatUtils.wrapByParentheses(result);
    }
}
