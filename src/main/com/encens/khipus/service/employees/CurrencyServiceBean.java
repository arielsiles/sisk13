package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:07:35 PM
 */
@Stateless
@Name("currencyService")
@AutoCreate
public class CurrencyServiceBean implements CurrencyService {

    @In("#{entityManager}")
    private EntityManager em;

    public Currency getCurrencyById(Long id) {
        Currency result = null;
        try {
            result = (Currency) em.createNamedQuery("Currency.findCurrency").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }

    public Currency findBaseCurrency() {
        return em.find(Currency.class, Constants.currencyIdBs);
    }
}