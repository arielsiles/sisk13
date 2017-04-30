package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Currency;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:05:12 PM
 */
@Local
public interface CurrencyService {

    Currency getCurrencyById(Long id);

    Currency findBaseCurrency();
}