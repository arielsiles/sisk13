package com.encens.khipus.service.production;

import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProducerPrice;
import com.encens.khipus.model.production.RawMaterialProducer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;

@Name("producerPriceService")
@Stateless
@AutoCreate
public class ProducerPriceServiceBean implements ProducerPriceService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public BigDecimal findProducerPrice(MetaProduct metaProduct, RawMaterialProducer producer) {

        BigDecimal price = BigDecimal.ZERO;
        ProducerPrice producerPrice = null;

        try {
            producerPrice = (ProducerPrice) em.createQuery("select p from ProducerPrice p " +
                    " where p.rawMaterialProducer =:producer " +
                    " and p.metaProduct =:metaProduct ")
                    .setParameter("producer", producer)
                    .setParameter("metaProduct", metaProduct)
                    .getSingleResult();
        } catch (NoResultException e) {
            producerPrice = null;
        }

        if (producerPrice != null)
            price = producerPrice.getPrice();

        return price;
    }
}
