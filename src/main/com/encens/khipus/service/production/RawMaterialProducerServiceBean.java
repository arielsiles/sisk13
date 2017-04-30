package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/7/13
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("rawMaterialProducerService")
@Stateless
@AutoCreate
public class RawMaterialProducerServiceBean extends ExtendedGenericServiceBean implements RawMaterialProducerService {

    /*@Override
    public List<RawMaterialProducer> findAllThatDontHaveCollectedRawMaterial(ProductiveZone productiveZone, Date date) {
        List<RawMaterialProducer> result = getEntityManager().createNamedQuery("RawMaterialProducer.findAllThatDontHaveCollectedRawMaterialByDateAndProductiveZone")
                                                      .setParameter("productiveZone", productiveZone)
                                                      .setParameter("date", date)
                                                      .getResultList();
        return result;
    }*/

    @Override
    public List<RawMaterialProducer> findAll(ProductiveZone productiveZone) {
        List<RawMaterialProducer> result = getEntityManager().createNamedQuery("RawMaterialProducer.findAllByProductiveZone")
                                                     .setParameter("productiveZone", productiveZone)
                                                     .getResultList();
        return result;
    }

    @Override
    protected Object preUpdate(Object entity) {
        RawMaterialProducer rawMaterialProducer = (RawMaterialProducer)entity;
        if (rawMaterialProducer.getResponsible() == true) {
            tryAssignAsResponsible(rawMaterialProducer);
        }

        return entity;
    }

    private void tryAssignAsResponsible(RawMaterialProducer rawMaterialProducer) {
        List<RawMaterialProducer> responsibles = getEntityManager().createNamedQuery("RawMaterialProducer.findReponsibleExceptThisByProductiveZone")
                                                            .setParameter("productiveZone", rawMaterialProducer.getProductiveZone())
                                                            .setParameter("rawMaterialProducer", rawMaterialProducer)
                                                            .getResultList();

        for(RawMaterialProducer resp : responsibles) {
            resp.setResponsible(false);
        }
    }

    @Override
    protected Object preCreate(Object entity) {
        return entity;
    }
}
