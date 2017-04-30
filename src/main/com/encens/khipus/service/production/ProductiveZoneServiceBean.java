package com.encens.khipus.service.production;

import com.encens.khipus.model.production.ProductiveZone;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@Name("productiveZoneService")
@AutoCreate
public class ProductiveZoneServiceBean implements ProductiveZoneService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public List<ProductiveZone> findAll() {
        List<ProductiveZone> aux = em.createNamedQuery("ProductiveZone.findAll").getResultList();
        return aux;
    }

    @Override
    public List<ProductiveZone> findAllThatDoNotHaveCollectionForm(Date startDate,Date endDate) {
        return em.createNamedQuery("ProductiveZone.findAllThatDoNotHavePayRollOnDate")
                 .setParameter("startDate", startDate)
                 .setParameter("endDate", endDate)
                 .getResultList();
    }

    @Override
    public ProductiveZone find(long id) {
        return em.find(ProductiveZone.class, id);
    }
}
