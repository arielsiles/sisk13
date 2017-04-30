package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.contacts.MaritalStatus;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;


/**
 * @author
 * @version 3.4
 */

@Stateless
@Name("maritalStatusService")
@AutoCreate
public class MaritalStatusServiceBean extends GenericServiceBean implements MaritalStatusService {

    public MaritalStatus findByCode(String code) {
        if (ValidatorUtil.isBlankOrNull(code)) {
            return null;
        }
        try {
            return (MaritalStatus) getEntityManager().createNamedQuery("MaritalStatus.findByCode")
                    .setParameter("code", code).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}