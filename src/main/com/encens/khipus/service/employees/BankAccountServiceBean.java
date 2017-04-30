package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.BankAccount;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * BankAccountServiceBean
 *
 * @author
 * @version 1.1.10
 */
@Stateless
@Name("bankAccountService")
@AutoCreate
public class BankAccountServiceBean implements BankAccountService {

    @In("#{entityManager}")
    private EntityManager em;

    public Boolean hasDefaultAccount(Employee employee) {
        return ((Long) em.createNamedQuery("BankAccount.countByDefaulAccount")
                .setParameter("employee", employee)
                .setParameter("defaultAccount", Boolean.TRUE).getSingleResult()).longValue() > 0;
    }

    public BankAccount getDefaultAccount(Employee employee) {
        try {
            return (BankAccount) em.createNamedQuery("BankAccount.findByDefaulAccount")
                    .setParameter("employee", employee)
                    .setParameter("defaultAccount", Boolean.TRUE).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Boolean hasDefaultAccount(Employee employee, BankAccount bankAccount) {
        return ((Long) em.createNamedQuery("BankAccount.countByDefaulAccountAndBankAccount")
                .setParameter("employee", employee)
                .setParameter("bankAccount", bankAccount)
                .setParameter("defaultAccount", Boolean.TRUE).getSingleResult()).longValue() > 0;
    }
}
