package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.LogProductiveZone;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 12/11/13
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@Name("logProductiveZoneService")
@AutoCreate
public class LogProductiveZoneServiceBean implements LogProductiveZoneService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public void createLog(LogProductiveZone logProductiveZone) {
        em.persist(logProductiveZone);
        em.flush();
    }
}
