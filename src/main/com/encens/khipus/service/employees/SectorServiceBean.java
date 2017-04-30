package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Sector;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * @author
 * @version 1.1.8
 */
@Stateless
@Name("sectorService")
@AutoCreate
public class SectorServiceBean implements SectorService {
    @In("#{entityManager}")
    private EntityManager em;


    public List<Sector> getAllSector() {
        try {
            return em.createNamedQuery("Sector.findAll").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
