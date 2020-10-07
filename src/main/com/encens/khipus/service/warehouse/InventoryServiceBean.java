package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.0
 */
@Stateless
@Name("inventoryService")
@AutoCreate
public class InventoryServiceBean extends GenericServiceBean implements InventoryService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private SaleService saleService;

    /**
     * Finds the unitaryBalance quantity by ProductItemPK and WarehousePK
     *
     * @param warehouseId   the warehouse filter
     * @param productItemId the productItem filter
     * @return the unitaryBalance quantity by ProductItemPK and WarehousePK
     */
    public BigDecimal findUnitaryBalanceByProductItemAndArticle(WarehousePK warehouseId, ProductItemPK productItemId) {
        try {
            return (BigDecimal) eventEm.createNamedQuery("Inventory.findUnitaryBalanceByProductItemAndArticle")
                    .setParameter("warehouseId", warehouseId)
                    .setParameter("productItemId", productItemId)
                    .getSingleResult();
        } catch (NoResultException exception) {
            return BigDecimal.ZERO;
        }
    }
    public Warehouse findWarehouseByItemArticle(ProductItem productItem){
        try {
            return (Warehouse) eventEm.createNamedQuery("Inventory.findWarehouseByItemArticle")
                    .setParameter("productItemId",productItem.getId())
                    .getSingleResult();
        }catch (NoResultException e){
            return null;
        }

    }

    @Override
    public void updateInventoryForSales(CustomerOrder customerOrder) {

        for (ArticleOrder articleOrder : customerOrder.getArticleOrderList()){

            Inventory inventory = findInventoryByProductItemCode(articleOrder.getCodArt());
            System.out.println("-----------> **** ACTUALIZANDO PRODUCTO Inventory: " + inventory.getProductItem().getFullName());
            BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(articleOrder.getTotal());
            BigDecimal availableQuantity = inventory.getUnitaryBalance();
            BigDecimal newAvailableQuantity = BigDecimalUtil.subtract(availableQuantity, requiredQuantity);
            inventory.setUnitaryBalance(newAvailableQuantity);
            eventEm.merge(inventory);
            eventEm.flush();

            InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(articleOrder.getCodArt());
            inventoryDetail.setQuantity(inventory.getUnitaryBalance());
            eventEm.merge(inventoryDetail);
            eventEm.flush();

            saleService.updateArticleForOutputs(articleOrder);
        }

    }

    @Override
    public Inventory findInventoryByProductItemCode(String productItemCode) {
        try {
            return (Inventory) eventEm.createNamedQuery("Inventory.findInventoryByProductItemCode")
                    .setParameter("productItemCode", productItemCode)
                    .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    @Override
    public InventoryDetail findInventoryDetailByProductItemCode(String productItemCode) {
        try {
            return (InventoryDetail) eventEm.createNamedQuery("InventoryDetail.findInventoryDetailByProductItemCode")
                    .setParameter("productItemCode", productItemCode)
                    .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }


}
