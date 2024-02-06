package com.encens.khipus.service.xproduction;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.xproduction.ProductiveZone;
import com.encens.khipus.model.xproduction.RawMaterialProducerDiscount;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/7/13
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("salaryMovementGABService")
@Stateless
@AutoCreate
public class SalaryMavementGABServiceBean extends ExtendedGenericServiceBean implements SalaryMovementGABService {

    @Override
    public Double getAlcoholBayGAB(ProductiveZone productiveZone, Date startDate, Date endDate) throws EntryNotFoundException {

        RawMaterialProducerDiscount rawMaterialProducerDiscount = new RawMaterialProducerDiscount();
        Double totalAlcohol = 0.0;
        List<Object[]> alcohols = getEntityManager().createNamedQuery("SalaryMovementGAB.getDiscount")
                                                               .setParameter("startDate",startDate,TemporalType.DATE)
                                                               .setParameter("endDate",endDate,TemporalType.DATE)
                                                               .setParameter("productiveZone",productiveZone)
                                                               .getResultList();
        for(Object[] alcohol: alcohols)
        {
            totalAlcohol += (Double)alcohol[0];
        }

        return totalAlcohol;
    }
}
