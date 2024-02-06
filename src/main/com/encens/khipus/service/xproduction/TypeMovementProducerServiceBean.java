package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.SalaryMovementProducerTypeEnum;
import com.encens.khipus.model.production.TypeMovementProducer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * @author
 * @version 3.1
 */

@Stateless
@Name("typeMovementProducerService")
@AutoCreate
public class TypeMovementProducerServiceBean implements TypeMovementProducerService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public TypeMovementProducer findTypeMovementProducer(SalaryMovementProducerTypeEnum salaryMovementProducerTypeEnum){
        return (TypeMovementProducer) em.createQuery("select t from TypeMovementProducer t where t.salaryMovementProducerTypeEnum =:salaryMovementProducerTypeEnum")
                .setParameter("salaryMovementProducerTypeEnum", salaryMovementProducerTypeEnum)
                .getSingleResult();

    }

}
