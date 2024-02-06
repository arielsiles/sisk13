package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.xproduction.ProductiveZone;
import com.encens.khipus.model.xproduction.RawMaterialProducer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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

    @In("#{entityManager}")
    private EntityManager em;

    @Override
    public List<RawMaterialProducer> findAll(ProductiveZone productiveZone) {
        List<RawMaterialProducer> result = getEntityManager().createNamedQuery("RawMaterialProducer.findAllByProductiveZone")
                                                     .setParameter("productiveZone", productiveZone)
                                                     .getResultList();
        return result;
    }

    @Override
    public RawMaterialProducer findProducerByIdNumber(String idNumber) {
        try {
            return (RawMaterialProducer) em.createNamedQuery("RawMaterialProducer.findByIdNumber")
                    .setParameter("idNumber", idNumber).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
