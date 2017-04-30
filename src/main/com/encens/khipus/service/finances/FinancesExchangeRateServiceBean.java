package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.3
 */
@Name("financesExchangeRateService")
@Stateless
@AutoCreate
public class FinancesExchangeRateServiceBean extends GenericServiceBean implements FinancesExchangeRateService {

    public Date findLastFinancesExchangeRateDate4UfvSus(String ufvCurrencyCode, String susCurrencyCode)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {
        /*retrieves the corresponding FinancesCurrency according to the string code supplied*/
        FinancesCurrencyPk susFinancesCurrencyPk = new FinancesCurrencyPk();
        susFinancesCurrencyPk.setCurrencyCode(susCurrencyCode);
        susFinancesCurrencyPk.setCompanyNumber(Constants.defaultCompanyNumber);
        FinancesCurrency susFinancesCurrency = getEntityManager().find(FinancesCurrency.class, susFinancesCurrencyPk);

        FinancesCurrencyPk ufvFinancesCurrencyPk = new FinancesCurrencyPk();
        ufvFinancesCurrencyPk.setCurrencyCode(ufvCurrencyCode);
        ufvFinancesCurrencyPk.setCompanyNumber(Constants.defaultCompanyNumber);
        FinancesCurrency ufvFinancesCurrency = getEntityManager().find(FinancesCurrency.class, ufvFinancesCurrencyPk);
        try {
            Query query = getEntityManager().createNamedQuery("FinancesExchangeRate.findLastFinancesExchangeRateDate4SusBs");
            query.setParameter("susExchangeKind", susFinancesCurrency.getExchangeKind());
            query.setParameter("ufvExchangeKind", ufvFinancesCurrency.getExchangeKind());
            return (Date) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BigDecimal findLastExchangeRateByCurrency(String currencyCode)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {
        /*retrieves the corresponding FinancesCurrency according to the string code supplied*/
        FinancesCurrencyPk financesCurrencyPk = new FinancesCurrencyPk();
        financesCurrencyPk.setCurrencyCode(currencyCode);
        financesCurrencyPk.setCompanyNumber(Constants.defaultCompanyNumber);

        FinancesCurrency financesCurrency = getEntityManager().find(FinancesCurrency.class, financesCurrencyPk);
        return findExchangeRateByDateByCurrency(
                findLastDateByFinancesCurrency(financesCurrency.getExchangeKind()), currencyCode);
    }

    public Date findLastDateByFinancesCurrency(ExchangeKind exchangeKind) {
        try {
            Query query = getEntityManager().createNamedQuery("FinancesExchangeRate.findLastDateByFinancesCurrency");
            query.setParameter("exchangeKind", exchangeKind);
            return (Date) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public BigDecimal findExchangeRateByDateByCurrency(Date date, String currencyCode)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException {
        /*retrieves the corresponding FinancesCurrency according to the string code supplied*/
        FinancesCurrencyPk financesCurrencyPk = new FinancesCurrencyPk();
        financesCurrencyPk.setCurrencyCode(currencyCode);
        financesCurrencyPk.setCompanyNumber(Constants.defaultCompanyNumber);

        FinancesCurrency financesCurrency = getEntityManager().find(FinancesCurrency.class, financesCurrencyPk);

        if (financesCurrency == null) {
            throw new FinancesCurrencyNotFoundException(
                    "Can't find Exchange Rate due to the specified currency was not found"
            );
        }
        FinancesExchangeRate financesExchangeRate = findFinancesExchangeRateByFinancesCurrencyByDate(financesCurrency.getExchangeKind(), date);

        return financesExchangeRate.getRate();
    }

    public FinancesExchangeRate findFinancesExchangeRateByFinancesCurrencyByDate(ExchangeKind exchangeKind, Date date) throws FinancesExchangeRateNotFoundException {
        try {
            Query query = getEntityManager().createNamedQuery("FinancesExchangeRate.findExchangeRateByDateByCurrency");
            query.setParameter("exchangeKind", exchangeKind);
            query.setParameter("date", date);
            return (FinancesExchangeRate) query.getSingleResult();
        } catch (NoResultException e) {
            throw new FinancesExchangeRateNotFoundException(
                    "Can't find Exchange Rate for the specified currency and Date"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BigDecimal getExchangeRateByCurrencyType(FinancesCurrencyType currencyType, BigDecimal defaultExchangeRate)
            throws FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException {
        BigDecimal currentExchangeRate = BigDecimal.ONE;
        if (!FinancesCurrencyType.P.equals(currencyType)) {
            if (BigDecimalUtil.isPositive(defaultExchangeRate) && !currentExchangeRate.equals(defaultExchangeRate)) {
                currentExchangeRate = defaultExchangeRate;
            } else {
                currentExchangeRate = findLastExchangeRateByCurrency(currencyType.name());
            }
        }

        return currentExchangeRate;
    }
}
