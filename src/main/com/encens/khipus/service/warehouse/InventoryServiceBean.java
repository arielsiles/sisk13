package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.ProductionProduct;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.model.xproduction.XProductionProduct;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
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
    public void updateInventoryForProduction(ProductionProduct product){

        Inventory inventory = findInventoryByProductItemCode(product.getProductItemCode());
        System.out.println("-----------> **** ACTUALIZANDO PRODUCTO PARA PRODUCCION Inventory: " + inventory.getProductItem().getFullName());
        BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(product.getQuantity());
        BigDecimal availableQuantity = inventory.getUnitaryBalance();
        BigDecimal newAvailableQuantity = BigDecimalUtil.sum(availableQuantity, requiredQuantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        eventEm.merge(inventory);
        eventEm.flush();

        InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(product.getProductItemCode());
        inventoryDetail.setQuantity(inventory.getUnitaryBalance());
        eventEm.merge(inventoryDetail);
        eventEm.flush();
    }

    @Override
    public void updateInventoryForCollectMaterial(CollectMaterial collectMaterial){

        /** Update Inventory **/
        Inventory inventory = findInventoryByProductItemCode(collectMaterial.getMetaProduct().getProductItemCode());
        BigDecimal quantity = collectMaterial.getBalanceWeight();

        BigDecimal availableQuantity = inventory.getUnitaryBalance();
        BigDecimal newAvailableQuantity = BigDecimalUtil.sum(availableQuantity, quantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        eventEm.merge(inventory);
        eventEm.flush();

        InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(collectMaterial.getMetaProduct().getProductItemCode());
        inventoryDetail.setQuantity(inventory.getUnitaryBalance());
        eventEm.merge(inventoryDetail);
        eventEm.flush();

        BigDecimal amountToAdd = collectMaterialNetAmount(collectMaterial);

        increaseProductItemAmount(collectMaterial.getMetaProduct().getProductItem(), newAvailableQuantity, amountToAdd, amountToAdd);

    }

    @Override
    public void revertInventoryForCollectMaterial(CollectMaterial collectMaterial){

        /** Reversa exacta de updateInventoryForCollectMaterial: descuenta del saldo la misma
         *  cantidad (balanceWeight) y del Saldo_Mon el mismo valor neto que se sumo al aprobar. **/
        Inventory inventory = findInventoryByProductItemCode(collectMaterial.getMetaProduct().getProductItemCode());
        BigDecimal quantity = collectMaterial.getBalanceWeight();

        BigDecimal newAvailableQuantity = BigDecimalUtil.subtract(inventory.getUnitaryBalance(), quantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        eventEm.merge(inventory);
        eventEm.flush();

        InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(collectMaterial.getMetaProduct().getProductItemCode());
        inventoryDetail.setQuantity(inventory.getUnitaryBalance());
        eventEm.merge(inventoryDetail);
        eventEm.flush();

        decreaseProductItemAmount(collectMaterial.getMetaProduct().getProductItem(), newAvailableQuantity,
                collectMaterialNetAmount(collectMaterial));

    }

    /** Valor NETO que el acopio aporta al Saldo_Mon del articulo, consistente con la
     *  contabilizacion en CollectMaterialServiceBean.createCollectMaterialListAccounting:
     *  precio es Bs/Tonelada y la cantidad que entra al inventario es balanceWeight (KG),
     *  asi que valor bruto = (KG / 1000) * precio; si hay factura se descuenta el IVA
     *  (credito fiscal): neto = bruto - bruto * VAT.
     *  Correccion historica: antes usaba precio/100 (en vez de /1000) y dividia por
     *  VAT_COMPLEMENT (en vez de descontar el IVA), lo que inflaba saldo_mon/costo_uni ~13x.
     *  Alta, reversa y previsualizacion comparten esta formula a proposito: no pueden
     *  desalinearse. */
    @Override
    public BigDecimal collectMaterialNetAmount(CollectMaterial collectMaterial) {
        BigDecimal weightTon   = BigDecimalUtil.divide(collectMaterial.getBalanceWeight(), BigDecimalUtil.ONE_THOUSAND, 6);
        BigDecimal grossAmount = BigDecimalUtil.multiply(weightTon, collectMaterial.getPrice(), 6);
        if ( collectMaterial.getHasInvoice() ) {
            BigDecimal taxCreditFiscal = BigDecimalUtil.multiply(grossAmount, Constants.VAT, 6);
            return BigDecimalUtil.subtract(grossAmount, taxCreditFiscal, 6);
        }
        return grossAmount;
    }

    /** Inversa de increaseProductItemAmount. Si el saldo queda en cero el costo unitario
     *  tambien: no se puede dividir entre cero. */
    private void decreaseProductItemAmount(ProductItem productItem, BigDecimal newQuantityInventory, BigDecimal amountToSubtract) {

        ProductItem managed = eventEm.find(ProductItem.class, productItem.getId());
        if (managed == null) {
            managed = productItem;
        }

        BigDecimal newInvestmentAmount = BigDecimalUtil.subtract(managed.getInvestmentAmount(), amountToSubtract, 6);
        managed.setInvestmentAmount(newInvestmentAmount);
        managed.setUnitCost( BigDecimalUtil.isZeroOrNull(newQuantityInventory) ?
                BigDecimal.ZERO : BigDecimalUtil.divide(newInvestmentAmount, newQuantityInventory, 6) );

        eventEm.flush();

    }

    @Override
    public void increaseProductItemAmount(ProductItem productItem, BigDecimal newQuantityInventory, BigDecimal amountToAdd, BigDecimal amountCTAdd) {

        /** Actualiza Saldo_Mon.
         *  Se recarga el ProductItem por su PK desde eventEm para operar sobre la
         *  instancia gestionada con la version vigente. Asi se evita el
         *  StaleObjectStateException que ocurria al mergear la copia (con version
         *  vieja) proveniente de la conversacion cuando la fila de inv_articulos
         *  fue actualizada por otra transaccion entremedio. */
        ProductItem managed = eventEm.find(ProductItem.class, productItem.getId());
        if (managed == null) {
            managed = productItem;
        }

        BigDecimal newInvestmentAmount = BigDecimalUtil.sum(managed.getInvestmentAmount(), amountToAdd, 6);
        managed.setInvestmentAmount(newInvestmentAmount);
        managed.setUnitCost( BigDecimalUtil.divide(newInvestmentAmount, newQuantityInventory, 6) );

        eventEm.flush();

    }

    @Override
    public void updateInventoryForProduction(XProductionProduct product){

        Inventory inventory = findInventoryByProductItemCode(product.getProductItemCode());
        System.out.println("-----------> **** ACTUALIZANDO PRODUCTO PARA PRODUCCION Inventory: " + inventory.getProductItem().getFullName());
        BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(product.getQuantity());
        BigDecimal availableQuantity = inventory.getUnitaryBalance();
        BigDecimal newAvailableQuantity = BigDecimalUtil.sum(availableQuantity, requiredQuantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        eventEm.merge(inventory);
        eventEm.flush();

        InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(product.getProductItemCode());
        inventoryDetail.setQuantity(inventory.getUnitaryBalance());
        eventEm.merge(inventoryDetail);
        eventEm.flush();
    }


    @Override
    public void updateInventoryForSalesAnnuled(CustomerOrder customerOrder) {

        for (ArticleOrder articleOrder : customerOrder.getArticleOrderList()){
            Inventory inventory = findInventoryByProductItemCode(articleOrder.getCodArt());
            BigDecimal returnQuantity    = BigDecimalUtil.toBigDecimal(articleOrder.getTotal());
            BigDecimal availableQuantity = inventory.getUnitaryBalance();

            BigDecimal newAvailableQuantity = BigDecimalUtil.sum(availableQuantity, returnQuantity);
            inventory.setUnitaryBalance(newAvailableQuantity);
            eventEm.merge(inventory);
            eventEm.flush();

            InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(articleOrder.getCodArt());
            inventoryDetail.setQuantity(inventory.getUnitaryBalance());
            eventEm.merge(inventoryDetail);
            eventEm.flush();

            saleService.updateArticleForInputs(articleOrder);
        }
    }

    @Override
    public void updateInventoryRemoveFromProduction(ProductionProduct product){

        Inventory inventory = findInventoryByProductItemCode(product.getProductItemCode());
        System.out.println("-----------> **** REMOVE PRODUCTO PRODUCCION Inventory: " + inventory.getProductItem().getFullName());
        BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(product.getQuantity());
        BigDecimal availableQuantity = inventory.getUnitaryBalance();
        BigDecimal newAvailableQuantity = BigDecimalUtil.subtract(availableQuantity, requiredQuantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        eventEm.merge(inventory);
        eventEm.flush();

        InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(product.getProductItemCode());
        inventoryDetail.setQuantity(inventory.getUnitaryBalance());
        eventEm.merge(inventoryDetail);
        eventEm.flush();
    }

    @Override
    public void updateInventoryRemoveFromProduction(XProductionProduct product){

        Inventory inventory = findInventoryByProductItemCode(product.getProductItemCode());
        System.out.println("-----------> **** REMOVE PRODUCTO PRODUCCION Inventory: " + inventory.getProductItem().getFullName());
        BigDecimal requiredQuantity = BigDecimalUtil.toBigDecimal(product.getQuantity());
        BigDecimal availableQuantity = inventory.getUnitaryBalance();
        BigDecimal newAvailableQuantity = BigDecimalUtil.subtract(availableQuantity, requiredQuantity);
        inventory.setUnitaryBalance(newAvailableQuantity);
        eventEm.merge(inventory);
        eventEm.flush();

        InventoryDetail inventoryDetail = findInventoryDetailByProductItemCode(product.getProductItemCode());
        inventoryDetail.setQuantity(inventory.getUnitaryBalance());
        eventEm.merge(inventoryDetail);
        eventEm.flush();
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
