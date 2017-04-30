package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.finances.KindOfSalary;
import com.encens.khipus.model.finances.Salary;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : SalaryServiceBean, 02-12-2009 07:10:49 PM
 */
@Stateless
@Name("salaryService")
@AutoCreate
public class SalaryServiceBean implements SalaryService {
    @In("#{entityManager}")
    private EntityManager em;

    public List<Salary> getSalariesByTypeCurrencyAndAmount(KindOfSalary kindOfSalary, Currency currency, BigDecimal amount) {

        if (kindOfSalary != null && currency != null && amount != null) {
            try {
                return em.createNamedQuery("Salary.findByTypeCurrencyAndAmount").setParameter("kindOfSalary", kindOfSalary)
                        .setParameter("currency", currency).setParameter("amount", amount).getResultList();
            } catch (Exception e) {
            }
        }

        return new ArrayList<Salary>(0);
    }
}
