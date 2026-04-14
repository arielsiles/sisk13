package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.warehouse.Inventory;
import com.encens.khipus.model.warehouse.InventoryDetail;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

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
    public void createSaleWithInventory(CustomerOrder customerOrder, String sequenceName) {

        /** 1. Generar secuencia en la misma transaccion **/
        long saleCode = generateSequence(sequenceName);
        customerOrder.setCode(saleCode);

        /** 2. Persistir pedido y articulos **/
        Collection<ArticleOrder> articleOrderList = customerOrder.getArticleOrderList();
        for (ArticleOrder articleOrder : articleOrderList) {
            articleOrder.setCustomerOrder(customerOrder);
            em.persist(articleOrder);
        }
        em.persist(customerOrder);
        em.flush();

        /** 3. Actualizar inventario con locking pesimista **/
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
     * Busca Inventory por codigo de producto con locking pesimista real.
     * Usa native SELECT FOR UPDATE para bloquear la fila hasta el commit,
     * serializando accesos concurrentes (JPA 1.0 no tiene PESSIMISTIC_WRITE).
     */
    private Inventory findInventoryWithLock(String productItemCode) {
        String schema = Constants.FINANCES_SCHEMA;

        // 1. Adquirir row lock via SELECT FOR UPDATE nativo (bloquea hasta commit)
        java.util.List<?> rows = em.createNativeQuery(
                "SELECT cod_art FROM " + schema + ".inv_inventario WHERE cod_art = :code FOR UPDATE")
                .setParameter("code", productItemCode)
                .getResultList();

        if (rows.isEmpty()) return null;

        // 2. Cargar entidad fresca (fila ya bloqueada, version correcta)
        try {
            return (Inventory) em.createQuery(
                    "SELECT i FROM Inventory i WHERE i.productItem.productItemCode = :productItemCode")
                    .setParameter("productItemCode", productItemCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Genera el siguiente valor de secuencia dentro de la transaccion actual.
     * Si la transaccion hace rollback, la secuencia tambien se revierte.
     */
    private long generateSequence(String sequenceName) {
        String schema = Constants.FINANCES_SCHEMA;

        int updated = em.createNativeQuery(
                "UPDATE " + schema + "._sequence SET seq_val = seq_val + 1 WHERE seq_name = :name")
                .setParameter("name", sequenceName)
                .executeUpdate();

        if (updated == 0) {
            em.createNativeQuery(
                    "INSERT INTO " + schema + "._sequence (seq_name, seq_val) VALUES (:name, 1)")
                    .setParameter("name", sequenceName)
                    .executeUpdate();
            return 1;
        }

        Number result = (Number) em.createNativeQuery(
                "SELECT seq_val FROM " + schema + "._sequence WHERE seq_name = :name")
                .setParameter("name", sequenceName)
                .getSingleResult();

        return result.longValue();
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
