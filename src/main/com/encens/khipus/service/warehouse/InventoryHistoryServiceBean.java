package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.warehouse.InventoryHistory;
import com.encens.khipus.model.warehouse.InventoryHistoryPK;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author
 * @version 2.0
 */
@Stateless
@Name("inventoryHistoryService")
@AutoCreate
public class InventoryHistoryServiceBean extends GenericServiceBean implements InventoryHistoryService {
    public void updateInventoryHistory(MovementDetail movementDetail) {
        String monthCode = buildMonthCode();
        InventoryHistoryPK pk = new InventoryHistoryPK(movementDetail.getCompanyNumber(),
                monthCode,
                movementDetail.getWarehouseCode(),
                movementDetail.getProductItem().getId().getProductItemCode());
        InventoryHistory inventoryHistory = getInventoryHistory(pk);
        if (null == inventoryHistory) {
            createInventoryHistory(movementDetail, pk);
        } else {
            updateInventoryHistory(movementDetail, inventoryHistory);
        }
    }

    private void createInventoryHistory(MovementDetail movementDetail,
                                        InventoryHistoryPK pk) {

        InventoryHistory inventoryHistory = new InventoryHistory();
        inventoryHistory.setId(pk);


        if (MovementDetailType.E.equals(movementDetail.getMovementType())) {
            inventoryHistory.setIncomingQuantity(movementDetail.getQuantity());
            if (null != movementDetail.getAmount()) {
                inventoryHistory.setIncomingAmount(movementDetail.getAmount());
            } else {
                inventoryHistory.setIncomingAmount(BigDecimal.ZERO);
            }
            inventoryHistory.setOutgoingQuantity(BigDecimal.ZERO);
            inventoryHistory.setOutgoingAmount(BigDecimal.ZERO);
        }

        if (MovementDetailType.S.equals(movementDetail.getMovementType())) {
            inventoryHistory.setOutgoingQuantity(movementDetail.getQuantity());
            if (null != movementDetail.getAmount()) {
                inventoryHistory.setOutgoingAmount(movementDetail.getAmount());
            } else {
                inventoryHistory.setOutgoingAmount(BigDecimal.ZERO);
            }
            inventoryHistory.setIncomingQuantity(BigDecimal.ZERO);
            inventoryHistory.setIncomingAmount(BigDecimal.ZERO);
        }

        getEntityManager().persist(inventoryHistory);
        getEntityManager().flush();
    }

    private void updateInventoryHistory(MovementDetail movementDetail, InventoryHistory inventoryHistory) {
        if (MovementDetailType.E.equals(movementDetail.getMovementType())) {
            BigDecimal actualIncomingQuantity = inventoryHistory.getIncomingQuantity();
            BigDecimal actualIncomingAmount = inventoryHistory.getIncomingAmount();
            inventoryHistory.setIncomingQuantity(BigDecimalUtil.sum(actualIncomingQuantity, movementDetail.getQuantity()));
            if (null != movementDetail.getAmount()) {
                inventoryHistory.setIncomingAmount(BigDecimalUtil.sum(actualIncomingAmount, movementDetail.getAmount(), 6));
            }
        }

        if (MovementDetailType.S.equals(movementDetail.getMovementType())) {
            BigDecimal actualOutgoingQuantity = inventoryHistory.getOutgoingQuantity();
            BigDecimal actualOutgoingAmount = inventoryHistory.getOutgoingAmount();
            inventoryHistory.setOutgoingQuantity(BigDecimalUtil.sum(actualOutgoingQuantity, movementDetail.getQuantity()));
            if (null != movementDetail.getAmount()) {
                inventoryHistory.setOutgoingAmount(BigDecimalUtil.sum(actualOutgoingAmount, movementDetail.getAmount(), 6));
            }
        }

        getEntityManager().merge(inventoryHistory);
        getEntityManager().flush();
    }

    private InventoryHistory getInventoryHistory(InventoryHistoryPK id) {
        try {
            InventoryHistory inventoryHistory = findById(InventoryHistory.class, id);
            getEntityManager().refresh(inventoryHistory);

            return inventoryHistory;
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    private String buildMonthCode() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        //calendar.set(Calendar.MONTH,Calendar.MARCH);
        int month = calendar.get(Calendar.MONTH);

        String monthAsString = String.valueOf(month);
        if (month < 10) {
            monthAsString = "0" + month;
        }

        String code = year + monthAsString;

        log.debug("The month code is: " + code);

        return code;
    }
}
