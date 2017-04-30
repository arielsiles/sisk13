package com.encens.khipus.converter;

import com.encens.khipus.util.JSFUtil;
import com.sun.faces.util.MessageFactory;
import org.jboss.seam.Component;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.NumberConverter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Converter for Double and BigDecimal numbers.
 * <p/>
 * The pattern can be defined in the tag through <code>pattern</code> attribute, if this attribute is not definded
 * in the tag the converter find a default pattern in properties files, this pattern should be registred under key
 * 'patterns.decimalNumber' finally if the converter cannot found a default pattern in properties file for some reason
 * they use a hardcoded defauld pattern this is '#,##0.00'
 * <p/>
 * The pattern is used to validate the number specially de decimal part because of this is important to define the
 * correct pattern in the tag or in a properties file.
 *
 * @author
 */


public class RealNumberConverter extends NumberConverter implements javax.faces.component.StateHolder {

    private static final String DEFAULT_PATTERN = "#,##0.00";

    private static final String DEFAULT_PATTERN_KEY = "patterns.decimalNumber";

    private static final String INVALID_DECIMAL_PART_MESSAGE_KEY = "converter.custom.realNumber.decimalPart.invalid";

    private static final String INVALID_MINIMUM_VALUE_MESSAGE_KEY = "converter.custom.realNumber.minimum.invalid";

    private static final String INVALID_MAXIMUM_VALUE_MESSAGE_KEY = "converter.custom.realNumber.maximum.invalid";

    private String minimum = null;

    private String maximum = null;

    private String forId = null;


    /**
     * Validate the input value, exists three validations
     * <p/>
     * 1. Decimal part validation .- Validate the decimal part digits, making a comparison between definded pattern and
     * the input value.
     * <p/>
     * 2. minimum value validation .- This validation is executed only when <code>minimum</code> attribute are present
     * in the tag and verifies that the input value is greather or equal to minimum value.
     * <p/>
     * 3. maximum value validation .- This validation is executed only when <code>maximum</code> attribute are present
     * in the tag and verifies that the input value is less or equal to maximum value.
     * <p/>
     * If all validations are passed successfully then return a <code>Object</code> that is valid number type.
     *
     * @param facesContext <code>FacesContext</code> object.
     * @param uiComponent  <code>UIComponent</code> object.
     * @param value        <code>String</code> object it is the value.
     * @return <code>Object</code> that is valid number type.
     */
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        Number numberObject = (Number) super.getAsObject(facesContext, uiComponent, value);

        if (null != numberObject) {
            BigDecimal number = null;

            if (numberObject instanceof Double) {
                number = new BigDecimal(String.valueOf(numberObject.doubleValue()));
            }

            if (numberObject instanceof Long) {
                number = new BigDecimal(String.valueOf(numberObject.longValue()));
            }

            if (!checkDecimalPart(number)) {
                throw new ConverterException(MessageFactory.getMessage(facesContext,
                        INVALID_DECIMAL_PART_MESSAGE_KEY,
                        getExample(number)));
            }

            if (!checkGroupSeparator(value)) {
                throw new ConverterException(MessageFactory.getMessage(facesContext,
                        INVALID_DECIMAL_PART_MESSAGE_KEY,
                        getExample(number)));
            }

            if (null != minimum && !checkMinimum(number)) {
                throw new ConverterException(MessageFactory.getMessage(facesContext,
                        INVALID_MINIMUM_VALUE_MESSAGE_KEY,
                        getExample(getBigDecimal(minimum, "minimum"))));
            }

            if (null != maximum && !checkMaximum(number)) {
                throw new ConverterException(MessageFactory.getMessage(facesContext,
                        INVALID_MAXIMUM_VALUE_MESSAGE_KEY,
                        getExample(getBigDecimal(maximum, "maximum"))));
            }

            if (null != forId) {
                Class expectedClass = getExpectedClassType(uiComponent, facesContext);

                if (java.math.BigDecimal.class.equals(expectedClass)) {
                    return number;
                }
            }
        }

        return numberObject;
    }

    @Override
    public Object saveState(FacesContext facesContext) {
        Object[] values = (Object[]) super.saveState(facesContext);

        if (null == values) {
            return values;
        }

        Object[] newValues = new Object[14];
        System.arraycopy(values, 0, newValues, 0, values.length);
        newValues[11] = minimum;
        newValues[12] = maximum;
        newValues[13] = forId;

        return newValues;
    }

    @Override
    public void restoreState(FacesContext facesContext, Object state) {
        super.restoreState(facesContext, state);
        if (null != state) {
            Object[] values = (Object[]) state;
            if (values.length == 14) {
                minimum = (String) values[11];
                maximum = (String) values[12];
                forId = (String) values[13];
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public String getPattern() {
        String pattern = super.getPattern();

        if (null != pattern && !"".equals(pattern.trim())) {
            return pattern;
        }

        Map<String, String> messages = (Map<String, String>) Component.getInstance("messages");
        if (null != messages) {
            return messages.get(DEFAULT_PATTERN_KEY);
        }

        return DEFAULT_PATTERN;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }


    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
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

    private boolean checkDecimalPart(BigDecimal number) {
        Integer decimalPartFromPattern = getDecimalPartFromPattern();
        Integer decimalPartFromNumber = getDecimalPartFromNumber(number);

        return decimalPartFromPattern.equals(decimalPartFromNumber) || decimalPartFromPattern > decimalPartFromNumber;
    }

    private boolean checkMinimum(BigDecimal number) {
        BigDecimal value = getBigDecimal(minimum, "minimum");
        return number.compareTo(value) == 0 || number.compareTo(value) == 1;
    }

    private boolean checkMaximum(BigDecimal number) {
        BigDecimal value = getBigDecimal(maximum, "maximum");
        return number.compareTo(value) == 0 || number.compareTo(value) == -1;
    }

    private Integer getDecimalPartFromNumber(BigDecimal number) {
        String numberAsString = String.valueOf(number.doubleValue());

        int pointIndex = numberAsString.indexOf('.');

        String decimalPart = numberAsString.substring(pointIndex + 1);
        return decimalPart.length();
    }

    private Integer getDecimalPartFromPattern() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(getLocale());
        DecimalFormat decimalFormat = new DecimalFormat(getPattern(), symbols);
        return decimalFormat.getMaximumFractionDigits();
    }

    private Object getExample(Object value) {
        NumberFormat numberFormat = getNumberFormat(getLocale());
        return numberFormat.format(value);
    }

    private NumberFormat getNumberFormat(Locale locale) {

        if (getPattern() == null && getType() == null) {
            throw new IllegalArgumentException("Either pattern or type must" +
                    " be specified.");
        }

        if (getPattern() != null) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            return (new DecimalFormat(getPattern(), symbols));
        } else if (getType().equals("currency")) {
            return (NumberFormat.getCurrencyInstance(locale));
        } else if (getType().equals("number")) {
            return (NumberFormat.getNumberInstance(locale));
        } else if (getType().equals("percent")) {
            return (NumberFormat.getPercentInstance(locale));
        } else {
            throw new ConverterException
                    (new IllegalArgumentException(getType()));
        }
    }

    private BigDecimal getBigDecimal(String value, String attributeName) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The attribute " + attributeName + " must be a valid number.");
        }
    }

    private boolean checkGroupSeparator(String value) {
        DecimalFormat decimalFormat = (DecimalFormat) getNumberFormat(getLocale());

        char decimalSeparator = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
        char groupSeparator = decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();

        String integerPart = value;
        if (-1 != value.indexOf(decimalSeparator)) {
            integerPart = value.substring(0, value.indexOf(decimalSeparator));
        }

        String[] elements = integerPart.split(String.valueOf("(\\" + groupSeparator + ")"));

        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            if (i == 0) {
                if (0 == element.length()) {
                    return false;
                }

                continue;
            }

            if (3 != element.length()) {
                return false;
            }
        }

        return true;
    }
}
