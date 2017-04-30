package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.CycleType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Sector;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * User: Ariel
 * Date: 24-06-2010
 * Time: 12:37:02 PM
 */

@Stateless
@Name("cycleService")
@AutoCreate
public class CycleServiceBean extends GenericServiceBean implements CycleService {

    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public Cycle findActiveCycle(Gestion gestion, CycleType cycleType) {
        Cycle cycle = null;
        try {
            cycle = (Cycle) em.createNamedQuery("Cycle.findActiveByGestionAndPeriod").
                    setParameter("gestion", gestion).
                    setParameter("cycleType", cycleType).
                    setParameter("active", Boolean.TRUE).
                    getSingleResult();

        } catch (Exception e) {
            log.debug("Not found Cycle...");
        }
        return cycle;
    }

    public boolean isThereActiveCycleForSector(Sector sector) {
        try {
            Query query = listEm.createNamedQuery("Cycle.countActiveCycleBySector");
            query.setParameter("sector", sector);
            query.setParameter("active", true);
            long count = (Long) query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            log.error(e, "An unexpected error has happened...");
            return true;
        }
    }
    /* When a new cycle is created this can be active, so the old active cycle for the corresponding sector must be unactivated*/

    public void unActiveCycleForSector(Sector sector) {
        try {
            Query query = listEm.createNamedQuery("Cycle.findActiveCycleBySector");
            query.setParameter("sector", sector);
            query.setParameter("active", true);
            Cycle cycle = (Cycle) query.getSingleResult();
            if (cycle != null) {
                cycle.setActive(false);
                listEm.merge(cycle);
                listEm.flush();
            }
        } catch (Exception e) {
            log.error(e, "An unexpected error has happened...");
        }
    }

    public boolean isActiveInDataBase(Cycle cycle) {
        try {
            Query query = listEm.createNamedQuery("Cycle.findCycle");
            query.setParameter("id", cycle.getId());
            Cycle cycleDB = (Cycle) query.getSingleResult();
            return cycleDB != null && cycleDB.getActive();
        } catch (Exception e) {
            log.error(e, "An unexpected error has happened...");
            return true;
        }
    }
}
