package com.encens.khipus.validator;

import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.faces.FacesMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author
 * @version 3.2
 */
public class TimeRangeValidator implements javax.faces.validator.Validator, Serializable {

    public static final String INVALID_LESS_MESSAGE_KEY = "validator.custom.rangeValidator.invalidLess";
    public static final String INVALID_GREATER_MESSAGE_KEY = "validator.custom.rangeValidator.invalidGreater";
    public static final String LESS_TYPE = "less";
    public static final String GREATER_TYPE = "greater";
    private String forId;
    private String forLabel;
    private Date forValue;
    private String type;


    public void validate(FacesContext facesContext, UIComponent uiComponent, Object value) throws ValidatorException {
        if (facesContext == null) {
            throw new NullPointerException("facesContext is null");
        }
        if (uiComponent == null) {
            throw new NullPointerException("uiComponent is null");
        }
        if (value == null) {
            return;
        }
        if (ValidatorUtil.isBlankOrNull(getType())) {
            setType(GREATER_TYPE);
        }
        Date dateValue;
        Calendar current = Calendar.getInstance();
        if (value != null) {
            dateValue = (Date) value;
            current.set(Calendar.HOUR, dateValue.getHours());
            current.set(Calendar.MINUTE, dateValue.getMinutes());
            current.set(Calendar.SECOND, dateValue.getSeconds());
            current.set(Calendar.MILLISECOND, 0);
        }
        if (getForId() != null) {
            EditableValueHolder editableValueHolder = JSFUtil.findEditableComponent(uiComponent, getForId());
            Date foreignValue = (Date) editableValueHolder.getValue();
            if (editableValueHolder.isRequired() && foreignValue == null) {
                return;
            }

            Calendar foreign = Calendar.getInstance();

            if (foreignValue != null) {
                foreign.set(Calendar.HOUR, foreignValue.getHours());
                foreign.set(Calendar.MINUTE, foreignValue.getMinutes());
                foreign.set(Calendar.SECOND, foreignValue.getSeconds());
                foreign.set(Calendar.MILLISECOND, 0);
            }

            int comparison = foreign.getTime().compareTo(current.getTime());
            if (foreignValue != null && (comparison == getValidationValue() || comparison == 0)) {
                throw new ValidatorException(FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, getMessage(), null, getForLabel()));
            }
        } else if (getForValue() != null) {
            Calendar forVal = Calendar.getInstance();
            forVal.set(Calendar.HOUR, getForValue().getHours());
            forVal.set(Calendar.MINUTE, getForValue().getMinutes());
            forVal.set(Calendar.SECOND, getForValue().getSeconds());
            forVal.set(Calendar.MILLISECOND, 0);

            int comparison = forVal.getTime().compareTo(current.getTime());
            if (comparison == getValidationValue() || comparison == 0) {
                throw new ValidatorException(FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, getMessage(), null, getForLabel()));
            }
        }
    }

    public String getMessage() {
        return LESS_TYPE.equals(getType()) ? INVALID_LESS_MESSAGE_KEY : INVALID_GREATER_MESSAGE_KEY;
    }

    public int getValidationValue() {
        return LESS_TYPE.equals(getType()) ? -1 : 1;
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

    public Date getForValue() {
        return forValue;
    }

    public void setForValue(Date forValue) {
        this.forValue = forValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
