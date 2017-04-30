package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.ContractState;
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
@Name("contractStateService")
@AutoCreate
public class ContractStateServiceBean implements ContractStateService {

    @In("#{entityManager}")
    private EntityManager em;

    public ContractState getContractStateById(Long id) {
        ContractState result = null;
        try {
            result = (ContractState) em.createNamedQuery("ContractState.findContractState").setParameter("id", id).getSingleResult();
        } catch (Exception e) {
        }
        return result;
    }
}