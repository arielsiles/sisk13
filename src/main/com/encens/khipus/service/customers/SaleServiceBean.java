package com.encens.khipus.service.customers;

import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;

@Stateless
@Name("saleService")
@AutoCreate
public class SaleServiceBean extends GenericServiceBean implements SaleService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @In
    private InventoryService inventoryService;

    @Override
    public String createSale(CustomerOrder customerOrder) {

        for (ArticleOrder articleOrder:customerOrder.getArticleOrderList()){
            articleOrder.setCustomerOrder(customerOrder);
            em.persist(articleOrder);
        }
        em.persist(customerOrder);
        em.flush();

        return Outcome.SUCCESS;
    }

    @Override
    public void updateCustomerOrder(CustomerOrder customerOrder) {
        em.merge(customerOrder);
        em.flush();
    }

    @Override
    public Long findLastSaleId(User user) {
        System.out.println("------>> userid: " + user.getId());
        System.out.println("------>> user: " + user.getUsername());
        Long result =  null;
        result = (Long) em.createQuery("select max(c.id) from CustomerOrder c " +
                " where c.user =:user")
        .setParameter("user", user)
        .getSingleResult();

        return result;
    }

    @Override
    public CustomerOrder findSaleById(Long id) {
        CustomerOrder result;
        result = (CustomerOrder) em.createQuery("select c from CustomerOrder c " +
                " where c.id =:id")
                .setParameter("id", id)
                .getSingleResult();

        System.out.println("----------------> ultima venta:" + result);

        return result;
    }

    @Override
    public void updateArticleForOutputs(ArticleOrder articleOrder) {

        ProductItem productItem = getEntityManager().find(ProductItem.class, articleOrder.getProductItem().getId());

        /** todo Controlar negativos, si cuando se requiera **/

        /** Actualiza CT **/
        BigDecimal total = BigDecimalUtil.multiply(productItem.getCu(), BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), 6);
        BigDecimal newTotalCost = BigDecimalUtil.subtract(productItem.getCt(), total, 6);
        productItem.setCt(newTotalCost);

        /** Actualiza Saldo_Mon **/
        BigDecimal totalCost = BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(articleOrder.getTotal()));
        BigDecimal newInvestmentAmount = BigDecimalUtil.subtract(productItem.getInvestmentAmount(), totalCost, 6);
        productItem.setInvestmentAmount(newInvestmentAmount);

        System.out.println("------------>1.  Actualizando Inv_Articulos: " + productItem.getFullName());

        em.merge(productItem);
        em.flush();
        System.out.println("------------>2. Actualizando Inv_Articulos: " + productItem.getFullName());

    }

    @Override
    public void removeFromInventory(ArticleOrder articleOrder){

        Inventory inventory = inventoryService.findInventoryByProductItemCode(articleOrder.getProductItem().getProductItemCode());
        InventoryDetail inventoryDetail = inventoryService.findInventoryDetailByProductItemCode(articleOrder.getProductItem().getProductItemCode());

        System.out.println("-----------------> Inventory: " + inventory);
        System.out.println("-----------------> InventoryDetail: " + inventoryDetail);

        BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(articleOrder.getTotal());
        BigDecimal availableQuantity = inventoryDetail.getQuantity();
        BigDecimal newAvailableQuantity = BigDecimalUtil.subtract(availableQuantity, requiredQuantity);

        /*if (BigDecimal.ZERO.compareTo(newAvailableQuantity) == 1) {
            throw new InventoryUnitaryBalanceException(availableQuantity, articleOrder.getProductItem());
        }*/

        BigDecimal actualUnitaryBalance = inventory.getUnitaryBalance();
        BigDecimal newUnitaryBalance = BigDecimalUtil.subtract(actualUnitaryBalance, requiredQuantity);

        /*if (BigDecimal.ZERO.compareTo(newUnitaryBalance) == 1) {
            throw new InventoryUnitaryBalanceException(actualUnitaryBalance, articleOrder.getProductItem());
        }*/


        System.out.println("----------------> newAvailableQuantity: " + newAvailableQuantity);
        System.out.println("----------------> newUnitaryBalance: " + newUnitaryBalance);


        inventory.setUnitaryBalance(newUnitaryBalance);
        em.refresh(inventory);
        em.merge(inventory);
        em.flush();

        inventoryDetail.setQuantity(newAvailableQuantity);
        em.refresh(inventoryDetail);
        em.merge(inventoryDetail);
        em.flush();
    }
}
