package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.fixedassets.PurchaseOrderDetailPart;
import com.encens.khipus.service.fixedassets.PurchaseOrderDetailPartService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.17
 */
@Name("purchaseOrderDetailPartAction")
@Scope(ScopeType.CONVERSATION)
public class PurchaseOrderDetailPartAction extends GenericAction<PurchaseOrderDetailPart> {

    @In
    private FixedAssetPurchaseOrderDetailAction fixedAssetPurchaseOrderDetailAction;

    @In
    private PurchaseOrderDetailPartService purchaseOrderDetailPartService;

    private List<PurchaseOrderDetailPart> instances = new ArrayList<PurchaseOrderDetailPart>();

    private Long currentTabIndex = new Long(1000);

    public List<PurchaseOrderDetailPart> getInstances() {
        return instances;
    }

    public void setInstances(List<PurchaseOrderDetailPart> instances) {
        this.instances = instances;
    }

    public void readInstances() {
        setInstances(purchaseOrderDetailPartService.readParts(fixedAssetPurchaseOrderDetailAction.getInstance()));
    }

    public void add() {
        PurchaseOrderDetailPart newInstance = new PurchaseOrderDetailPart();
        newInstance.setDetail(fixedAssetPurchaseOrderDetailAction.getInstance());

        instances.add(newInstance);
    }

    public void remove(PurchaseOrderDetailPart instance) {
        instances.remove(instance);
    }

    public boolean isEnabledOptions() {
        return fixedAssetPurchaseOrderDetailAction.getInstance().getPurchaseOrder().isPurchaseOrderPending();
    }

    public boolean isEmptyInstanceList() {
        return instances.isEmpty();
    }

    public String validateUnitPrices() {
        if (!instances.isEmpty()) {
            BigDecimal totalUnitPrices = BigDecimal.ZERO;
            for (PurchaseOrderDetailPart instance : instances) {
                totalUnitPrices = BigDecimalUtil.sum(totalUnitPrices, instance.getUnitPrice());
            }

            BigDecimal purchaseOrderDetailTotal = fixedAssetPurchaseOrderDetailAction
                    .getInstance()
                    .getBsUnitPriceValue();

            if (null == purchaseOrderDetailTotal) {
                return Outcome.SUCCESS;
            }

            if (purchaseOrderDetailTotal.compareTo(totalUnitPrices) != 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseOrderDetailPart.error.totalUnitPricesDistinctPurchaseOrderDetailTotal",
                        purchaseOrderDetailTotal);
                return Outcome.FAIL;
            }
        }

        return Outcome.SUCCESS;
    }

    public Long getCurrentTabIndex() {
        return currentTabIndex;
    }

    public void setCurrentTabIndex(Long currentTabIndex) {
        this.currentTabIndex = currentTabIndex;
    }

    public Long getNextIndex() {
        setCurrentTabIndex(getCurrentTabIndex() + 1);
        return getCurrentTabIndex();
    }
}
