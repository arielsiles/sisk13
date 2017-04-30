package com.encens.khipus.action.common;

import com.encens.khipus.tag.RestrictionTagHandler;
import com.encens.khipus.tag.RestrictionTagUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.el.EL;
import org.jboss.seam.log.Log;
import org.richfaces.component.html.HtmlSuggestionBox;

import javax.el.ELContext;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.24
 */
@Name("suggestionBoxAction")
@Scope(ScopeType.PAGE)
public class SuggestionBoxAction {
    @Logger
    private Log log;

    private String searchField;

    private String searchCompoundFields;

    private String searchCompoundCondition;

    private String entityAlias;

    private String filters;

    private UIInput inputText;

    private UIInput inputHidden;

    private UIComponent a4jSupport;

    private HtmlSuggestionBox suggestionBox;

    private String searchValue;

    private String dataProcessorComponentName;

    public List search(Object suggest) {
        if (null != suggest && !ValidatorUtil.isBlankOrNull(suggest.toString())) {
            setSearchValue(suggest.toString());

            Class clazz = getExpectedClass();

            return this.getDataModel().getSuggestedElements(clazz,
                    getSearchFieldForSuggestedElements(),
                    this.entityAlias,
                    this.getFilters(),
                    getRestrictions());
        }

        return new ArrayList();
    }

    public void onValueChange(ValueChangeEvent event) {
        log.debug("event.getNewValue(): " + event.getNewValue());
        log.debug("event.getOldValue(): " + event.getOldValue());
        log.debug("event.getPhaseId(): " + event.getPhaseId());
        if (null != event.getNewValue()) {
            log.debug("working with new value: " + event.getNewValue().toString());
            setSearchValue(event.getNewValue().toString());

            Class clazz = getExpectedClass();

            List result = this.getDataModel().getElement(clazz,
                    getSearchFieldForSearchElement(),
                    this.entityAlias,
                    this.getFilters(),
                    getRestrictions());
            ELContext elContext = EL.createELContext();
            Object hiddenValue = null;
            if (null != result && !result.isEmpty()) {
                hiddenValue = result.get(0);
            }

            inputHidden.getValueExpression("value").setValue(elContext, hiddenValue);
        }
    }

    public UIInput getInputText() {
        return inputText;
    }

    public void setInputText(UIInput inputText) {
        this.inputText = inputText;
    }

    public UIInput getInputHidden() {
        return inputHidden;
    }

    public void setInputHidden(UIInput inputHidden) {
        this.inputHidden = inputHidden;
    }

    public UIComponent getA4jSupport() {
        return a4jSupport;
    }

    public void setA4jSupport(UIComponent a4jSupport) {
        this.a4jSupport = a4jSupport;
    }

    public String getSearchField() {
        return searchField;
    }

    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public String getSearchCompoundFields() {
        return searchCompoundFields;
    }

    public void setSearchCompoundFields(String searchCompoundFields) {
        this.searchCompoundFields = searchCompoundFields;
    }

    public String getSearchCompoundCondition() {
        return searchCompoundCondition;
    }

    public void setSearchCompoundCondition(String searchCompoundCondition) {
        this.searchCompoundCondition = searchCompoundCondition;
    }

    public String getSearchFieldForSuggestedElements() {
        return searchCompoundFields != null ? searchCompoundFields : searchField;
    }

    public String getSearchFieldForSearchElement() {
        return searchCompoundCondition != null ? searchCompoundCondition : searchField;
    }

    public String getEntityAlias() {
        return entityAlias;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getSearchValueForCompoundFields() {
        return searchValue != null && !ValidatorUtil.isBlankOrNull(getSearchCompoundFields()) && !ValidatorUtil.isBlankOrNull(getSearchCompoundCondition()) ? searchValue.replaceAll(" ", "%") : searchValue;
    }

    public String getDataProcessorComponentName() {
        return dataProcessorComponentName;
    }

    public void setDataProcessorComponentName(String dataProcessorComponentName) {
        if (!ValidatorUtil.isBlankOrNull(dataProcessorComponentName)) {
            this.dataProcessorComponentName = dataProcessorComponentName;
        }

        this.dataProcessorComponentName = null;
    }

    public HtmlSuggestionBox getSuggestionBox() {
        return suggestionBox;
    }

    public void setSuggestionBox(HtmlSuggestionBox suggestionBox) {
        this.suggestionBox = suggestionBox;
    }

    private Class getExpectedClass() {
        return inputHidden.getValueExpression("value").getType(EL.createELContext());
    }

    private DefaultSuggestionBoxDataModel getDataModel() {
        if (null != dataProcessorComponentName) {
            return (DefaultSuggestionBoxDataModel) Component.getInstance(dataProcessorComponentName);
        }

        return (DefaultSuggestionBoxDataModel) Component.getInstance("defaultSuggestionBoxDataModel");
    }

    @SuppressWarnings(value = "unchecked")
    private List<String> getRestrictions() {
        List<String> encodedRestrictions =
                (List<String>) a4jSupport.getAttributes().get(RestrictionTagHandler.LIST_NAME);

        return RestrictionTagUtil.i.decodeRestrictions(encodedRestrictions);
    }

    public void cleanInputText(UIComponent inputText) {
        ((UIInput) inputText).setValue("");
    }
}
