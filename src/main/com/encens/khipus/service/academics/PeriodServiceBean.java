package com.encens.khipus.service.academics;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Encens S.R.L.
 * Service to management Period entity
 *
 * @author
 * @version $Id: PeriodServiceBean.java  19-ago-2010 15:53:16$
 */
@Stateless
@Name("periodService")
@AutoCreate
public class PeriodServiceBean implements PeriodService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public PeriodServiceBean() {
    }


    public List<Integer> findAllPeriods() {
        log.debug("Executing findAllPeriods........");
        return em.createNamedQuery("Period.findPeriods").getResultList();
    }
}
