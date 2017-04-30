package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.warehouse.InventoryDetail;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Local
public interface InventoryManagementService extends GenericService {
    List<InventoryDetail> getAvailableInventoryDetails(String companyNumber,
                                                       BusinessUnit executorUnit,
                                                       String warehouseCode,
                                                       String productItemCode);

    void createInventoryDetail(Long sourceInventoryDetailId,
                               CostCenter targetCostCenter,
                               BigDecimal quantity,
                               String description) throws InventoryUnitaryBalanceException;
}
