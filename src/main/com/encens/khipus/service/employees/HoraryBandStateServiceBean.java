package com.encens.khipus.service.employees;


import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.HoraryBandState;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.Date;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("horaryBandStateService")
@AutoCreate
public class HoraryBandStateServiceBean extends GenericServiceBean implements HoraryBandStateService {

    public HoraryBandState findByDateAndHoraryBand(Date date, Long horaryBandId) {
        try {
            return (HoraryBandState) getEntityManager().createNamedQuery("HoraryBandState.findByDateAndHoraryBand")
                    .setParameter("date", date)
                    .setParameter("horaryBandId", horaryBandId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
