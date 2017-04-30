package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.SalaryMovementProducer;
import com.encens.khipus.service.production.SalaryMovementProducerService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import javax.net.ssl.SSLEngineResult;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("salaryMovementProducerAction")
@Scope(ScopeType.CONVERSATION)
public class SalaryMovementProducerAction extends GenericAction<SalaryMovementProducer> {

    @In
    private SalaryMovementProducerService salaryMovementProducerService;

    private boolean readonly;

    @Factory(value = "salaryMovementProducer", scope = ScopeType.STATELESS)
    public SalaryMovementProducer initSalaryMovementProducer() {
        return getInstance();
    }

    public void selectRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        try {
            rawMaterialProducer = getService().findById(RawMaterialProducer.class, rawMaterialProducer.getId());
            getInstance().setRawMaterialProducer(rawMaterialProducer);
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    @Override
    @End
    public String create() {
        try {
            Double totalCollected = salaryMovementProducerService.getTotalCollectedByProductor(getInstance().getRawMaterialProducer(), getInstance().getDate());
            if(totalCollected < getInstance().getValor())
            {
                addMessgeFailBalance(getInstance().getRawMaterialProducer().getFullName(),totalCollected);
                return Outcome.REDISPLAY;
            }
            getInstance().setProductiveZone(getInstance().getRawMaterialProducer().getProductiveZone());
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    private void addMessgeFailBalance(String fullName,Double totalCollected ) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"SalaryMovementProducer.message.insufficientBalance",fullName,totalCollected);
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearRawMaterialProducer() {
        getInstance().setRawMaterialProducer(null);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isPending() {
        return ProductionCollectionState.PENDING.equals(getInstance().getState());
    }
}
