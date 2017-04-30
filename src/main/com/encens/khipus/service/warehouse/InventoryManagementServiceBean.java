package com.encens.khipus.service.warehouse;

import com.encens.khipus.exception.warehouse.InventoryUnitaryBalanceException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.interceptor.FinancesUser;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.warehouse.InventoryDetail;
import com.encens.khipus.model.warehouse.InventoryDetailLog;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.ProductItemPK;
import com.encens.khipus.service.finances.FinancesUserService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.2
 */
@Name("inventoryManagementService")
@FinancesUser
@Stateless
@AutoCreate
public class InventoryManagementServiceBean extends GenericServiceBean implements InventoryManagementService {

    @In
    private FinancesUserService financesUserService;

    @SuppressWarnings(value = "unchecked")
    public List<InventoryDetail> getAvailableInventoryDetails(String companyNumber,
                                                              BusinessUnit executorUnit,
                                                              String warehouseCode,
                                                              String productItemCode) {
        List<InventoryDetail> inventoryDetails = getEntityManager()
                .createNamedQuery("InventoryDetail.findByInventoryAndExecutorUnitCode")
                .setParameter("companyNumber", companyNumber)
                .setParameter("executorUnit", executorUnit)
                .setParameter("productItemCode", productItemCode)
                .setParameter("warehouseCode", warehouseCode).getResultList();

        List<InventoryDetail> result = new ArrayList<InventoryDetail>();
        for (int i = 0; i < inventoryDetails.size(); i++) {
            InventoryDetail inventoryDetail = inventoryDetails.get(i);

            if (inventoryDetail.getQuantity().compareTo(BigDecimal.ZERO) == 1) {
                result.add(inventoryDetail);
            }
        }

        return result;
    }

    public void createInventoryDetail(Long sourceInventoryDetailId,
                                      CostCenter targetCostCenter,
                                      BigDecimal quantity,
                                      String description) throws InventoryUnitaryBalanceException {
        InventoryDetail sourceInventoryDetail = getEntityManager().find(InventoryDetail.class, sourceInventoryDetailId);
        getEntityManager().refresh(sourceInventoryDetail);


        BigDecimal newSourceInventoryDetailQuantity = BigDecimalUtil.subtract(sourceInventoryDetail.getQuantity(), quantity);
        if (BigDecimal.ZERO.compareTo(newSourceInventoryDetailQuantity) == 1) {
            throw new InventoryUnitaryBalanceException(sourceInventoryDetail.getQuantity(),
                    getProductItem(sourceInventoryDetail.getCompanyNumber(), sourceInventoryDetail.getProductItemCode()));
        }

        InventoryDetail targetInventoryDetail = getInventoryDetail(sourceInventoryDetail.getCompanyNumber(),
                sourceInventoryDetail.getProductItemCode(),
                sourceInventoryDetail.getWarehouseCode(),
                sourceInventoryDetail.getExecutorUnit(),
                targetCostCenter.getId().getCode());

        if (null == targetInventoryDetail) {
            targetInventoryDetail = new InventoryDetail();
            targetInventoryDetail.setCompanyNumber(sourceInventoryDetail.getCompanyNumber());
            targetInventoryDetail.setProductItemCode(sourceInventoryDetail.getProductItemCode());
            targetInventoryDetail.setWarehouseCode(sourceInventoryDetail.getWarehouseCode());
            targetInventoryDetail.setExecutorUnit(sourceInventoryDetail.getExecutorUnit());
            targetInventoryDetail.setCostCenter(targetCostCenter);
            targetInventoryDetail.setQuantity(quantity);

            getEntityManager().persist(targetInventoryDetail);

        } else {
            BigDecimal newQuantity = BigDecimalUtil.sum(targetInventoryDetail.getQuantity(), quantity);
            targetInventoryDetail.setQuantity(newQuantity);

            getEntityManager().merge(targetInventoryDetail);
        }

        sourceInventoryDetail.setQuantity(newSourceInventoryDetailQuantity);
        getEntityManager().merge(sourceInventoryDetail);
        getEntityManager().flush();

        createInventoryDetailLog(sourceInventoryDetail, targetInventoryDetail, description, quantity);
    }

    private void createInventoryDetailLog(InventoryDetail source,
                                          InventoryDetail target,
                                          String description,
                                          BigDecimal quantity) {
        InventoryDetailLog inventoryDetailLog = new InventoryDetailLog();
        inventoryDetailLog.setDate(new Date());
        inventoryDetailLog.setDescription(description);
        inventoryDetailLog.setUserNumber(financesUserService.getFinancesUserCode());
        inventoryDetailLog.setSourceInventoryDetailId(source.getId());
        inventoryDetailLog.setTargetInventoryDetailId(target.getId());
        inventoryDetailLog.setQuantity(quantity);

        getEntityManager().persist(inventoryDetailLog);
        getEntityManager().flush();
    }

    @SuppressWarnings(value = "unchecked")
    private InventoryDetail getInventoryDetail(String companyNumber,
                                               String productItemCode,
                                               String warehouseCode,
                                               BusinessUnit executorUnit,
                                               String costCenterCode) {

        List<InventoryDetail> inventoryDetails = getEntityManager().
                createNamedQuery("InventoryDetail.findByExecutorUnitAndCostCenter").
                setParameter("companyNumber", companyNumber).
                setParameter("productItemCode", productItemCode).
                setParameter("warehouseCode", warehouseCode).
                setParameter("executorUnit", executorUnit).
                setParameter("costCenterCode", costCenterCode).getResultList();

        if (null == inventoryDetails || inventoryDetails.isEmpty()) {
            return null;
        }

        if (inventoryDetails.size() > 1) {
            throw new RuntimeException("Cannot process multiple Inventory details entities.");
        }

        return inventoryDetails.get(0);
    }

    private ProductItem getProductItem(String companyNumber, String productItemCode) {
        return getEntityManager().find(ProductItem.class, new ProductItemPK(companyNumber, productItemCode));
    }
}
