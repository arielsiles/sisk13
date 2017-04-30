package com.encens.khipus.service.employees;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.contacts.Salutation;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * User: Ariel
 * Date: 26-08-2010
 * Time: 11:59:50 AM
 */
@Stateless
@Name("salutationService")
@AutoCreate
public class SalutationServiceBean implements SalutationService {

    @In("#{entityManager}")
    private EntityManager em;

    @In
    private CompanyConfigurationService companyConfigurationService;

    public Salutation getDefaultSalutation(String gender) {

        if (null == gender) {
            return null;
        }
        Salutation result = null;
        //todo the gender value must be fixed by enumeration structure
        try {
            result = gender.equals("M") ? companyConfigurationService.findDefaultSalutationForMan() : companyConfigurationService.findDefaultSalutationForWoman();
        } catch (CompanyConfigurationNotFoundException e) {
            return null;
        } catch (NoResultException e) {
            return null;
        }
        return result;

    }
}
