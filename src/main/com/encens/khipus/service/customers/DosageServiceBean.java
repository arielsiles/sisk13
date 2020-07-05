package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Dosage;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("dosageService")
@AutoCreate
public class DosageServiceBean implements DosageService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public Dosage findDosageByOffice(Long branchOfficeId) {

        System.out.println("--------------> branchOfficeId: " + branchOfficeId);

        Dosage result = (Dosage) em.createQuery(
                " select d from Dosage d " +
                   " where d.branchOffice.id =:branchOfficeId " +
                   " and d.active =:active ")
                .setParameter("branchOfficeId", branchOfficeId)
                .setParameter("active", Boolean.TRUE)
                .getSingleResult();

        System.out.println("--------------> result: "+ result);

        return result;
    }

    @Override
    public void increaseInvoiceNumber(Dosage dosage) {
        Long number = dosage.getCurrentNumber();
        number = number + 1;
        dosage.setCurrentNumber(number);
        em.merge(dosage);
        em.flush();
    }
}
