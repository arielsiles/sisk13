package com.encens.khipus.service.finances;

import com.encens.khipus.action.finances.reports.CashAvailableReportUtil;
import com.encens.khipus.dashboard.component.factory.SqlQuery;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.model.academics.ExecutorUnit;
import com.encens.khipus.model.finances.FinancesBankAccount;
import com.encens.khipus.model.finances.FinancesBankAccountPk;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.FinancesExecutorUnit;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.query.QueryUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * Service to calculate all values for crosstab in cash available report
 *
 * @author
 * @version $Id: CashAvailableReportServiceBean.java  24-nov-2010 17:16:33$
 */
@Stateless
@Name("cashAvailableReportService")
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class CashAvailableReportServiceBean implements CashAvailableReportService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    private static final String FINANCESBANKID_KEY = "financesBankId";
    private static final String COMPANYNUMBER_KEY = "companyNumber";
    private static final String ACCOUNTNUMBER_KEY = "accountNumber";

    /**
     * calculate al data info and set in Map
     *
     * @param currentDate current date
     * @return Map
     */
    public Map<String, Object> calculateCashAvailableCrossTabInfoData(Date currentDate) {
        log.debug("Executing calculateCashAvailableCrossTabInfoData...........");
        Map<String, Object> crossTabMap = new HashMap<String, Object>();

        List<Map> bankAccountInfoMapList = findBankAccountCrossTabSelect();

        crossTabMap.putAll(processBalancesCrossTabInfo(bankAccountInfoMapList, currentDate));
        processMorningDepositCrossTabInfo(crossTabMap, bankAccountInfoMapList, currentDate);
        processAfternoonDepositCrossTabInfo(crossTabMap, bankAccountInfoMapList, currentDate);
        processChecksReceivableCrossTabInfo(crossTabMap, bankAccountInfoMapList, currentDate);
        processCashAvailableAndBalancesTotal(crossTabMap, bankAccountInfoMapList);
        return crossTabMap;
    }

    /**
     * process balances data info
     *
     * @param bankAccountInfoMapList
     * @param currentDate
     * @return Map
     */
    private Map<String, Object> processBalancesCrossTabInfo(List<Map> bankAccountInfoMapList, Date currentDate) {
        Map<String, Object> crossTabMap = new HashMap<String, Object>();
        for (Map bankAccountInfoMap : bankAccountInfoMapList) {
            String financesBankId = (String) bankAccountInfoMap.get(FINANCESBANKID_KEY);
            String companyNumber = (String) bankAccountInfoMap.get(COMPANYNUMBER_KEY);
            String accountNumber = (String) bankAccountInfoMap.get(ACCOUNTNUMBER_KEY);
            BigDecimal balancesSum = sumAccountingMovementDetailAmount(financesBankId, companyNumber, accountNumber, currentDate);

            crossTabMap.put(CashAvailableReportUtil.composeBalancesBankAccountKey(financesBankId, companyNumber, accountNumber), balancesSum);
        }

        return crossTabMap;
    }

    private void processMorningDepositCrossTabInfo(Map<String, Object> crossTabMap, List<Map> bankAccountInfoMapList, Date currentDate) {
        processDepositCrossTabInfo(crossTabMap, bankAccountInfoMapList, currentDate, 0, 12, true);
    }

    private void processAfternoonDepositCrossTabInfo(Map<String, Object> crossTabMap, List<Map> bankAccountInfoMapList, Date currentDate) {
        processDepositCrossTabInfo(crossTabMap, bankAccountInfoMapList, currentDate, 13, 24, false);
    }

    /**
     * Process deposits data info and set in Map
     *
     * @param crossTabMap
     * @param bankAccountInfoMapList
     * @param currentDate
     * @param initHour
     * @param endHour
     * @param isMorningDeposit
     */
    private void processDepositCrossTabInfo(Map<String, Object> crossTabMap, List<Map> bankAccountInfoMapList, Date currentDate, Integer initHour, Integer endHour, boolean isMorningDeposit) {

        List<ExecutorUnit> executorUnitList = findAllExecutorUnit();
        for (ExecutorUnit executorUnit : executorUnitList) {
            for (Map bankAccountInfoMap : bankAccountInfoMapList) {
                String financesBankId = (String) bankAccountInfoMap.get(FINANCESBANKID_KEY);
                String companyNumber = (String) bankAccountInfoMap.get(COMPANYNUMBER_KEY);
                String accountNumber = (String) bankAccountInfoMap.get(ACCOUNTNUMBER_KEY);

                BigDecimal depositSum = calculateExecutorUnitCurrentDayDeposit(executorUnit.getId(), accountNumber, currentDate, initHour, endHour);
                String depositGroupKey;
                if (isMorningDeposit) {
                    depositGroupKey = CashAvailableReportUtil.composeMorningDepositsKey(executorUnit.getId().toString(), financesBankId, companyNumber, accountNumber);
                } else {
                    depositGroupKey = CashAvailableReportUtil.composeAfternoonDepositsKey(executorUnit.getId().toString(), financesBankId, companyNumber, accountNumber);
                }

                crossTabMap.put(depositGroupKey, depositSum);

                //calculate deposit totals
                processDepositTotalsCrossTabInfo(crossTabMap, depositSum, financesBankId, companyNumber, accountNumber);

                //calculate collections totals by sede
                FinancesBankAccount financesBankAccount = getFinancesBankAccount(companyNumber, accountNumber);
                processCollectionBySedeTotals(crossTabMap, depositSum, executorUnit.getId().toString(), financesBankAccount);
            }
        }
    }

    /**
     * Calculate deposit totals data info and set in Map
     *
     * @param crossTabMap
     * @param depositSumValue
     * @param financesBankId
     * @param companyNumber
     * @param accountNumber
     */
    private void processDepositTotalsCrossTabInfo(Map<String, Object> crossTabMap, BigDecimal depositSumValue, String financesBankId, String companyNumber, String accountNumber) {

        String totalDepositGroupKey = CashAvailableReportUtil.composeTotalDepositsKey(financesBankId, companyNumber, accountNumber);
        String balancesGroupKey = CashAvailableReportUtil.composeBalancesBankAccountKey(financesBankId, companyNumber, accountNumber);
        String totalDepositBalancesGroupKey = CashAvailableReportUtil.composeTotalDepositMoreBalancesKey(financesBankId, companyNumber, accountNumber);

        //calculate total deposit
        sumInCrossTabInfoMap(crossTabMap, totalDepositGroupKey, depositSumValue);

        //calculate totaldeposit + balances
        BigDecimal totalDeposit = getCrossTabInfoValue(crossTabMap, totalDepositGroupKey, true);
        BigDecimal balancesValue = getCrossTabInfoValue(crossTabMap, balancesGroupKey, true);
        BigDecimal totalDepositBalances = BigDecimalUtil.sum(balancesValue, totalDeposit);

        crossTabMap.put(totalDepositBalancesGroupKey, totalDepositBalances);
    }

    /**
     * Calculate collections totals by sede an set in Map
     *
     * @param crossTabMap
     * @param depositSumValue
     * @param executorUnitId
     * @param financesBankAccount
     */
    private void processCollectionBySedeTotals(Map<String, Object> crossTabMap, BigDecimal depositSumValue, String executorUnitId, FinancesBankAccount financesBankAccount) {
        if (financesBankAccount != null) {
            if (FinancesCurrencyType.P.equals(financesBankAccount.getCurrency())) {
                sumInCrossTabInfoMap(crossTabMap, CashAvailableReportUtil.composeCollectionBySedeBsKey(executorUnitId), depositSumValue);
                sumInCrossTabInfoMap(crossTabMap, CashAvailableReportUtil.TOTALCOLLECTION_BS_KEY, depositSumValue);

            } else if (FinancesCurrencyType.D.equals(financesBankAccount.getCurrency())) {
                sumInCrossTabInfoMap(crossTabMap, CashAvailableReportUtil.composeCollectionBySedeSusKey(executorUnitId), depositSumValue);
                sumInCrossTabInfoMap(crossTabMap, CashAvailableReportUtil.TOTALCOLLECTION_SUS_KEY, depositSumValue);
            }
        }
    }

    /**
     * Process checks receivable data info and set in Map
     *
     * @param crossTabMap
     * @param bankAccountInfoMapList
     * @param currentDate
     */
    private void processChecksReceivableCrossTabInfo(Map<String, Object> crossTabMap, List<Map> bankAccountInfoMapList, Date currentDate) {
        List<FinancesExecutorUnit> financesExecutorUnitList = findAllFinancesExecutorUnit();
        for (FinancesExecutorUnit financesExecutorUnit : financesExecutorUnitList) {
            for (Map bankAccountInfoMap : bankAccountInfoMapList) {
                String financesBankId = (String) bankAccountInfoMap.get(FINANCESBANKID_KEY);
                String companyNumber = (String) bankAccountInfoMap.get(COMPANYNUMBER_KEY);
                String accountNumber = (String) bankAccountInfoMap.get(ACCOUNTNUMBER_KEY);

                BigDecimal checkReceivableSum = calculateFinancesExecutorUnitCheckReceivable(financesExecutorUnit.getExecutorUnitCode(), accountNumber, currentDate);

                crossTabMap.put(CashAvailableReportUtil.composeChecksReceivableKey(financesExecutorUnit.getExecutorUnitCode(), financesBankId, companyNumber, accountNumber), checkReceivableSum);

                //calculate totals
                processChecksReceivableTotalCrossTabInfo(crossTabMap, checkReceivableSum, financesBankId, companyNumber, accountNumber);
            }
        }
    }

    /**
     * Calculate checks receivable totals and set in Map
     *
     * @param crossTabMap
     * @param checkReceivableSumValue
     * @param financesBankId
     * @param companyNumber
     * @param accountNumber
     */
    private void processChecksReceivableTotalCrossTabInfo(Map<String, Object> crossTabMap, BigDecimal checkReceivableSumValue, String financesBankId, String companyNumber, String accountNumber) {
        if (checkReceivableSumValue != null) {
            String totalCheckReceivableGroupKey = CashAvailableReportUtil.composeTotalChecksReceivableKey(financesBankId, companyNumber, accountNumber);

            //calculate total
            BigDecimal totalCheckReceivable = (BigDecimal) crossTabMap.get(totalCheckReceivableGroupKey);
            if (totalCheckReceivable != null) {
                totalCheckReceivable = BigDecimalUtil.sum(totalCheckReceivable, checkReceivableSumValue);
            } else {
                totalCheckReceivable = checkReceivableSumValue;
            }
            crossTabMap.put(totalCheckReceivableGroupKey, totalCheckReceivable);
        }
    }

    /**
     * Calculate cash available totals and set in Map
     *
     * @param crossTabMap
     * @param bankAccountInfoMapList
     */
    private void processCashAvailableAndBalancesTotal(Map<String, Object> crossTabMap, List<Map> bankAccountInfoMapList) {

        for (Map bankAccountInfoMap : bankAccountInfoMapList) {
            String financesBankId = (String) bankAccountInfoMap.get(FINANCESBANKID_KEY);
            String companyNumber = (String) bankAccountInfoMap.get(COMPANYNUMBER_KEY);
            String accountNumber = (String) bankAccountInfoMap.get(ACCOUNTNUMBER_KEY);

            String totalDepositBalancesGroupKey = CashAvailableReportUtil.composeTotalDepositMoreBalancesKey(financesBankId, companyNumber, accountNumber);
            String totalCheckReceivableGroupKey = CashAvailableReportUtil.composeTotalChecksReceivableKey(financesBankId, companyNumber, accountNumber);

            BigDecimal totalDepositBalances = getCrossTabInfoValue(crossTabMap, totalDepositBalancesGroupKey, true);
            BigDecimal totalCheckReceivable = getCrossTabInfoValue(crossTabMap, totalCheckReceivableGroupKey, true);

            //calculate cash available total
            BigDecimal totalCashAvailable = BigDecimalUtil.subtract(totalDepositBalances, totalCheckReceivable);

            FinancesBankAccount financesBankAccount = getFinancesBankAccount(companyNumber, accountNumber);
            if (financesBankAccount != null) {
                if (FinancesCurrencyType.P.equals(financesBankAccount.getCurrency())) {
                    sumInCrossTabInfoMap(crossTabMap, CashAvailableReportUtil.PARTIALTOTAL_BS_BALANCES_KEY, totalCashAvailable);
                } else if (FinancesCurrencyType.D.equals(financesBankAccount.getCurrency())) {
                    sumInCrossTabInfoMap(crossTabMap, CashAvailableReportUtil.PARTIALTOTAL_SUS_BALANCES_KEY, totalCashAvailable);
                }
            }
            crossTabMap.put(CashAvailableReportUtil.composeTotalCashAvailableKey(financesBankId, companyNumber, accountNumber), totalCashAvailable);
        }

        //calculate total balances
        BigDecimal susToBsExchange = getSusToBsExchangeRate();
        BigDecimal partialTotalBsBalances = getCrossTabInfoValue(crossTabMap, CashAvailableReportUtil.PARTIALTOTAL_BS_BALANCES_KEY, true);
        BigDecimal partialTotalSusBalances = getCrossTabInfoValue(crossTabMap, CashAvailableReportUtil.PARTIALTOTAL_SUS_BALANCES_KEY, true);

        BigDecimal partialTotalBsSus = BigDecimalUtil.divide(partialTotalBsBalances, susToBsExchange);
        BigDecimal totalBalancesSus = BigDecimalUtil.sum(partialTotalBsSus, partialTotalSusBalances);

        crossTabMap.put(CashAvailableReportUtil.EXCHANGE_RATE_KEY, susToBsExchange);
        crossTabMap.put(CashAvailableReportUtil.PARTIALTOTAL_BSSUS_BALANCES_KEY, partialTotalBsSus);
        crossTabMap.put(CashAvailableReportUtil.TOTAL_SUS_BALANCES_KEY, totalBalancesSus);
    }

    /**
     * Get crosstab data info value
     *
     * @param crossTabMap
     * @param valueIdentifierKey
     * @param zeroIfNull
     * @return BigDecimal
     */
    private BigDecimal getCrossTabInfoValue(Map<String, Object> crossTabMap, String valueIdentifierKey, boolean zeroIfNull) {
        BigDecimal value = null;
        if (crossTabMap.containsKey(valueIdentifierKey)) {
            value = (BigDecimal) crossTabMap.get(valueIdentifierKey);
        }
        if (zeroIfNull && value == null) {
            value = BigDecimal.ZERO;
        }
        return value;
    }

    /**
     * Sum in crosstab Map the value of key with new value
     *
     * @param crossTabMap
     * @param keyValue
     * @param value
     */
    private void sumInCrossTabInfoMap(Map<String, Object> crossTabMap, String keyValue, BigDecimal value) {

        if (value != null) {
            BigDecimal total = (BigDecimal) crossTabMap.get(keyValue);
            if (total != null) {
                total = BigDecimalUtil.sum(total, value);
            } else {
                total = value;
            }
            crossTabMap.put(keyValue, total);
        }
    }

    /**
     * get the echange rate of $us to Bs
     *
     * @return BigDecimal
     */
    private BigDecimal getSusToBsExchangeRate() {
        BigDecimal susToBsExchange = BigDecimal.ONE;
        try {
            susToBsExchange = financesExchangeRateService.findLastExchangeRateByCurrency(FinancesCurrencyType.D.name());
        } catch (FinancesCurrencyNotFoundException e) {
            log.debug("$us currency not found... " + e.getMessage());
        } catch (FinancesExchangeRateNotFoundException e) {
            log.debug("$us exchange not found... " + e.getMessage());
        }
        return susToBsExchange;
    }

    /**
     * find banck account info to iterate in crosstab
     *
     * @return List
     */
    public List<Map> findBankAccountCrossTabSelect() {
        log.debug("Executing findBankAccountCrossTabSelect service.........................");

        List<Map> bankAccountInfoMapList = new ArrayList<Map>();

        try {
            List<Object[]> resultList = (List<Object[]>) em.createNamedQuery("FinancesBank.bankAccountCrossTabSelect").getResultList();
            for (Object[] rowResult : resultList) {
                String financesBankId = (String) rowResult[0];
                String companyNumber = (String) rowResult[1];
                String accountNumber = (String) rowResult[2];

                Map<String, Object> rowMap = new HashMap<String, Object>();
                rowMap.put(FINANCESBANKID_KEY, financesBankId);
                rowMap.put(COMPANYNUMBER_KEY, companyNumber);
                rowMap.put(ACCOUNTNUMBER_KEY, accountNumber);

                bankAccountInfoMapList.add(rowMap);
            }
        } catch (Exception e) {
            log.error("Error in find bank account corsstab info", e);
        }
        return bankAccountInfoMapList;
    }

    /**
     * Calculate balances at to current date, sum AccountingMovementDetail amount
     *
     * @param financesBankId
     * @param companyNumber
     * @param accountNumber
     * @param currentDate
     * @return BigDecimal
     */
    public BigDecimal sumAccountingMovementDetailAmount(String financesBankId, String companyNumber, String accountNumber, Date currentDate) {
        log.debug("Executing sumAccountingMovementDetailAmount service.........................");
        BigDecimal sumValue = null;
        try {
            sumValue = (BigDecimal) em.createNamedQuery("FinancesBank.sumAccountingMovementDetailAmount").
                    setParameter("financesBankId", financesBankId).
                    setParameter("accountNumber", accountNumber).
                    setParameter("companyNumber", companyNumber).
                    setParameter("currentDate", currentDate).
                    getSingleResult();
        } catch (Exception e) {
            log.error("Error in sum accounting Movement Detail Amount...", e);
        }
        return sumValue;
    }


    public BigDecimal calculateExecutorUnitCurrentDayDeposit(Integer executorUnitId, String accountNumber, Date currentDate, Integer initHour, Integer endHour) {
        log.debug("Executing calculateExecutorUnitCurrentDayDeposit service........................." + executorUnitId + ", " + accountNumber + ", " + currentDate + ", " + initHour + ", " + endHour);
        BigDecimal sumValue = null;
        ExecutorUnitDepositSqlQuery executorUnitDepositSqlQuery = new ExecutorUnitDepositSqlQuery();
        try {
            sumValue = (BigDecimal) em.createNativeQuery(executorUnitDepositSqlQuery.getSql()).
                    setParameter("unidad_ejecutora", executorUnitId).
                    setParameter("cta_bco", accountNumber).
                    setParameter("fecha", currentDate).
                    setParameter("hora_inicio", initHour).
                    setParameter("hora_fin", endHour).
                    getSingleResult();
        } catch (Exception e) {
            log.error("Error calculate executor unit current day deposit...", e);
        }
        return sumValue;
    }

    public BigDecimal calculateFinancesExecutorUnitCheckReceivable(String executorUnitCode, String accountNumber, Date currentDate) {
        log.debug("Executing calculateFinancesExecutorUnitCheckReceivable service........................." + executorUnitCode + ", " + accountNumber + ", " + currentDate);
        BigDecimal sumValue = null;
        FinancesExecutorUnitCheckReceivableSqlQuery checkReceivableSqlQuery = new FinancesExecutorUnitCheckReceivableSqlQuery();
        try {
            sumValue = (BigDecimal) em.createNativeQuery(checkReceivableSqlQuery.getSql()).
                    setParameter("cod_unidad_ejecutora", executorUnitCode).
                    setParameter("cta_bco", accountNumber).
                    setParameter("fecha", currentDate).
                    getSingleResult();
        } catch (Exception e) {
            log.error("Error calculate executor unit check receivable...", e);
        }
        return sumValue;
    }

    private List<ExecutorUnit> findAllExecutorUnit() {
        return QueryUtils.selectAll(em, ExecutorUnit.class).getResultList();
    }

    private List<FinancesExecutorUnit> findAllFinancesExecutorUnit() {
        return QueryUtils.selectAll(em, FinancesExecutorUnit.class).getResultList();
    }

    private FinancesBankAccount getFinancesBankAccount(String companyNumber, String accountNumber) {
        FinancesBankAccount financesBankAccount = null;
        FinancesBankAccountPk financesBankAccountPk = new FinancesBankAccountPk(companyNumber, accountNumber);
        try {
            financesBankAccount = em.find(FinancesBankAccount.class, financesBankAccountPk);
        } catch (Exception e) {
            log.error("Not found FinancesBankAccount... ", e);
        }

        return financesBankAccount;
    }

    /**
     * Query to calculate executor unit deposits
     * Filters: unidad_ejecutora, cta_bco, fecha, hora_inicio, hora_fin
     */
    public class ExecutorUnitDepositSqlQuery implements SqlQuery {
        public String getSql() {

            return "select sum(importe) monto\n" +
                    "from " + Constants.CASHBOX_SCHEMA + ".cheques_depositos cd, " + Constants.CASHBOX_SCHEMA + ".estructuras es\n" +
                    "where cd.est_cod = es.codigo\n" +
                    "and cd.tipo = 'D'\n" +
                    "and cd.estado = 'V'\n" +
                    "and es.UNIDAD_ACAD_ADM = :unidad_ejecutora \n" +
                    "and cd.cta_bco_wise = :cta_bco\n" +
                    "and trunc(cd.fecha) = trunc(:fecha)\n" +
                    "and to_number(to_char(cd.fecha,'HH24')) between :hora_inicio and :hora_fin \n";
        }
    }

    /**
     * Query to calculate executor unit checks receivables
     * Filters: cta_bco, fecha, cod_unidad_ejecutora
     */
    public class FinancesExecutorUnitCheckReceivableSqlQuery implements SqlQuery {
        public String getSql() {

            return "SELECT sum(doc.MONTO) AS amount \n" +
                    "FROM " + Constants.FINANCES_SCHEMA + ".CK_TIPODOCS td\n" +
                    "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CK_DOCUS doc ON doc.NO_CIA = td.NO_CIA AND doc.TIPO_DOC = td.TIPO_DOC\n" +
                    "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".CK_MOVS mov ON mov.NO_CIA = doc.NO_CIA AND mov.NO_TRANS = doc.NO_TRANS\n" +
                    "LEFT JOIN " + Constants.FINANCES_SCHEMA + ".USUARIOS us ON us.NO_USR = mov.NO_USR\n" +
                    "WHERE td.DOSIFICADOR = 'S'\n" +
                    "AND doc.PROCEDENCIA = 'E'\n" +
                    "AND doc.NO_CONCI IS NULL\n" +
                    "AND doc.ESTADO = 'APR'\n" +
                    "AND mov.ESTADO = 'APR'\n" +
                    "AND doc.CTA_BCO = :cta_bco \n" +
                    "AND trunc(mov.FECHA) = trunc(:fecha)\n" +
                    "AND us.COD_UNI = :cod_unidad_ejecutora \n";
        }
    }

}
