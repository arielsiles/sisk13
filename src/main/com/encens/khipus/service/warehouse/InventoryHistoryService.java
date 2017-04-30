package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.MovementDetail;

import javax.ejb.Local;

/**
 * @author
 * @version 2.0
 */
@Local
public interface InventoryHistoryService extends GenericService {
    void updateInventoryHistory(MovementDetail movementDetail);
}
