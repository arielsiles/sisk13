package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.ContractMode;
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
@Name("contractModeService")
@AutoCreate
public class ContractModeServiceBean implements ContractModeService {

    @In("#{entityManager}")
    private EntityManager em;

    public ContractMode getContractModeById(Long id) {
        ContractMode result = null;
        try {
            result = (ContractMode) em.createNamedQuery("ContractMode.findContractMode").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }
}