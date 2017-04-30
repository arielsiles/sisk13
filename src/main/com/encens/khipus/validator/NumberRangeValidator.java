package com.encens.khipus.validator;

import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.faces.FacesMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;

/**
 * NumberRangeValidator
 *
 * @author
 * @version 2.8
 */

public class NumberRangeValidator implements javax.faces.validator.Validator, Serializable {

    public static final String INVALID_LESS_MESSAGE_KEY = "validator.custom.rangeValidator.invalidLess";
    public static final String INVALID_LESS_EQUAL_MESSAGE_KEY = "validator.custom.rangeValidator.invalidLessEqual";
    public static final String INVALID_GREATER_MESSAGE_KEY = "validator.custom.rangeValidator.invalidGreater";
    public static final String INVALID_GREATER_EQUAL_MESSAGE_KEY = "validator.custom.rangeValidator.invalidGreaterEqual";
    public static final String LESS_TYPE = "less";
    public static final String LESS_EQUAL_TYPE = "lessEqual";
    public static final String GREATER_TYPE = "greater";
    public static final String GREATER_EQUAL_TYPE = "greaterEqual";
    private String forId;
    private String forLabel;
    private Number forValue;
    private String type;

    public void validate(FacesContext facesContext, UIComponent uiComponent, Object value) throws ValidatorException {
        if (facesContext == null) {
            throw new NullPointerException("facesContext is null");
        }
        if (uiComponent == null) {
            throw new NullPointerException("uiComponent is null");
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("the current value isn't a number instance");
        }
        if (value == null) {
            return;
        }
        if (ValidatorUtil.isBlankOrNull(getType())) {
            setType(GREATER_TYPE);
        }
        if (getForId() != null) {
            EditableValueHolder editableValueHolder = JSFUtil.findEditableComponent(uiComponent, getForId());
            Object foreignValue = editableValueHolder.getValue();
            if (editableValueHolder.isRequired() && foreignValue == null) {
                return;
            }
            if (foreignValue != null && !isValidRange(value, foreignValue)) {
                throw new ValidatorException(FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, getMessage(), null, getForLabel()));
            }
        } else if (getForValue() != null && !isValidRange(value, getForValue())) {
            throw new ValidatorException(FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, getMessage(), null, getForLabel()));
        }
    }

    public String getMessage() {
        String resourceMessage = "";
        if (GREATER_TYPE.equals(getType())) {
            resourceMessage = INVALID_GREATER_MESSAGE_KEY;
        } else if (GREATER_EQUAL_TYPE.equals(getType())) {
            resourceMessage = INVALID_GREATER_EQUAL_MESSAGE_KEY;
        } else if (LESS_TYPE.equals(getType())) {
            resourceMessage = INVALID_LESS_MESSAGE_KEY;
        } else if (LESS_EQUAL_TYPE.equals(getType())) {
            resourceMessage = INVALID_LESS_EQUAL_MESSAGE_KEY;
        }
        return resourceMessage;
    }

    public boolean isValidRange(Object value, Object foreignValue) {
        boolean validRange = true;
        int evaluation = BigDecimalUtil.compareTo(value, foreignValue);

        if (GREATER_TYPE.equals(getType())) {
            validRange = evaluation == 1;
        } else if (GREATER_EQUAL_TYPE.equals(getType())) {
            validRange = (evaluation == 1 || evaluation == 0);
        } else if (LESS_TYPE.equals(getType())) {
            validRange = evaluation == -1;
        } else if (LESS_EQUAL_TYPE.equals(getType())) {
            validRange = (evaluation == -1 || evaluation == 0);
        }
        return validRange;
    }

    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
    }

    public String getForLabel() {
        return forLabel;
    }

    public void setForLabel(String forLabel) {
        this.forLabel = forLabel;
    }

    public Number getForValue() {
        return forValue;
    }

    public void setForValue(Number forValue) {
        this.forValue = forValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}