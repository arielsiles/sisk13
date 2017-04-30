package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.MarkState;
import com.encens.khipus.model.employees.RHMark;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

/**
 * @author
 * @version 0.3
 */
@Stateless
@Name("markStateService")
@AutoCreate
public class MarkStateStateServiceBean extends GenericServiceBean implements MarkStateService {

    public MarkState findByMark(RHMark mark) {
        try {
            return (MarkState) getEntityManager().createNamedQuery("MarkState.findByMark")
                    .setParameter("mark", mark)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
