package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.warehouse.reports.ProductInventoryReportAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.warehouse.MovementDetail;

import javax.ejb.Local;
import java.util.Collection;
import java.util.Date;

/**
 * @author
 * @version 2.0
 */
@Local
public interface InitialInventoryService extends GenericService {
    public void createInitialInventory(Collection<ProductInventoryReportAction.CollectionData> collectionDatas, String warehouseCode, Date startDate);
}
