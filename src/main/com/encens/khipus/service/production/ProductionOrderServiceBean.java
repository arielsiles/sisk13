package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.xproduction.XProductionProduct;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.Date;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Created by Diego on 05/08/2014.
 */
@Name("productionOrderService")
@Stateless
@AutoCreate
public class ProductionOrderServiceBean extends GenericServiceBean implements ProductionOrderService  {
    @In(value = "#{entityManager}")
    private EntityManager em;

    @TransactionAttribute(REQUIRES_NEW)
    @Override
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        try {
            if (!getEntityManager().contains(entity)) {
                getEntityManager().merge(entity);
            }
            getEntityManager().flush();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        } catch (PersistenceException ee) { // TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            throw new EntryDuplicatedException(ee);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProductionOrder> findProductionOrdersByProductItem(String productItemCode, Date startDate, Date endDate){
        return em.createNamedQuery("ProductionOrder.findProductionOrdersByProductItem")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProductionProduct> findProductionByProductItem(String productItemCode, Date startDate, Date endDate){
        return em.createNamedQuery("ProductionProduct.findProductionByProductItem")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProductionProduct> findProductionByDate(Date startDate, Date endDate){
        return em.createNamedQuery("ProductionProduct.findProductionByDates")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProductionOrder> findProductionOrders(Date startDate, Date endDate){
        return em.createNamedQuery("ProductionOrder.findProductionOrders")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<BaseProduct> findBaseProductByDate(Date startDate, Date endDate){
        return em.createNamedQuery("BaseProduct.findBaseProductByDate")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    /** XProductionProduct **/
    public List<XProductionProduct> findXProductionByDate(Date startDate, Date endDate){
        return em.createNamedQuery("XProductionProduct.findProductionByDates")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public List<XProductionProduct> findXProductionByProductItem(String productItemCode, Date startDate, Date endDate){
        return em.createNamedQuery("XProductionProduct.findProductionByProductItem")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("productItemCode", productItemCode)
                .getResultList();
    }

    /** Variantes con filtro de almacen: agregan join a productItem y filtran por warehouseCode en SQL,
     * para que el reporte no traiga produccion/ventas/acopio de otros almacenes y luego los descarte. **/

    @SuppressWarnings(value = "unchecked")
    public List<ProductionProduct> findProductionByDate(Date startDate, Date endDate, String warehouseCode){
        return em.createQuery(
                "select pp from ProductionProduct pp " +
                "left join pp.productionPlan plan " +
                "join pp.productItem pi " +
                "where plan.date between :startDate and :endDate " +
                "and pi.warehouseCode = :warehouseCode")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("warehouseCode", warehouseCode)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<XProductionProduct> findXProductionByDate(Date startDate, Date endDate, String warehouseCode){
        return em.createQuery(
                "select pp from XProductionProduct pp " +
                "left join pp.productionPlan plan " +
                "join pp.productItem pi " +
                "where plan.date between :startDate and :endDate " +
                "and pi.warehouseCode = :warehouseCode")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("warehouseCode", warehouseCode)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<ProductionOrder> findProductionOrders(Date startDate, Date endDate, String warehouseCode){
        return em.createQuery(
                "select po from ProductionOrder po " +
                "left join po.productionPlanning plan " +
                "join po.productComposition pc " +
                "join pc.processedProduct proc " +
                "join proc.productItem pi " +
                "where plan.date between :startDate and :endDate " +
                "and pi.warehouseCode = :warehouseCode")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("warehouseCode", warehouseCode)
                .getResultList();
    }

    @SuppressWarnings(value = "unchecked")
    public List<BaseProduct> findBaseProductByDate(Date startDate, Date endDate, String warehouseCode){
        return em.createQuery(
                "select distinct bp from BaseProduct bp " +
                "left join bp.productionPlanningBase ppb " +
                "join bp.singleProducts sp " +
                "join sp.productProcessingSingle pps " +
                "join pps.metaProduct mp " +
                "join mp.productItem pi " +
                "where ppb.date between :startDate and :endDate " +
                "and pi.warehouseCode = :warehouseCode")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("warehouseCode", warehouseCode)
                .getResultList();
    }

}
