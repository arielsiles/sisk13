package com.encens.khipus.service.warehouse;

import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.ProductionProcess;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface WarehouseVoucherService {

    List<WarehouseVoucher> findWarehouseVoucherNoAccounting(WarehouseDocumentType documentType, Date startDate, Date endDate);

    String createWarehouseVoucherListAccounting(List<WarehouseVoucher> warehouseVoucherList);

    WarehouseVoucher findWarehouseVoucherByPurchaseOrder(PurchaseOrder purchaseOrder);

    List<ProductionProcess> getProductionProcesses(ProductionLine productionLine);

    List<ProductItem> getFinishedGoodsList();
}
