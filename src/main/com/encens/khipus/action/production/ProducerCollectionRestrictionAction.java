package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProducerCollectionRestriction;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.service.production.ProducerCollectionRestrictionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.End;
import org.jboss.seam.international.StatusMessage;

@Name("producerCollectionRestrictionAction")
@Scope(ScopeType.CONVERSATION)
public class ProducerCollectionRestrictionAction extends GenericAction<ProducerCollectionRestriction> {

    @In
    private ProducerCollectionRestrictionService producerCollectionRestrictionService;

    @Factory(value = "producerCollectionRestriction", scope = ScopeType.STATELESS)
    public ProducerCollectionRestriction initProducerCollectionRestriction() {
        if (getInstance().getMaxLitersPerDay() == null) {
            getInstance().setMaxLitersPerDay(80.0);
            getInstance().setExcessPriceWeekday(4.0);
            getInstance().setExcessPriceSunday(4.0);
            getInstance().setState("ENABLE");
        }
        return getInstance();
    }

    @End
    @Override
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @End
    @Override
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    private boolean validate() {
        ProducerCollectionRestriction instance = getInstance();
        if (instance.getRawMaterialProducer() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProducerCollectionRestriction.error.producerRequired");
            return false;
        }
        if (instance.getStartDate() == null || instance.getEndDate() == null
                || instance.getStartDate().compareTo(instance.getEndDate()) > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProducerCollectionRestriction.error.invalidDates");
            return false;
        }
        if (!producerCollectionRestrictionService.findOverlapping(
                instance.getRawMaterialProducer(), instance.getStartDate(),
                instance.getEndDate(), instance.getId()).isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "ProducerCollectionRestriction.error.overlap",
                    instance.getRawMaterialProducer().getFullName());
            return false;
        }
        return true;
    }

    public void selectRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        getInstance().setRawMaterialProducer(rawMaterialProducer);
    }

    public void clearRawMaterialProducer() {
        getInstance().setRawMaterialProducer(null);
    }

    public Boolean getState() {
        return getInstance().getState() != null && getInstance().getState().equals("ENABLE");
    }

    public void setState(Boolean state) {
        getInstance().setState(state ? "ENABLE" : "DISABLE");
    }
}
