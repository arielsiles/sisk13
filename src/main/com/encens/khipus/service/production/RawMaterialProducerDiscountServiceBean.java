package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.RawMaterialProducerDiscount;
import com.encens.khipus.model.production.SalaryMovementProducer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.Date;

@Stateless
@Name("rawMaterialProducerDiscountService")
@AutoCreate
public class RawMaterialProducerDiscountServiceBean extends ExtendedGenericServiceBean implements RawMaterialProducerDiscountService {

    @Override
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer) throws EntryNotFoundException {
        try {
            RawMaterialProducerDiscount discount = (RawMaterialProducerDiscount) getEntityManager().createNamedQuery("RawMaterialProducerDiscount.findWithGreatestCodeByRawMaterialProducer")
                                                                                     .setParameter("rawMaterialProducer", rawMaterialProducer)
                                                                                     .getSingleResult();
            if (discount.getRawMaterialPayRecord() == null) {
                return discount;
            } else {
                return createNewRawMaterialProducerDiscount(rawMaterialProducer, discount.getCode() + 1);
            }
        } catch (NoResultException ex) {
            return createNewRawMaterialProducerDiscount(rawMaterialProducer, 1);
        }
    }

    @Override
    public SalaryMovementProducer prepareDiscountSalary(RawMaterialProducer rawMaterialProducer , Date startDate, Date endDate) throws EntryNotFoundException{
            SalaryMovementProducer discount = (SalaryMovementProducer) getEntityManager().createNamedQuery("SalaryMovementProducer.getDiscountProducer")
                    .setParameter("rawMaterialProducer", rawMaterialProducer)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getSingleResult();
            return discount;
    }

    private RawMaterialProducerDiscount createNewRawMaterialProducerDiscount(RawMaterialProducer rawMaterialProducer, long code) throws EntryNotFoundException {
        rawMaterialProducer = findById(RawMaterialProducer.class, rawMaterialProducer.getId());
        RawMaterialProducerDiscount discount = new RawMaterialProducerDiscount();
        discount.setCode(code);
        discount.setRawMaterialProducer(rawMaterialProducer);
        return discount;
    }
}
