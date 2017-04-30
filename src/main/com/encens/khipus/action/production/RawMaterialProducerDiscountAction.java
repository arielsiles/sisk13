package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.RawMaterialProducerDiscount;
import com.encens.khipus.service.production.RawMaterialProducerDiscountService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

@Name("rawMaterialProducerDiscountAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialProducerDiscountAction extends GenericAction<RawMaterialProducerDiscount> {

    @In
    private RawMaterialProducerDiscountService rawMaterialProducerDiscountService;

    @Override
    protected GenericService getService() {
        return rawMaterialProducerDiscountService;
    }

    @Factory(value = "rawMaterialProducerDiscount", scope = ScopeType.STATELESS)
    public RawMaterialProducerDiscount initRawMaterialProducerDiscount() {
        return getInstance();
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(RawMaterialProducer rawMaterialProducer) {
        try {
            RawMaterialProducerDiscount discount = rawMaterialProducerDiscountService.prepareDiscount(rawMaterialProducer);
            setOp(OP_UPDATE);
            setInstance(discount);
            return Outcome.SUCCESS;
        } catch (Exception ex) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            return Outcome.REDISPLAY;
        }
    }
}
