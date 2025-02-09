package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.xproduction.ProductionLine;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

@Stateless
@Name("productionLineService")
@AutoCreate
public class ProductionLineServiceBean extends GenericServiceBean implements ProductionLineService {

    @In(value = "#{entityManager}")
    private EntityManager em;



    @Override
    public ProductionLine getDefaultProductionLine() {
        List<ProductionLine> productionLines = em.createQuery("select o from ProductionLine o order by o.name ASC")
                                                 .setMaxResults(1)
                                                 .getResultList();

        return productionLines.isEmpty() ? null : productionLines.get(0);
    }
}
