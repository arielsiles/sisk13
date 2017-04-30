package com.encens.khipus.action.common;

import com.encens.khipus.util.ELEvaluatorUtil;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryExecutorUtil;
import com.encens.khipus.util.query.QuickSearchQueryUtil;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author
 * @version 2.25
 */
@Name("defaultSuggestionBoxDataModel")
public class DefaultSuggestionBoxDataModel {
    @Logger
    private Log log;

    public List getSuggestedElements(Class entityClass,
                                     String searchField,
                                     String definedEntityAlias,
                                     String definedFilters,
                                     List<String> restrictions) {
        String entityAlias = getBasicEntityAlias(entityClass, definedEntityAlias);
        String basicRestriction = getRestrictionForSuggestedElements(entityAlias, searchField);
        return search(entityClass, definedEntityAlias, definedFilters, basicRestriction, restrictions);
    }

    public List getElement(Class entityClass,
                           String searchField,
                           String definedEntityAlias,
                           String definedFilters,
                           List<String> restrictions) {
        String entityAlias = getBasicEntityAlias(entityClass, definedEntityAlias);
        String basicRestriction = getRestrictionBySearchElement(entityAlias, searchField);
        return search(entityClass, definedEntityAlias, definedFilters, basicRestriction, restrictions);
    }

    private String getBasicEntityAlias(Class entityClass, String definedEntityAlias) {
        return ValidatorUtil.isBlankOrNull(definedEntityAlias) ? QuickSearchQueryUtil.i.getEntityAlias(entityClass) : definedEntityAlias;
    }

    private String getRestrictionForSuggestedElements(String entityAlias, String searchField) {
        String basicRestriction;
        if (isCompoundField(searchField)) {
            List<String> searchFieldList = getBasicSearchFields(searchField);
            basicRestriction = "(";
            for (String searchFieldItem : searchFieldList) {
                basicRestriction += (!basicRestriction.equals("(") ? " or " : "") + "lower(" + searchFieldItem + ")  like concat(lower(#{suggestionBoxAction.searchValue}), '%')";
            }
            basicRestriction += ")";
        } else {
            basicRestriction = "lower(" + entityAlias + "." + searchField + ")  like concat(lower(#{suggestionBoxAction.searchValue}), '%')";
        }
        return basicRestriction;
    }

    private String getRestrictionBySearchElement(String entityAlias, String searchField) {
        String basicRestriction;
        if (isCompoundField(searchField)) {
            basicRestriction = "lower(" + searchField + ") like lower('" + ELEvaluatorUtil.i.getValue("#{suggestionBoxAction.searchValueForCompoundFields}") + "')";
        } else {
            basicRestriction = "lower(" + entityAlias + "." + searchField + ")  = lower(#{suggestionBoxAction.searchValue})";
        }
        return basicRestriction;
    }

    private List<String> getBasicSearchFields(String searchField) {
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(searchField.trim(), ",");
        while (tokenizer.hasMoreTokens()) {
            String element = tokenizer.nextToken();
            if (!ValidatorUtil.isBlankOrNull(element)) {
                result.add(element.trim());
            }
        }
        if (result.isEmpty()) {
            result.add(searchField);
        }
        return result;
    }

    public boolean isCompoundField(String searchField) {
        return searchField.contains(",") || searchField.contains(".") || searchField.contains("(");
    }

    private List search(Class entityClass,
                        String definedEntityAlias,
                        String definedFilters,
                        String basicRestriction,
                        List<String> restrictions) {
        String entityAlias = QuickSearchQueryUtil.i.getEntityAlias(entityClass);
        if (!restrictions.isEmpty()) {
            entityAlias = definedEntityAlias;
        }

        EntityQuery query = QuickSearchQueryUtil.i.createEntityQuery(entityClass, entityAlias, basicRestriction, restrictions);
        log.debug("The ejbQL: " + query.getEjbql());
        log.debug("The restrictions: " + restrictions);

        List<String> filterNames = new ArrayList<String>();

        if (null != definedFilters) {
            filterNames = processFilters(definedFilters);
        }

        return EntityQueryExecutorUtil.i.executeNamedQuery(query, filterNames);
    }

    private List<String> processFilters(String filters) {
        List<String> result = new ArrayList<String>();

        if (null != filters && !"".equals(filters.trim())) {
            String[] array = filters.split(",");
            for (String element : array) {
                result.add(element.trim());
            }
        }

        return result;
    }
}
