package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.model.warehouse.WarehousePK;
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


}
