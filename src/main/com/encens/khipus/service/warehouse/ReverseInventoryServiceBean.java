package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryProductItemNotFoundException;
import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Implementacion del servicio de reversion de inventario y costo promedio.
 *
 * Reverso simetrico:
 *   - Entrada (E) revertida: resta quantity del stock y resta amount del
 *     investmentAmount del producto; descuenta purchasePrice del ct.
 *   - Salida  (S) revertida: suma quantity al stock y suma amount al
 *     investmentAmount; suma purchasePrice al ct.
 *
 * unitCost y cu se recalculan dividiendo el nuevo investmentAmount / ct por
 * la nueva sumUnitaryBalances (total de stock agregado entre todos los
 * almacenes del producto, criterio identico al usado en aprobacion).
 *
 * @version 6.0.70
 */
@Stateless
@Name("reverseInventoryService")
@AutoCreate
public class ReverseInventoryServiceBean extends GenericServiceBean implements ReverseInventoryService {

    @In
    private ApprovalWarehouseVoucherService approvalWarehouseVoucherService;

    @Override
    public void reverseInventory(WarehouseVoucher warehouseVoucher,
                                 Warehouse warehouse,
                                 MovementDetail movementDetail)
            throws InventoryUnitaryBalanceException,
                   InventoryProductItemNotFoundException {

        Inventory inventory = findInventory(warehouse, movementDetail.getProductItem());
        if (null == inventory) {
            throw new InventoryProductItemNotFoundException(
                    "Cannot reverse: inventory not found for product " + movementDetail.getProductItemCode(),
                    warehouseVoucher.getExecutorUnit() != null ? warehouseVoucher.getExecutorUnit().getFullName() : null,
                    movementDetail.getProductItem(),
                    warehouse);
        }

        BusinessUnit executorUnit = warehouseVoucher.getExecutorUnit();
        String costCenterCode = warehouseVoucher.getCostCenterCode();
        // Para reverso simetrico usamos el mismo par (executor, costCenter) que se
        // uso en la aprobacion. En un vale de transferencia entre U.Ejec. la entrada
        // original fue al target: al revertir ese detalle de entrada pasamos el target.
        if (warehouseVoucher.isExecutorUnitTransfer()
                && MovementDetailType.E.equals(movementDetail.getMovementType())) {
            executorUnit = warehouseVoucher.getTargetExecutorUnit();
            costCenterCode = warehouseVoucher.getTargetCostCenterCode();
        }

        InventoryDetail inventoryDetail = approvalWarehouseVoucherService.getInventoryDetail(
                inventory, executorUnit, costCenterCode);

        BigDecimal quantity = movementDetail.getQuantity();

        if (MovementDetailType.E.equals(movementDetail.getMovementType())) {
            // El movimiento original fue entrada: revertir resta del stock.
            if (null == inventoryDetail) {
                throw new InventoryUnitaryBalanceException(
                        BigDecimal.ZERO, movementDetail.getProductItem());
            }
            BigDecimal availableDetail = inventoryDetail.getQuantity();
            BigDecimal newDetailQuantity = BigDecimalUtil.subtract(availableDetail, quantity);
            if (BigDecimal.ZERO.compareTo(newDetailQuantity) == 1) {
                throw new InventoryUnitaryBalanceException(
                        availableDetail, movementDetail.getProductItem());
            }
            inventoryDetail.setQuantity(newDetailQuantity);
            getEntityManager().merge(inventoryDetail);

            BigDecimal actualUnitaryBalance = inventory.getUnitaryBalance();
            BigDecimal newUnitaryBalance = BigDecimalUtil.subtract(actualUnitaryBalance, quantity);
            if (BigDecimal.ZERO.compareTo(newUnitaryBalance) == 1) {
                throw new InventoryUnitaryBalanceException(
                        actualUnitaryBalance, movementDetail.getProductItem());
            }
            inventory.setUnitaryBalance(newUnitaryBalance);
            getEntityManager().merge(inventory);
            getEntityManager().flush();
        } else {
            // El movimiento original fue salida: revertir suma al stock.
            if (null == inventoryDetail) {
                createInventoryDetailForReverse(inventory, executorUnit, costCenterCode, quantity);
            } else {
                BigDecimal newDetailQuantity = BigDecimalUtil.sum(inventoryDetail.getQuantity(), quantity);
                inventoryDetail.setQuantity(newDetailQuantity);
                getEntityManager().merge(inventoryDetail);
            }

            BigDecimal actualUnitaryBalance = inventory.getUnitaryBalance();
            inventory.setUnitaryBalance(BigDecimalUtil.sum(actualUnitaryBalance, quantity));
            if (!getEntityManager().contains(inventory)) {
                getEntityManager().merge(inventory);
            }
            getEntityManager().flush();
        }
    }

    @Override
    public void reverseProductItemCost(WarehouseVoucher warehouseVoucher,
                                       MovementDetail movementDetail) {
        ProductItem productItem = getEntityManager().find(ProductItem.class,
                movementDetail.getProductItem().getId());
        if (null == productItem || !Boolean.TRUE.equals(productItem.getControlValued())) {
            return;
        }

        BigDecimal sumUnitaryBalances = (BigDecimal) getEntityManager()
                .createNamedQuery("Inventory.sumUnitaryBalancesByArticleCode")
                .setParameter("articleNumber", productItem.getId().getProductItemCode())
                .setParameter("companyNumber", movementDetail.getCompanyNumber())
                .getSingleResult();
        if (null == sumUnitaryBalances) {
            sumUnitaryBalances = BigDecimal.ZERO;
        }

        BigDecimal amount = movementDetail.getAmount() != null
                ? movementDetail.getAmount() : BigDecimal.ZERO;
        BigDecimal purchasePrice = movementDetail.getPurchasePrice() != null
                ? movementDetail.getPurchasePrice() : BigDecimal.ZERO;

        BigDecimal newInvestmentAmount;
        BigDecimal newCTAmount;

        if (MovementDetailType.E.equals(movementDetail.getMovementType())) {
            // Revertir entrada: descontar el monto que se habia sumado al aprobar
            newInvestmentAmount = BigDecimalUtil.subtract(productItem.getInvestmentAmount(), amount, 6);
            newCTAmount         = BigDecimalUtil.subtract(productItem.getCt(), purchasePrice, 6);
        } else {
            // Revertir salida: volver a sumar el monto al saldo monetario
            newInvestmentAmount = BigDecimalUtil.sum(productItem.getInvestmentAmount(), amount, 6);
            newCTAmount         = BigDecimalUtil.sum(productItem.getCt(), purchasePrice, 6);
        }

        if (sumUnitaryBalances.doubleValue() > 0) {
            productItem.setUnitCost(BigDecimalUtil.divide(newInvestmentAmount, sumUnitaryBalances, 6));
            productItem.setCu(BigDecimalUtil.divide(newCTAmount, sumUnitaryBalances, 6));
            productItem.setInvestmentAmount(newInvestmentAmount);
            productItem.setCt(newCTAmount);
        } else {
            // Sin stock remanente: saldos monetarios a cero; se preserva ultimo unitCost
            productItem.setInvestmentAmount(BigDecimal.ZERO);
            productItem.setCt(BigDecimal.ZERO);
        }

        getEntityManager().merge(productItem);
        getEntityManager().flush();
    }

    @Override
    public void reverseInventoryHistory(MovementDetail movementDetail) {
        String monthCode = buildMonthCode();
        InventoryHistoryPK pk = new InventoryHistoryPK(
                movementDetail.getCompanyNumber(),
                monthCode,
                movementDetail.getWarehouseCode(),
                movementDetail.getProductItem().getId().getProductItemCode());

        InventoryHistory inventoryHistory;
        try {
            inventoryHistory = findById(InventoryHistory.class, pk);
            getEntityManager().refresh(inventoryHistory);
        } catch (EntryNotFoundException e) {
            // No hay acumulado en el mes actual: nada que revertir en esta tabla.
            return;
        }

        BigDecimal quantity = movementDetail.getQuantity() != null
                ? movementDetail.getQuantity() : BigDecimal.ZERO;
        BigDecimal amount = movementDetail.getAmount() != null
                ? movementDetail.getAmount() : BigDecimal.ZERO;

        if (MovementDetailType.E.equals(movementDetail.getMovementType())) {
            inventoryHistory.setIncomingQuantity(
                    BigDecimalUtil.subtract(inventoryHistory.getIncomingQuantity(), quantity));
            inventoryHistory.setIncomingAmount(
                    BigDecimalUtil.subtract(inventoryHistory.getIncomingAmount(), amount, 6));
        } else {
            inventoryHistory.setOutgoingQuantity(
                    BigDecimalUtil.subtract(inventoryHistory.getOutgoingQuantity(), quantity));
            inventoryHistory.setOutgoingAmount(
                    BigDecimalUtil.subtract(inventoryHistory.getOutgoingAmount(), amount, 6));
        }

        getEntityManager().merge(inventoryHistory);
        getEntityManager().flush();
    }

    private Inventory findInventory(Warehouse warehouse, ProductItem productItem) {
        InventoryPK pk = new InventoryPK(
                warehouse.getId().getCompanyNumber(),
                warehouse.getId().getWarehouseCode(),
                productItem.getId().getProductItemCode());
        return getEntityManager().find(Inventory.class, pk);
    }

    private void createInventoryDetailForReverse(Inventory inventory,
                                                 BusinessUnit executorUnit,
                                                 String costCenterCode,
                                                 BigDecimal quantity) {
        // Replica la estrategia de ApprovalWarehouseVoucherServiceBean.createInventoryDetail
        // (insert nativo para trabajar con el secuencial propio de inv_inventario_detalle)
        Long nextId = ((Number) getEntityManager()
                .createNativeQuery("select coalesce(max(id_inv_det),0)+1 from inv_inventario_detalle")
                .getSingleResult()).longValue();

        getEntityManager().createNativeQuery(
                "insert into inv_inventario_detalle (id_inv_det, no_cia, cod_cc, cod_art, cantidad, version, cod_alm, idunidadnegocio) "
                + "values (:id, :no_cia, :cod_cc, :cod_art, :cantidad, 0, :cod_alm, :idunidadnegocio)")
                .setParameter("id", nextId)
                .setParameter("no_cia", inventory.getId().getCompanyNumber())
                .setParameter("cod_cc", costCenterCode)
                .setParameter("cod_art", inventory.getId().getArticleCode())
                .setParameter("cantidad", quantity)
                .setParameter("cod_alm", inventory.getId().getWarehouseCode())
                .setParameter("idunidadnegocio", executorUnit.getId())
                .executeUpdate();
        getEntityManager().flush();
    }

    private String buildMonthCode() {
        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        String monthAsString = month < 10 ? "0" + month : String.valueOf(month);
        return year + monthAsString;
    }
}
