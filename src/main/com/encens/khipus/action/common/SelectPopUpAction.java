package com.encens.khipus.action.common;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.tag.CriteriaValueTagHandler;
import com.encens.khipus.util.ListEntityManagerName;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author
 * @version 2.22
 */
@Name("selectPopUpAction")
public class SelectPopUpAction {

    @Logger
    private Log log;

    public void select(ActionEvent actionEvent) {
        UIComponent commandLink = actionEvent.getComponent();

        QueryDataModel queryDataModel = (QueryDataModel) commandLink.getAttributes().get("dataModelObject");

        if (null != queryDataModel) {
            queryDataModel.clear();

            String entityManagerName = (String) commandLink.getAttributes().get("entityManagerName");
            if (ValidatorUtil.isBlankOrNull(entityManagerName)) {
                entityManagerName = ListEntityManagerName.DEFAULT_LIST.getName();
            }

            log.debug("SelectPopUp work wth :");
            log.debug("queryDataModel: " + queryDataModel.getClass().getName());
            log.debug("entityManagerName : " + entityManagerName);
            queryDataModel.setEntityManagerName(entityManagerName);

            Map<String, Object> values = getCriteriaValues(commandLink);
            if (null != values && !values.isEmpty()) {
                log.debug("criteria values map: " + values);
                setCriteriaValues(values, queryDataModel.getCriteria().getClass(), queryDataModel.getCriteria());
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    private Map<String, Object> getCriteriaValues(UIComponent commandLink) {
        return (Map<String, Object>) commandLink.getAttributes().get(CriteriaValueTagHandler.MAP_NAME);
    }

    private <T> void setCriteriaValues(Map<String, Object> values, Class clazz, T instance) {

        Set<String> criteriaFieldNames = values.keySet();

        List<Field> fields = new ArrayList<Field>();
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (criteriaFieldNames.contains(field.getName())) {
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                field.set(instance, values.get(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot set values for "
                        + instance.getClass().getName()
                        + " criteria Object", e);
            }
        }
    }
}
