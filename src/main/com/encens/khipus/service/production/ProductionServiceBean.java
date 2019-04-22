package com.encens.khipus.service.production;

import com.encens.khipus.model.production.Production;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

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


    public void createProduction(Production production){

        em.persist(production);
        em.flush();

    }


}
