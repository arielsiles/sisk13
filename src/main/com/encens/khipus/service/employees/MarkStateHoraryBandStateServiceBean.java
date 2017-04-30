package com.encens.khipus.service.employees;


import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.MarkState;
import com.encens.khipus.model.employees.MarkStateHoraryBandState;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("markStateHoraryBandStateService")
@AutoCreate
public class MarkStateHoraryBandStateServiceBean extends GenericServiceBean implements MarkStateHoraryBandStateService {

    public Long countByMarkStateAndNotMarkStateHoraryBandState(MarkState markState, MarkStateHoraryBandState markStateHoraryBandState) {
        try {
            return (Long) getEntityManager().createNamedQuery("MarkStateHoraryBandState.countByMarkStateAndNotMarkStateHoraryBandState")
                    .setParameter("markState", markState)
                    .setParameter("markStateHoraryBandState", markStateHoraryBandState)
                    .getSingleResult();
        } catch (NoResultException e) {
            return (long) 0;
        }
    }

    public MarkStateHoraryBandState findByMarkStateAndHoraryBandState(MarkState markState, Long horaryBandStateId, EntityManager entityManager) {
        try {
            return (MarkStateHoraryBandState) entityManager.createNamedQuery("MarkStateHoraryBandState.findByMarkStateAndHoraryBandState")
                    .setParameter("markState", markState)
                    .setParameter("horaryBandStateId", horaryBandStateId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
