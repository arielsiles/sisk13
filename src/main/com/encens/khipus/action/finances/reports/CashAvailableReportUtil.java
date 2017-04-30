package com.encens.khipus.action.finances.reports;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Encens S.R.L.
 * Util to design the cash available report
 * is there defined keys to access in crosstab Map info
 *
 * @author
 * @version $Id: CashAvailableReportUtil.java  24-nov-2010 18:09:48$
 */
public class CashAvailableReportUtil {
    private CashAvailableReportUtil() {
    }

    public static String SEPARATOR_KEY = "_";
    public static String BANK_KEY = "Bank";
    public static String BANKACCOUNT_KEY = "BankAcc";
    public static String EXECUTORUNIT_KEY = "ExeUnit";

    public static String BALANCES_GROUP_KEY = "Balance";
    public static String DEPOSIT_MORNING_GROUP_KEY = "DepositM";
    public static String DEPOSIT_AFTERNOON_GROUP_KEY = "DepositA";
    public static String TOTALDEPOSIT_GROUP_KEY = "TDeposit";
    public static String TOTALDEPOSITBALANCES_GROUP_KEY = "TDepositBalance";
    public static String CHECKSRECEIVABLE_GROUP_KEY = "Check";
    public static String TOTALCHECKSRECEIVABLE_GROUP_KEY = "TCheck";
    public static String TOTALCASHAVAILABLE_GROUP_KEY = "TCashAvailable";

    public static String PARTIALTOTAL_BS_BALANCES_KEY = "PTBalanceBs";
    public static String EXCHANGE_RATE_KEY = "exchangeRate";
    public static String PARTIALTOTAL_BSSUS_BALANCES_KEY = "PTBalanceBsSus";
    public static String PARTIALTOTAL_SUS_BALANCES_KEY = "PTBalanceSus";
    public static String TOTAL_SUS_BALANCES_KEY = "TotalBalanceSus";

    public static String COLLECTIONBYSEDE_BS_GROUP_KEY = "CollectBs";
    public static String COLLECTIONBYSEDE_SUS_GROUP_KEY = "CollectSus";
    public static String TOTALCOLLECTION_BS_KEY = "TCollectBs";
    public static String TOTALCOLLECTION_SUS_KEY = "TCollectSus";

    public static String composeBalancesBankAccountKey(String financesBankId, String companyNumber, String accountNumber) {
        return BALANCES_GROUP_KEY + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeMorningDepositsKey(String executorUnitId, String financesBankId, String companyNumber, String accountNumber) {
        return DEPOSIT_MORNING_GROUP_KEY + SEPARATOR_KEY + EXECUTORUNIT_KEY + executorUnitId + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeAfternoonDepositsKey(String executorUnitId, String financesBankId, String companyNumber, String accountNumber) {
        return DEPOSIT_AFTERNOON_GROUP_KEY + SEPARATOR_KEY + EXECUTORUNIT_KEY + executorUnitId + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeTotalDepositsKey(String financesBankId, String companyNumber, String accountNumber) {
        return TOTALDEPOSIT_GROUP_KEY + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeTotalDepositMoreBalancesKey(String financesBankId, String companyNumber, String accountNumber) {
        return TOTALDEPOSITBALANCES_GROUP_KEY + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeChecksReceivableKey(String executorUnitId, String financesBankId, String companyNumber, String accountNumber) {
        return CHECKSRECEIVABLE_GROUP_KEY + SEPARATOR_KEY + EXECUTORUNIT_KEY + executorUnitId + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeTotalChecksReceivableKey(String financesBankId, String companyNumber, String accountNumber) {
        return TOTALCHECKSRECEIVABLE_GROUP_KEY + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    public static String composeTotalCashAvailableKey(String financesBankId, String companyNumber, String accountNumber) {
        return TOTALCASHAVAILABLE_GROUP_KEY + SEPARATOR_KEY + composeBankAccountColumnKey(financesBankId, companyNumber, accountNumber);
    }

    private static String composeBankAccountColumnKey(String financesBankId, String companyNumber, String accountNumber) {
        return BANK_KEY + financesBankId + SEPARATOR_KEY + BANKACCOUNT_KEY + companyNumber + SEPARATOR_KEY + accountNumber;
    }

    public static String composeCollectionBySedeBsKey(String executorUnitId) {
        return COLLECTIONBYSEDE_BS_GROUP_KEY + SEPARATOR_KEY + EXECUTORUNIT_KEY + executorUnitId;
    }

    public static String composeCollectionBySedeSusKey(String executorUnitId) {
        return COLLECTIONBYSEDE_SUS_GROUP_KEY + SEPARATOR_KEY + EXECUTORUNIT_KEY + executorUnitId;
    }

    /**
     * get crosstab info value
     *
     * @param groupIdentifier
     * @param crosstabInfoMap
     * @param executorUnitId
     * @param financesBankId
     * @param companyNumber
     * @param accountNumber
     * @return BigDecimal
     */
    public static BigDecimal getCrosstabRowColumnValue(String groupIdentifier, Map crosstabInfoMap, Object executorUnitId, String financesBankId, String companyNumber, String accountNumber) {
        BigDecimal rowColumnValue = null;
        String key = null;
        if (crosstabInfoMap != null) {
            if (BALANCES_GROUP_KEY.equals(groupIdentifier)) {
                key = composeBalancesBankAccountKey(financesBankId, companyNumber, accountNumber);
            } else if (DEPOSIT_MORNING_GROUP_KEY.equals(groupIdentifier)) {
                key = composeMorningDepositsKey(executorUnitId.toString(), financesBankId, companyNumber, accountNumber);
            } else if (DEPOSIT_AFTERNOON_GROUP_KEY.equals(groupIdentifier)) {
                key = composeAfternoonDepositsKey(executorUnitId.toString(), financesBankId, companyNumber, accountNumber);
            } else if (TOTALDEPOSIT_GROUP_KEY.equals(groupIdentifier)) {
                key = composeTotalDepositsKey(financesBankId, companyNumber, accountNumber);
            } else if (TOTALDEPOSITBALANCES_GROUP_KEY.equals(groupIdentifier)) {
                key = composeTotalDepositMoreBalancesKey(financesBankId, companyNumber, accountNumber);
            } else if (CHECKSRECEIVABLE_GROUP_KEY.equals(groupIdentifier)) {
                key = composeChecksReceivableKey(executorUnitId.toString(), financesBankId, companyNumber, accountNumber);
            } else if (TOTALCHECKSRECEIVABLE_GROUP_KEY.equals(groupIdentifier)) {
                key = composeTotalChecksReceivableKey(financesBankId, companyNumber, accountNumber);
            } else if (TOTALCASHAVAILABLE_GROUP_KEY.equals(groupIdentifier)) {
                key = composeTotalCashAvailableKey(financesBankId, companyNumber, accountNumber);
            }

            if (key != null && crosstabInfoMap.containsKey(key)) {
                rowColumnValue = (BigDecimal) crosstabInfoMap.get(key);
            }
        }

        return rowColumnValue;
    }

    /**
     * get crosstab info value
     *
     * @param groupIdentifier
     * @param crosstabInfoMap
     * @param executorUnitId
     * @return BigDecimal
     */
    public static BigDecimal getCrosstabColumnValue(String groupIdentifier, Map crosstabInfoMap, Object executorUnitId) {
        BigDecimal columnValue = null;
        String key = null;
        if (crosstabInfoMap != null) {
            if (COLLECTIONBYSEDE_BS_GROUP_KEY.equals(groupIdentifier)) {
                key = composeCollectionBySedeBsKey(executorUnitId.toString());
            } else if (COLLECTIONBYSEDE_SUS_GROUP_KEY.equals(groupIdentifier)) {
                key = composeCollectionBySedeSusKey(executorUnitId.toString());
            }

            if (key != null && crosstabInfoMap.containsKey(key)) {
                columnValue = (BigDecimal) crosstabInfoMap.get(key);
            }
        }
        return columnValue;
    }

    /**
     * get crostab info value by key
     *
     * @param crosstabInfoMap
     * @param valueKey
     * @return BigDecimal
     */
    public static BigDecimal getCrossTabInfoValue(Map crosstabInfoMap, String valueKey) {
        BigDecimal value = null;
        if (valueKey != null && crosstabInfoMap.containsKey(valueKey)) {
            value = (BigDecimal) crosstabInfoMap.get(valueKey);
        }
        return value;
    }
}
