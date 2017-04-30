package com.encens.khipus.converter;

import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.NaturalNumberValidator;
import com.sun.faces.util.MessageFactory;

import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import static com.encens.khipus.util.NaturalNumberValidator.ValidationCode.*;

/**
 * A converter for <code>Integer</code> inputs, it verifies that the value is a positive integer number.
 *
 * @author
 * @version 1.1.8
 */

public class NaturalNumberConverter implements Converter, StateHolder {

    public static final String CONVERTER_ID = "numberConverter";

    private static final String INVALID_NUMBER_MESSAGE_KEY = "converter.custom.naturalNumber.invalid";

    private static final String POSITIVE_NUMBER_MESSAGE_KEY = "converter.custom.naturalNumber.positive";

    private static final String NUMBER_MESSAGE_KEY = "converter.custom.naturalNumber";

    private boolean transientFlag = false;

    private String pattern = null;

    private Locale locale = null;

    private String forId = null;


    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        if (null == value || "".equals(value.trim())) {
            return null;
        }

        NaturalNumberValidator numberValidator = new NaturalNumberValidator(getLocale(), getPattern());

        NaturalNumberValidator.ValidationCode validationCode = numberValidator.validate(value);

        if (INVALID_NUMBER.equals(validationCode)) {
            throw new ConverterException(MessageFactory.getMessage(facesContext,
                    NUMBER_MESSAGE_KEY, value));
        }

        if (DECIMAL_SYMBOL_PRESENT.equals(validationCode)) {
            throw new ConverterException(MessageFactory.getMessage(facesContext,
                    INVALID_NUMBER_MESSAGE_KEY, value));
        }

        Number returnValue;
        if (null == getPattern() || "".equals(getPattern().trim())) {
            if (INVALID_PATTERN_USAGE.equals(validationCode)) {
                throw new ConverterException(MessageFactory.getMessage(facesContext,
                        INVALID_NUMBER_MESSAGE_KEY, value));
            }

            returnValue = parseNumberWithoutPattern(value);
        } else {
            if (INVALID_GROUP_SYMBOL_USAGE.equals(validationCode)) {
                throw new ConverterException(MessageFactory.getMessage(facesContext,
                        INVALID_NUMBER_MESSAGE_KEY, value));
            }

            returnValue = parseNumberWithPattern(value);
        }

        if (null == returnValue) {
            throw new ConverterException(MessageFactory.getMessage(facesContext,
                    NUMBER_MESSAGE_KEY, value));
        }

        if (returnValue.intValue() < 0) {
            throw new ConverterException(MessageFactory.getMessage(facesContext,
                    POSITIVE_NUMBER_MESSAGE_KEY, returnValue));
        }

        if (null == forId) {
            return returnValue.intValue();
        }

        Class expectedClass = getExpectedClassType(uiComponent, facesContext);
        if (java.lang.String.class.equals(expectedClass)) {
            return value;
        } else {
            return returnValue.intValue();
        }
    }


    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        if (null == value) {
            return "";
        }

        if (null != this.getPattern() && !"".equals(this.getPattern().trim())) {
            return formatNumber(value);
        }

        return value.toString();
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Locale getLocale() {
        if (null == this.locale) {
            this.locale = getLocale(FacesContext.getCurrentInstance());
        }

        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
    }

    public Object saveState(FacesContext facesContext) {
        Object[] values = new Object[3];
        values[0] = forId;
        values[1] = pattern;
        values[2] = locale;
        return values;
    }

    public void restoreState(FacesContext facesContext, Object o) {
        Object[] values = (Object[]) o;
        forId = (String) values[0];
        pattern = (String) values[1];
        locale = (Locale) values[2];
    }

    public boolean isTransient() {
        return transientFlag;
    }

    public void setTransient(boolean b) {
        this.transientFlag = b;
    }

    private String formatNumber(Object number) {
        if (number instanceof String) {
            return (String) number;
        }


        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(getLocale());
        NumberFormat parser = new DecimalFormat(pattern, decimalFormatSymbols);
        return parser.format(number);
    }

    private Locale getLocale(FacesContext context) {
        return context.getViewRoot().getLocale();
    }

    private Class getExpectedClassType(UIComponent uiComponent, FacesContext facesContext) {
        UIComponent component = JSFUtil.findComponent(uiComponent, forId);
        ValueExpression valueExpression = component.getValueExpression("value");

        try {
            return valueExpression.getType(facesContext.getELContext());
        } catch (Exception e) {
            //
        }

        return null;
    }

    private Number parseNumberWithoutPattern(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Number parseNumberWithPattern(String value) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(getLocale());
        NumberFormat parser = new DecimalFormat(pattern, decimalFormatSymbols);
        parser.setParseIntegerOnly(true);
        try {
            return parser.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
