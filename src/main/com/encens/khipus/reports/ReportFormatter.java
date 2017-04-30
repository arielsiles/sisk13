package com.encens.khipus.reports;

import com.encens.khipus.util.MessageUtils;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Encens S.R.L.
 * This class implements methods to format values for reports
 *
 * @author
 */
public class ReportFormatter {

    private ReportFormatter() {
    }

    /**
     * Format a number
     *
     * @param number       The number to format
     * @param maxIntPart   Max Int part
     * @param maxFloatPart Max Float part
     * @param locale       The Locale
     * @return The formatted number
     */
    public static String formatNumber(Object number, Integer maxIntPart, Integer maxFloatPart, Locale locale) {
        String result = "";

        if (number != null) {
            NumberFormat numberFormat = settingUpValuesOfFormat(locale, maxIntPart, maxFloatPart);
            try {
                Double doubleNumber = new Double(number.toString());
                result = numberFormat.format(doubleNumber);
            } catch (NumberFormatException e) {
            }
        }
        return result;
    }

    /**
     * Format a date with a pattern and a timezone
     *
     * @param date     The value to be formatted
     * @param timeZone The timezone
     * @param pattern  The pattern
     * @return The formatted value
     */
    public static String getFormattedDateTimeWithTimeZone(Date date, TimeZone timeZone, String pattern) {
        String result = "";
        if (date != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
            dateFormatter.setTimeZone(timeZone);
            dateFormatter.setLenient(false);
            result = dateFormatter.format(date);
        }
        return result;
    }

    /**
     * Setting up the values for the instance an numberformat
     *
     * @param locale       the locale
     * @param maxIntPart   this the max of digits on the integer part
     * @param maxFloatPart this the max of digits on the decimal part
     * @return numberformat
     */
    private static NumberFormat settingUpValuesOfFormat(Locale locale, int maxIntPart, int maxFloatPart) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        numberFormat.setMaximumFractionDigits(maxFloatPart);
        numberFormat.setMaximumIntegerDigits(maxIntPart);
        numberFormat.setMinimumFractionDigits(maxFloatPart);
        return numberFormat;
    }

    /**
     * Format the person name
     *
     * @param firstName  First name
     * @param maidenName Maiden name
     * @param lastName   Last name
     * @return The person name
     */
    public static String getPersonName(String firstName, String maidenName, String lastName) {
        String result = "";
        if (lastName != null) {
            result += " " + lastName;
        }

        if (maidenName != null) {
            result += " " + maidenName;
        }

        if (firstName != null) {
            result += " " + firstName;
        }
        return (result);
    }

    /**
     * Format the person id number
     *
     * @param number    number
     * @param extension extension
     * @return The id number
     */
    public static String getPersonIdNumber(String number, String extension) {
        String result = number;
        if (extension != null) {
            result += " " + extension;
        }
        return (result);
    }

    public static void setChartColors(PiePlot plot, DefaultPieDataset dataset, Color[] colors) {
        {
            List<Comparable> keys = dataset.getKeys();
            int aInt;

            for (int i = 0; i < keys.size(); i++) {
                aInt = i % colors.length;
                plot.setSectionPaint(keys.get(i), colors[aInt]);
            }
        }
    }

    /**
     * This method convert a number of months to years
     *
     * @param months Number of months
     * @return Years equivalence
     */
    public static Double convertToYears(Integer months) {
        Double years = 0.0;
        years = (double) months / 12;
        return years;
    }

    /**
     * Return empty string when the text is null, else return the same text
     *
     * @param text Text to be evaluated
     * @return Text or empty string
     */
    public static String showNotNull(Object text) {
        String res = "";
        if (text != null) {
            res = text.toString();
        }
        return res;
    }

    /**
     * Format boolean value, TRUE=yes, FALSE=not
     *
     * @param value
     * @return String
     */
    public static String formatBooleanValue(Boolean value) {
        String formatedValue = null;
        if (value != null) {
            if (value) {
                formatedValue = MessageUtils.getMessage("Common.yes");
            } else {
                formatedValue = MessageUtils.getMessage("Common.not");
            }
        }
        return formatedValue;
    }

    /**
     * format the full name: code - name
     *
     * @param code The code
     * @param name The name or description
     * @return The full name
     */
    public static String formatFullName(String code, String name) {
        return (code != null ? code + " - " : "") + (name != null ? name : "");
    }
}
