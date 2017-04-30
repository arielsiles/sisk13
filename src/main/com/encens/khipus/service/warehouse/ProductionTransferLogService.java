package com.encens.khipus.service.warehouse;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.warehouse.ProductionTransferLog;
import com.encens.khipus.model.warehouse.WarehouseSlot;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ProductionTransferLogService extends GenericService {

    public void prepareForReceiving(ProductionTransferLog transfer);

    List<WarehouseSlot> findWarehouseSlots(MetaProduct product);
}
