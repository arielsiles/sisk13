package com.encens.khipus.action.fixedassets;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.fixedassets.FixedAssetPart;
import com.encens.khipus.model.fixedassets.FixedAssetState;
import com.encens.khipus.service.fixedassets.FixedAssetPartService;
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
@Name("fixedAssetPartAction")
@Scope(ScopeType.CONVERSATION)
public class FixedAssetPartAction extends GenericAction<FixedAssetPart> {
    @In
    private FixedAssetPartService fixedAssetPartService;

    @In
    private FixedAssetAction fixedAssetAction;

    private List<FixedAssetPart> instances = new ArrayList<FixedAssetPart>();

    public List<FixedAssetPart> getInstances() {
        return instances;
    }

    public void setInstances(List<FixedAssetPart> instances) {
        this.instances = instances;
    }

    public void readInstances() {
        setInstances(fixedAssetPartService.readFixedAssetParts(fixedAssetAction.getInstance()));
    }

    public void add() {
        FixedAssetPart newInstance = new FixedAssetPart();
        newInstance.setFixedAsset(fixedAssetAction.getInstance());

        instances.add(newInstance);
    }

    public void remove(FixedAssetPart instance) {
        instances.remove(instance);
    }


    public boolean isEnabledOptions() {
        return (!fixedAssetAction.isManaged()
                || (fixedAssetAction.isManaged()
                && FixedAssetState.PEN.equals(fixedAssetAction.getInstance().getState())
                && null == fixedAssetAction.getInstance().getPurchaseOrder()));
    }

    public boolean isEmptyInstanceList() {
        return instances.isEmpty();
    }

    public String validateUnitPrices() {
        if (!instances.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (FixedAssetPart instance : instances) {
                total = BigDecimalUtil.sum(total, instance.getUnitPrice());
            }

            BigDecimal fixedAssetOriginalValue = fixedAssetAction.getInstance().getBsOriginalValue();
            if (null == fixedAssetOriginalValue) {
                fixedAssetOriginalValue = BigDecimal.ZERO;
            }

            if (total.compareTo(fixedAssetOriginalValue) > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "FixedAssetPart.error.unitPricesExceedFixedAssetValue",
                        fixedAssetOriginalValue);
                return Outcome.FAIL;
            }
        }

        return Outcome.SUCCESS;
    }
}
