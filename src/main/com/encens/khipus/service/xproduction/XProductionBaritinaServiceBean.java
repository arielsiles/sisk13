package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionBaritina;
import com.encens.khipus.model.xproduction.XProductionBaritinaZona;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

@Stateless
@Name("xproductionBaritinaService")
@AutoCreate
public class XProductionBaritinaServiceBean implements XProductionBaritinaService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public XProductionBaritina findByProduction(XProduction production) {
        if (production == null || production.getId() == null) return null;
        try {
            return (XProductionBaritina) em.createQuery(
                    "select b from XProductionBaritina b where b.production.id = :pid")
                    .setParameter("pid", production.getId())
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public void save(XProductionBaritina baritina) {
        if (baritina == null) return;
        if (baritina.getId() == null) {
            em.persist(baritina);
        } else {
            em.merge(baritina);
        }
        em.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XProductionBaritinaZona> findZonasByProduction(XProduction production) {
        if (production == null || production.getId() == null) return Collections.emptyList();
        return em.createQuery(
                "select z from XProductionBaritinaZona z " +
                "where z.production.id = :pid order by z.id asc")
                .setParameter("pid", production.getId())
                .getResultList();
    }

    @Override
    public void saveZona(XProductionBaritinaZona zona) {
        if (zona == null) return;
        if (zona.getId() == null) {
            em.persist(zona);
        } else {
            em.merge(zona);
        }
        em.flush();
    }

    @Override
    public void removeZona(XProductionBaritinaZona zona) {
        if (zona == null || zona.getId() == null) return;
        XProductionBaritinaZona managed = em.find(XProductionBaritinaZona.class, zona.getId());
        if (managed != null) {
            em.remove(managed);
            em.flush();
        }
    }

    @Override
    public void deleteByProduction(XProduction production) {
        if (production == null || production.getId() == null) return;
        em.createQuery("delete from XProductionBaritinaZona z where z.production.id = :pid")
                .setParameter("pid", production.getId())
                .executeUpdate();
        XProductionBaritina header = findByProduction(production);
        if (header != null) {
            em.remove(em.contains(header) ? header : em.merge(header));
        }
        em.flush();
    }
}
