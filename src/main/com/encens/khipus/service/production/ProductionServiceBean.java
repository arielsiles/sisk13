package com.encens.khipus.service.production;

import com.encens.khipus.model.production.Production;
import com.encens.khipus.model.production.Supply;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id: ProductServiceBean.java 2008-9-11 13:50:57 $
 */
@Stateless
@Name("productionService")
@AutoCreate
public class ProductionServiceBean implements ProductionService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    public void createProduction(Production production, List<Supply> mainSupplyList){

        em.persist(production);
        em.flush();
        for (Supply supply : mainSupplyList){
            supply.setProduction(production);
            em.persist(supply);
            em.flush();
        }
    }

    public List<Supply> getSupplyList(Production production) {

        List<Supply> supplyList = em.createQuery("select supply from Supply supply " +
                " where supply.production = :production ")
                .setParameter("production", production)
                .getResultList();

        return supplyList;
    }
}
