package com.encens.khipus.converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * This converter turns an empty string or white spaces (coming from client) into a null String.
 * This is done because it's not required to save empty strings in the database, if the
 * string is empty, it must be saved as null.
 *
 * @author
 * @version $Id: EmptyStringToNullConverter.java 2008-8-21 14:52:40 $
 */
@Name("EmptyStringToNullConverter")
@BypassInterceptors
@Converter(forClass = java.lang.String.class)
public class EmptyStringToNullConverter implements javax.faces.convert.Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String string) {
        if ("".equals(string.trim())) {
            return null;
        } else {
            return string;
        }
    }

    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object string) {
        return string.toString();
    }
}
