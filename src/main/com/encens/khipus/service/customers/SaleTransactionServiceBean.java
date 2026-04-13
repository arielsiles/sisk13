package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Servicio transaccional para creacion atomica de ventas.
 * Toda la operacion (persist pedido + actualizacion inventario + costos)
 * se ejecuta en una sola transaccion REQUIRES_NEW con un unico EntityManager.
 *
 * Usa PESSIMISTIC_WRITE en la lectura de inventario para serializar
 * accesos concurrentes al mismo producto.
 */
@Stateless
@Name("saleTransactionService")
@AutoCreate
public class SaleTransactionServiceBean implements SaleTransactionService {

    @PersistenceContext(unitName = "khipus")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createSaleWithInventory(CustomerOrder customerOrder) {

        /** 1. Persistir pedido y articulos **/
        Collection<ArticleOrder> articleOrderList = customerOrder.getArticleOrderList();
        for (ArticleOrder articleOrder : articleOrderList) {
            articleOrder.setCustomerOrder(customerOrder);
            em.persist(articleOrder);
        }
        em.persist(customerOrder);
        em.flush();

        /** 2. Actualizar inventario con locking pesimista **/
        for (ArticleOrder articleOrder : articleOrderList) {
            updateInventory(articleOrder);
            updateArticleCosts(articleOrder);
        }
    }

    /**
     * Actualiza Inventory e InventoryDetail para un articulo vendido.
     * Usa PESSIMISTIC_WRITE para evitar race conditions.
     */
    private void updateInventory(ArticleOrder articleOrder) {
        Inventory inventory = findInventoryWithLock(articleOrder.getCodArt());
        if (inventory == null) return;

        BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(articleOrder.getTotal());
        BigDecimal availableQuantity = inventory.getUnitaryBalance();
        BigDecimal newAvailableQuantity = BigDecimalUtil.subtract(availableQuantity, requiredQuantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        em.merge(inventory);
        em.flush();

        InventoryDetail inventoryDetail = findInventoryDetail(articleOrder.getCodArt());
        if (inventoryDetail != null) {
            inventoryDetail.setQuantity(inventory.getUnitaryBalance());
            em.merge(inventoryDetail);
            em.flush();
        }
    }

    /**
     * Actualiza costos del ProductItem (CT y Saldo_Mon).
     * Misma logica que SaleServiceBean.updateArticleForOutputs().
     */
    private void updateArticleCosts(ArticleOrder articleOrder) {
        ProductItem productItem = em.find(ProductItem.class, articleOrder.getProductItem().getId());

        /** Actualiza CT **/
        BigDecimal total = BigDecimalUtil.multiply(productItem.getCu(), BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), 6);
        BigDecimal newTotalCost = BigDecimalUtil.subtract(productItem.getCt(), total, 6);
        productItem.setCt(newTotalCost);

        /** Actualiza Saldo_Mon **/
        BigDecimal totalCost = BigDecimalUtil.multiply(productItem.getUnitCost(), BigDecimalUtil.toBigDecimal(articleOrder.getTotal()));
        BigDecimal newInvestmentAmount = BigDecimalUtil.subtract(productItem.getInvestmentAmount(), totalCost, 6);
        productItem.setInvestmentAmount(newInvestmentAmount);

        em.merge(productItem);
        em.flush();
    }

    /**
     * Busca Inventory por codigo de producto con lock de escritura (JPA 1.0 WRITE).
     * En Hibernate 3.3 esto ejecuta SELECT FOR UPDATE + version check,
     * serializando actualizaciones concurrentes al mismo producto.
     */
    private Inventory findInventoryWithLock(String productItemCode) {
        try {
            Inventory inventory = (Inventory) em.createQuery(
                    "SELECT i FROM Inventory i WHERE i.productItem.productItemCode = :productItemCode")
                    .setParameter("productItemCode", productItemCode)
                    .getSingleResult();
            em.lock(inventory, LockModeType.WRITE);
            return inventory;
        } catch (NoResultException e) {
            return null;
        }
    }

    private InventoryDetail findInventoryDetail(String productItemCode) {
        try {
            return (InventoryDetail) em.createQuery(
                    "SELECT d FROM InventoryDetail d WHERE d.productItemCode = :productItemCode")
                    .setParameter("productItemCode", productItemCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
