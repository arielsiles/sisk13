package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.*;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AccessRightService implementation class
 *
 * @author:
 */

@Stateless
@Name("accessRightService")
@AutoCreate
public class AccessRightServiceBean implements AccessRightService {

    @In("#{entityManager}")
    private EntityManager em;

    @SuppressWarnings({"unchecked"})
    public List<SystemFunction> getFunctions() {
        try {
            return em.createNamedQuery("SystemFunction.findAll").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AccessRight getAccessRight(SystemFunction function, Role role) {
        try {
            return (AccessRight) em.createNamedQuery("AccessRight.findByFunctionAndRole")
                    .setParameter("function", function)
                    .setParameter("role", role).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Map<SystemFunction, AccessRight> getAccessRightMapByFunction(Role role) {
        Map<SystemFunction, AccessRight> accessRightMap = new HashMap<SystemFunction, AccessRight>();
        try {
            List<AccessRight> accessRightList = em.createNamedQuery("AccessRight.findAllByRole")
                    .setParameter("role", role).getResultList();

            if (!ValidatorUtil.isEmptyOrNull(accessRightList)) {
                for (AccessRight accessRight : accessRightList) {
                    accessRightMap.put(accessRight.getFunction(), accessRight);
                }
            }

        } catch (NoResultException e) {
            return null;
        }

        return accessRightMap;
    }

    public List<SystemFunction> getFunctions(SystemModule module) {
        try {
            return em.createNamedQuery("SystemFunction.findByModule")
                    .setParameter("module", module).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<SystemFunction> getAllFunctions(Company company) {
        try {
            return em.createNamedQuery("SystemFunction.findAllByModule")
                    .setParameter("company", company)
                    .setParameter("active", Boolean.TRUE)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(Object entity) {
        if (!em.contains(entity)) {
            em.merge(entity);
        }
    }

}
