package com.encens.khipus.converter;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;
import java.math.BigDecimal;
import java.util.Map;

/**
 * A converter for BigDecimal inputs
 *
 * @author
 * @version $Id: BigDecimalConverter.java 2008-9-9 11:22:57 $
 */
@Name("bigDecimalConverter")
@BypassInterceptors
@Converter(forClass = java.math.BigDecimal.class)
public class BigDecimalConverter extends NumberConverter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {

        Number number = (Number) super.getAsObject(facesContext, uiComponent, s);
        if (number != null) {
            if (number instanceof Double) {
                return new BigDecimal(number.doubleValue());
            }
            if (number instanceof Long) {
                return new BigDecimal(number.longValue());
            }
        }
        return number;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public String getPattern() {
        return ((Map<String, String>) Component.getInstance("messages")).get("patterns.decimalNumber");
    }
}
