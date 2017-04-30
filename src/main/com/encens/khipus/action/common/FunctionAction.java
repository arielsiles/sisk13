package com.encens.khipus.action.common;

import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : FunctionAction, 19-11-2009 07:32:07 PM
 */
@Name("functionAction")
@Scope(ScopeType.PAGE)
public class FunctionAction implements Serializable {

    @In
    protected FacesMessages facesMessages;

    public String hourMinuteToString(Integer time) {
        if (time != null) {
            String resultTime = time + "m";

            if (time >= 60) {
                resultTime = (time / 60) + "h";
                if (time % 60 > 0) {
                    resultTime += " " + (time % 60) + "m";
                }
            }
            return resultTime;
        }
        return "";
    }

    public Long nextValue(String attributeName) {
        Long currentValue = currentValue(attributeName) + 1;
        JSFUtil.setRequestAttribute(attributeName, currentValue);
        return currentValue;
    }

    public Long currentValue(String attributeName) {
        Long currentValue = (long) 0;
        if (JSFUtil.getRequestParameter(attributeName) != null) {
            currentValue = Long.valueOf(JSFUtil.getRequestParameter(attributeName));
        }
        return currentValue;
    }

    public String getStringValueAndConcat(Object object, String property, String beforeString, String afterString) {
        String stringValue = getStringValue(object, property);
        return !ValidatorUtil.isBlankOrNull(stringValue) ? beforeString + getStringValue(object, property) + afterString : "";
    }

    public String getStringValue(Object object, String property) {
        Object propertyValue = getPropertyValue(object, property);
        return propertyValue != null ? propertyValue.toString() : "";
    }

    public Object getPropertyValue(Object object, String property) {
        Object propertyValue = null;
        if (object != null) {
            try {
                Class clazz = object.getClass();
                Field field = clazz.getDeclaredField(property);
                field.setAccessible(true);
                propertyValue = field.get(object);
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException ex) {

            }
        }

        return propertyValue;
    }

    public Boolean getHasSeverityErrorMessages() {
        Boolean result = false;
        List<FacesMessage> facesMessageList = facesMessages.getCurrentMessages();
        for (int i = 0; i < facesMessageList.size() && !result; i++) {
            result = FacesMessage.SEVERITY_ERROR.equals(facesMessageList.get(i).getSeverity());
        }
        return result;
    }

    public Date getToDay() {
        return DateUtils.toDay();
    }
}
