package com.encens.khipus.validator;

import com.encens.khipus.util.JSFUtil;
import org.jboss.seam.faces.FacesMessages;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;

/**
 * Validates equality between a property and other one.
 *
 * @author
 * @version 1.0
 */

public class EqualValidator implements javax.faces.validator.Validator, Serializable {


    public static final String MESSAGE_KEY = "validator.custom.equal.invalid";
    private static final long serialVersionUID = -2327975783893629683L;
    private String forId;
    private String forLabel;


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

        EditableValueHolder foreignEditableValueHolder = JSFUtil.findEditableComponent(uiComponent, forId);
        if (foreignEditableValueHolder.isRequired() && foreignEditableValueHolder.getValue() == null) {
            return;
        }
        if (foreignEditableValueHolder.getValue() == null ||
                !foreignEditableValueHolder.getValue().toString().equals(value.toString())) {

            FacesMessage message = FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, MESSAGE_KEY, null,
                    getForLabel());
            throw new ValidatorException(message);
        }
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

}
