package com.encens.khipus.action.common;

import com.encens.khipus.tag.RestrictionTagHandler;
import com.encens.khipus.tag.RestrictionTagUtil;
import com.encens.khipus.util.ELEvaluatorUtil;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryExecutorUtil;
import com.encens.khipus.util.query.QuickSearchQueryUtil;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.el.EL;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.Log;

import javax.el.ELContext;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.20
 */

@Name("quickSearchAction")
public class QuickSearchAction {

    private static final String VALUE = "value";
    public static final String INPUT_HIDDEN_VALUE_ID = "inputHidden_value";

    @Logger
    private Log log;

    private Object searchValue;

    public void search(ActionEvent actionEvent) {
        UIComponent a4jSupport = actionEvent.getComponent();

        String a4jSupportId = a4jSupport.getId();

        Object namedQuery = getNamedQuery(a4jSupport);

        Object componentSearchValue = ((UIInput) a4jSupport.getParent()).getValue();
        if (null != componentSearchValue && !"".equals(componentSearchValue.toString().trim())) {
            setSearchValue(componentSearchValue);

            List result;
            if (null != namedQuery && !"".equals(namedQuery.toString().trim())) {
                log.debug("The QuickSearch using a namedQuery: " + namedQuery);
                result = executeNamedQuery(namedQuery.toString(), a4jSupport);
            } else {
                log.debug("The QuickSearch using the queryGenerator");
                Object searchField = getSearchField(a4jSupport);
                Class entityClass = getExpectedClass(getInputHiddenValueId(a4jSupportId), a4jSupport);

                result = executeGeneratedQuery(a4jSupport, entityClass, searchField);
            }

            setResultValue(getInputHiddenValueId(a4jSupportId), a4jSupport, result);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private List<String> getRestrictions(UIComponent a4jSupport) {
        List<String> encodedRestrictions =
                (List<String>) a4jSupport.getAttributes().get(RestrictionTagHandler.LIST_NAME);

        return RestrictionTagUtil.i.decodeRestrictions(encodedRestrictions);
    }

    private Object getNamedQuery(UIComponent uiComponent) {
        return uiComponent.getAttributes().get("namedQuery");
    }

    private Object getPostSearchAction(UIComponent uiComponent) {
        return uiComponent.getAttributes().get("postSearchAction");
    }

    private Object getSearchField(UIComponent uiComponent) {
        return uiComponent.getAttributes().get("searchField");
    }

    private List executeNamedQuery(String namedQuery, UIComponent a4jSupport) {
        EntityQuery query = (EntityQuery) Component.getInstance(namedQuery);

        List<String> filterNames = new ArrayList<String>();
        Object filters = getFilters(a4jSupport);
        if (null != filters) {
            filterNames = processFilters(filters.toString());
        }

        return EntityQueryExecutorUtil.i.executeNamedQuery(query, filterNames);
    }

    private Object getAlias(UIComponent a4jSupport) {
        return a4jSupport.getAttributes().get("alias");
    }

    private Object getFilters(UIComponent a4jSupport) {
        return a4jSupport.getAttributes().get("filters");
    }

    private List executeGeneratedQuery(UIComponent a4jSupport, Class entityClass, Object searchField) {
        List<String> restrictions = getRestrictions(a4jSupport);

        String entityAlias = QuickSearchQueryUtil.i.getEntityAlias(entityClass);
        if (!restrictions.isEmpty()) {
            entityAlias = getAlias(a4jSupport).toString();
        }

        String basicRestriction = entityAlias
                + "." + searchField + " = #{quickSearchAction.searchValue}";

        restrictions.add(basicRestriction);

        log.debug("The restrictions: " + restrictions);
        EntityQuery query = QuickSearchQueryUtil.i.createEntityQuery(entityClass, entityAlias, restrictions);

        List<String> filterNames = new ArrayList<String>();
        Object filters = getFilters(a4jSupport);
        if (null != filters) {
            filterNames = processFilters(filters.toString());
        }

        return EntityQueryExecutorUtil.i.executeNamedQuery(query, filterNames);
    }

    private void setResultValue(String inputHiddenValueId, UIComponent a4jSupport, List result) {
        UIInput inputHiddenValue = (UIInput) JSFUtil.findComponent(a4jSupport, inputHiddenValueId);
        ELContext elContext = EL.createELContext();

        if (!ValidatorUtil.isEmptyOrNull(result)) {
            Object actualValue = inputHiddenValue.getValueExpression(VALUE).getValue(elContext);

            Object newValue = result.get(0);
            if (null == actualValue || !actualValue.equals(newValue)) {
                inputHiddenValue.getValueExpression(VALUE).setValue(elContext, newValue);

                Object postSearchAction = getPostSearchAction(a4jSupport);
                if (null != postSearchAction && !"".equals(postSearchAction.toString().trim())) {
                    log.debug("Calling the postSearchAction method: " + postSearchAction);
                    ELEvaluatorUtil.i.getValue(postSearchAction.toString());
                }
            }

        } else {
            inputHiddenValue.getValueExpression(VALUE).setValue(elContext, null);
            FacesMessage message = new FacesMessage(MessageUtils.getMessage("Common.quickSearch.error.notFound"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(a4jSupport.getParent().getClientId(facesContext), message);
        }
    }

    private Class getExpectedClass(String inputHiddenValueId, UIComponent baseComponent) {
        UIInput inputHiddenValue = (UIInput) JSFUtil.findComponent(baseComponent, inputHiddenValueId);
        ELContext elContext = EL.createELContext();
        return inputHiddenValue.getValueExpression(VALUE).getType(elContext);
    }

    private String getInputHiddenValueId(String id) {
        return id + "_" + INPUT_HIDDEN_VALUE_ID;
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

    public Object getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(Object searchValue) {
        this.searchValue = searchValue;
    }

    public void evaluateTextValue(String el, UIComponent component) {
        Object value = ELEvaluatorUtil.i.getValue(el);
        log.debug("The actual value is: " + value);

        this.searchValue = value;

        //update the value in the input component because the JSF render phase was passed
        ((UIInput) component).setValue(value);
    }
}
