package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.ExchangeKind;
import com.encens.khipus.model.finances.FinancesCurrencyType;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;

/**
 * FinancesExchangeRate Service
 *
 * @author
 * @version 2.3
 */
@Local
public interface FinancesExchangeRateService extends GenericService {

    BigDecimal findExchangeRateByDateByCurrency(Date date, String currencyCode)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException;

    BigDecimal findLastExchangeRateByCurrency(String currencyCode)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException;

    Date findLastDateByFinancesCurrency(ExchangeKind exchangeKind);

    Date findLastFinancesExchangeRateDate4UfvSus(String ufvCurrencyCode, String susCurrencyCode)
            throws FinancesCurrencyNotFoundException, FinancesExchangeRateNotFoundException;

    BigDecimal getExchangeRateByCurrencyType(FinancesCurrencyType currencyType, BigDecimal defaultExchangeRate)
            throws FinancesCurrencyNotFoundException,
            FinancesExchangeRateNotFoundException;
}
