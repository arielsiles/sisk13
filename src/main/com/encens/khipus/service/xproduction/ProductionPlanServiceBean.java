package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.CollectionForm;
import com.encens.khipus.model.xproduction.ProductionPlan;
import com.encens.khipus.model.xproduction.ProductionProduct;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Products services
 *
 * @author
 * @version $Id:
 */
@Stateless
@Name("productionPlanService")
@AutoCreate
public class ProductionPlanServiceBean implements ProductionPlanService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private InventoryService inventoryService;

    public void updateProductionPlan(ProductionPlan productionPlan, List<ProductionProduct> productList){

        for (ProductionProduct product : productList){
            if (product.getId() == null){
                product.setProductionPlan(productionPlan);
                em.persist(product);
                em.flush();

                updateProductForProduction(product);
                inventoryService.updateInventoryForProduction(product);

            }else {
                em.merge(product);
                em.flush();
            }
        }
        em.merge(productionPlan);
        em.flush();
    }

    public void updateProductionPlan(ProductionPlan productionPlan){
        em.merge(productionPlan);
        em.flush();
    }

    public void removeProduct(ProductionProduct product){
        em.remove(product);
        em.flush();
    }

    public List<ProductionProduct> getProductionProductList(ProductionPlan productionPlan){

        List<ProductionProduct> productionProductList = new ArrayList<ProductionProduct>();

        productionProductList =  (List<ProductionProduct>) em.createQuery("select p from ProductionProduct p " +
                " where p.productionPlan =:productionPlan " +
                " and p.production is null ")
                .setParameter("productionPlan", productionPlan)
                .getResultList();

        return  productionProductList;
    }

    public double findTotalWeighed(Date date){

        double result = 0.0;
        try {
            CollectionForm collectionForm = (CollectionForm)em.createQuery(" select collectionForm from CollectionForm collectionForm" +
                    " where collectionForm.date = :date ")
                    .setParameter("date",date)
                    .getSingleResult();

            if (collectionForm != null)
                result = collectionForm.getTotalWeighed();

        }catch (NoResultException exception){
            result = 0;
        }

        return result;
    }

    public List<ProductionPlan> getProductionPlanList(Date startDate, Date endDate){

        List<ProductionPlan> productionPlanList = (List<ProductionPlan>) em.createQuery("select p from ProductionPlan p " +
                "where p.date between :startDate and :endDate")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
        return productionPlanList;
    }

    @Override
    public void updateProductForProduction(ProductionProduct product) {

        ProductItem productItem = em.find(ProductItem.class, product.getProductItem().getId());

        /** Actualiza CT **/
        BigDecimal total = BigDecimalUtil.multiply(productItem.getCu(), BigDecimalUtil.toBigDecimal(product.getQuantity()), 6);
        BigDecimal newTotalCost = BigDecimalUtil.sum(productItem.getCt(), total, 6);
        productItem.setCt(newTotalCost);

        /** Actualiza Saldo_Mon **/
        BigDecimal totalCost = BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(product.getQuantity()));
        BigDecimal newInvestmentAmount = BigDecimalUtil.sum(productItem.getInvestmentAmount(), totalCost, 6);
        productItem.setInvestmentAmount(newInvestmentAmount);

        em.merge(productItem);
        em.flush();

    }

    @Override
    public void updateProductItemRemoveFromProduction(ProductionProduct product) {

        ProductItem productItem = em.find(ProductItem.class, product.getProductItem().getId());

        /** Actualiza CT **/
        BigDecimal total = BigDecimalUtil.multiply(productItem.getCu(), BigDecimalUtil.toBigDecimal(product.getQuantity()), 6);
        BigDecimal newTotalCost = BigDecimalUtil.subtract(productItem.getCt(), total, 6);
        productItem.setCt(newTotalCost);

        /** Actualiza Saldo_Mon **/
        BigDecimal totalCost = BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(product.getQuantity()));
        BigDecimal newInvestmentAmount = BigDecimalUtil.subtract(productItem.getInvestmentAmount(), totalCost, 6);
        productItem.setInvestmentAmount(newInvestmentAmount);

        em.merge(productItem);
        em.flush();

    }
}
