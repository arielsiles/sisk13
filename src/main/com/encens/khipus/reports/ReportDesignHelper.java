package com.encens.khipus.reports;

import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements methods for help to the report design task
 *
 * @author
 * @version 2.17
 */
public class ReportDesignHelper {
    public static String KEYVALUESEPARATOR = "<>";

    private ReportDesignHelper() {
    }

    /**
     * Return the month's name
     *
     * @param monthNumber The month number to process
     * @return The month's name
     */
    public static String getMonthName(String monthNumber) {
        String res = null;
        if (monthNumber != null && monthNumber.length() > 0) {
            Month month = Month.getMonth(new Integer(monthNumber));
            res = MessageUtils.getMessage(month.getResourceKey());
        }
        return res;
    }


    /**
     * Return 'Debit' or 'Credit' resource value when apply
     *
     * @param accountClass The account.accountClass
     * @return A string
     */
    public static String getAccountClassName(String accountClass) {
        String res = "";
        if (accountClass.equals("G")) {
            res = MessageUtils.getMessage("Reports.finances.creditDebitComparative.debits");
        } else {
            res = MessageUtils.getMessage("Reports.finances.creditDebitComparative.credits");
        }

        return res;
    }

    /**
     * Calculates the bsAmount(converted to dollars)+susAmount
     *
     * @param bsAmount   Amount in bs
     * @param susAmount  amount is sus
     * @param changeRate changeRate
     * @return the sum
     */
    public static Double getMonthTotal(Double bsAmount, Double susAmount, Double changeRate) {
        Double res = null;
        if (bsAmount != null) {
            res = bsAmount / changeRate;
        }
        if (susAmount != null) {
            if (res != null) {
                res += susAmount;
            } else {
                res = susAmount;
            }
        }
        return (res);
    }

    /**
     * Get the bank account full name if the bank account is not null
     *
     * @param bankAccount BankAccount
     * @return the bankAccount
     */
    public static String getBankAccountFullName(FinancesBankAccount bankAccount) {
        String res = null;
        if (bankAccount != null) {
            res = bankAccount.getFullName();
        }
        return (res);
    }

    /**
     * calculate value only as $us
     *
     * @param bsValue      value as Bs
     * @param susValue     value as $us
     * @param exchangeRate exchange rate
     * @return BigDecimal value as $us
     */
    public static BigDecimal calculateValueAsSus(BigDecimal bsValue, BigDecimal susValue, BigDecimal exchangeRate) {
        BigDecimal totalValue = null;

        if (bsValue != null && susValue != null && exchangeRate != null) {
            totalValue = BigDecimalUtil.sum(susValue, BigDecimalUtil.divide(bsValue, exchangeRate));
        }
        return totalValue;
    }

    /**
     * calculate value only as $us
     *
     * @param bsValue      value as Bs
     * @param exchangeRate exchange rate
     * @return BigDecimal value as $us
     */
    public static BigDecimal calculateValueAsSus(BigDecimal bsValue, BigDecimal exchangeRate) {
        BigDecimal totalValue = null;
        if (bsValue != null && exchangeRate != null) {
            totalValue = BigDecimalUtil.divide(bsValue, exchangeRate);
        }
        return totalValue;
    }

    /**
     * calculate value only as $us
     *
     * @param susValue     value as Sus
     * @param exchangeRate exchange rate
     * @return BigDecimal value as Bs
     */
    public static BigDecimal calculateValueAsBs(BigDecimal susValue, BigDecimal exchangeRate) {
        BigDecimal totalValue = null;
        if (susValue != null && exchangeRate != null) {
            totalValue = BigDecimalUtil.multiply(susValue, exchangeRate);
        }
        return totalValue;
    }

    /**
     * sum BigDecimal value in map with this key
     *
     * @param resourceKey map key
     * @param value       BigDecimal value
     * @param map         Map
     * @return Map
     */
    public static Map<String, BigDecimal> sumBigDecimalCurrencyMap(String resourceKey, BigDecimal value, Map<String, BigDecimal> map) {
        if (value != null) {
            BigDecimal sumValue = value;
            if (map.containsKey(resourceKey)) {
                sumValue = BigDecimalUtil.sum(map.get(resourceKey), value);
            }
            map.put(resourceKey, sumValue);
        }
        return map;
    }

    /**
     * subtract BigDecimal value in map with this key
     *
     * @param resourceKey map key
     * @param value       BigDecimal value
     * @param map         Map
     * @return Map
     */
    public static Map<String, BigDecimal> subtractBigDecimalCurrencyMap(String resourceKey, BigDecimal value, Map<String, BigDecimal> map) {
        if (value != null) {
            BigDecimal subtractValue = BigDecimalUtil.negate(value);
            if (map.containsKey(resourceKey)) {
                subtractValue = BigDecimalUtil.subtract(map.get(resourceKey), value);
            }
            map.put(resourceKey, subtractValue);
        }
        return map;
    }

    /**
     * Format with decimal pattern the all Map values
     *
     * @param map
     * @param locale
     * @return String
     */
    public static String formatBigDecimalCurrencyMap(Map<String, BigDecimal> map, Locale locale) {
        String formatedValue = "";
        for (String resourseKey : map.keySet()) {
            String amountStr = FormatUtils.formatNumber(map.get(resourseKey), MessageUtils.getMessage("patterns.decimalNumber"), locale != null ? locale : Locale.getDefault());
            String currency = MessageUtils.getMessage(resourseKey);

            if (!formatedValue.isEmpty()) {
                formatedValue += "\n";
            }
            formatedValue += amountStr + " " + currency;
        }

        return formatedValue;
    }

    /**
     * Format with decimal pattern the all Map values
     *
     * @param map
     * @param resourseKey
     * @param locale
     * @return String
     */
    public static String formatBigDecimal(Map<String, BigDecimal> map, String resourseKey, Locale locale) {
        String formatedValue = "";
        if (map.get(resourseKey) != null) {
            formatedValue = FormatUtils.formatNumber(map.get(resourseKey), MessageUtils.getMessage("patterns.decimalNumber"), locale != null ? locale : Locale.getDefault());
        }
        return formatedValue;
    }

    /**
     * Format amount with currency
     *
     * @param value            BigDecimal value
     * @param currencyResource curerncy resource
     * @param locale           locale
     * @return String
     */
    public static String formatBigDecimalCurrency(BigDecimal value, String currencyResource, Locale locale) {
        String formatedValue = "";
        if (value != null) {
            formatedValue = FormatUtils.formatNumber(value, MessageUtils.getMessage("patterns.decimalNumber"), locale != null ? locale : Locale.getDefault());
            if (currencyResource != null) {
                formatedValue += " " + MessageUtils.getMessage(currencyResource);
            }
        }
        return formatedValue;
    }

    public static Long getFieldAsLong(JRDefaultScriptlet scriptlet, String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = scriptlet.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }

    public static Integer getFieldAsInteger(JRDefaultScriptlet scriptlet, String fieldName) throws JRScriptletException {
        Integer value = null;
        Object fieldObj = scriptlet.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            value = new Integer(fieldObj.toString());
        }
        return value;
    }

    public static BigDecimal getFieldAsBigDecimal(JRDefaultScriptlet scriptlet, String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = scriptlet.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }

    public static String getFieldAsString(JRDefaultScriptlet scriptlet, String fieldName) throws JRScriptletException {
        String stringValue = null;
        Object fieldObj = scriptlet.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            stringValue = String.valueOf(fieldObj);
        }
        return stringValue;
    }

    public static Date getFieldAsDate(JRDefaultScriptlet scriptlet, String fieldName) throws JRScriptletException {
        Date dateValue = null;
        Object fieldObj = scriptlet.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            dateValue = (Date) fieldObj;
        }
        return dateValue;
    }


    /**
     * Compose an keyvalue string with separator as '<>'
     * like: 10<>someValue
     *
     * @param key
     * @param value
     * @return String
     */
    public static String composeKeyValue(Object key, Object value) {
        return (new String()) + key + KEYVALUESEPARATOR + value;
    }

    /**
     * Get the key of keyvalue string
     * like: 10<>someValue => 10
     *
     * @param keyValue
     * @return String
     */
    public static String getKeyOfKeyValue(String keyValue) {
        String key = null;
        if (keyValue != null) {
            int indexSeparator = keyValue.indexOf(KEYVALUESEPARATOR);
            if (indexSeparator > -1) {
                key = keyValue.substring(0, indexSeparator);
            }
        }
        return key;
    }

    /**
     * Get the value of keyvalue string
     * like: 10<>someValue => someValue
     *
     * @param keyValue
     * @return String
     */
    public static String getValueOfKeyValue(String keyValue) {
        String value = null;
        if (keyValue != null) {
            int indexSeparator = keyValue.indexOf(KEYVALUESEPARATOR);
            if (indexSeparator > -1 && keyValue.length() > indexSeparator + KEYVALUESEPARATOR.length()) {
                value = keyValue.substring(indexSeparator + KEYVALUESEPARATOR.length());
            }
        }
        return value;
    }

}
