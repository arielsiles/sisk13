package com.encens.khipus.service.warehouse;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * Service to calculate values related to product item report
 *
 * @author
 * @version $Id: ${NAME}.java  11-mar-2010 19:46:33$
 */
@Stateless
@Name("productItemReportService")
@AutoCreate
public class ProductItemReportServiceBean implements ProductItemReportService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;


    public ProductItemReportServiceBean() {
    }

    /**
     * Sum unitary balance porduct item invetory
     *
     * @param productItemCode
     * @param companyNumber
     * @return BigDecimal
     */
    public BigDecimal sumProductItemUnitaryBalanceInventory(String productItemCode, String companyNumber) {
        log.debug("Executing sumProductItemUnitaryBalanceInventory service.........................");

        BigDecimal unitaryBalanceSum = null;

        if (productItemCode != null && companyNumber != null) {
            unitaryBalanceSum = (BigDecimal) em.createNamedQuery("Inventory.sumUnitaryBalancesByArticleCode").
                    setParameter("companyNumber", companyNumber).
                    setParameter("articleNumber", productItemCode).
                    getSingleResult();
        }
        return unitaryBalanceSum;
    }
}
