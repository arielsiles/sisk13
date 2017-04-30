package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.RawMaterialPayRollException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.RawMaterialPayRecord;
import com.encens.khipus.model.production.RawMaterialPayRecordDetailDummy;
import com.encens.khipus.model.production.RawMaterialProducerDiscount;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import java.util.List;

import static com.encens.khipus.exception.production.RawMaterialPayRollException.*;
import static org.jboss.seam.international.StatusMessage.Severity.ERROR;
import static org.jboss.seam.international.StatusMessage.Severity.WARN;

@Name("rawMaterialPayRecordDetailsAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialPayRecordDetailsAction {

    @In
    private RawMaterialPayRollService rawMaterialPayRollService;

    @In
    private FacesMessages facesMessages;

    @Logger
    private Log log;

    @Out(scope = ScopeType.CONVERSATION)
    private RawMaterialPayRecord rawMaterialPayRecord;

    @Out(scope = ScopeType.CONVERSATION)
    private List<RawMaterialPayRecordDetailDummy> rawMaterialPayRecordDetailDummyList;

    @Begin(join = true)
    public String select(RawMaterialPayRecord rawMaterialPayRecord) {
        try {
            this.rawMaterialPayRecord = rawMaterialPayRollService.findById(RawMaterialPayRecord.class, rawMaterialPayRecord.getId());
            this.rawMaterialPayRecordDetailDummyList = rawMaterialPayRollService.generateDetails(rawMaterialPayRecord);
            return Outcome.SUCCESS;
        } catch (RawMaterialPayRollException ex) {
            print(ex);
            return Outcome.REDISPLAY;
        } catch (EntryNotFoundException e) {
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
            log.error("Caught error", e);
            return Outcome.REDISPLAY;
        }
    }

    private void print(RawMaterialPayRollException ex) {
        if (ex.getCode() == CROSS_WITH_ANOTHER_PAYROLL) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.StartDateCrossWithAnotherPayRoll", ex.getDate());
        } else
        if (ex.getCode() == MINIMUM_START_DATE) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.StartDateMustBe", ex.getDate());
        } else
        if (ex.getCode() == NO_COLLECTION_ON_DATE) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.NoCollectionOnDate", ex.getDate());
        } else {
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    public double getTotalDiscount() {
        RawMaterialProducerDiscount discount = rawMaterialPayRecord.getRawMaterialProducerDiscount();
        return discount.getVeterinary() + discount.getCans() + discount.getCredit() + discount.getYogurt() + discount.getOtherDiscount();
    }

    public double getTotalMoney() {
        double total = 0.0;
        for(RawMaterialPayRecordDetailDummy detail : rawMaterialPayRecordDetailDummyList) {
            total += detail.getGrandTotal();
        }
        return total;
    }

    public double getGrandTotal() {
        RawMaterialProducerDiscount discount = rawMaterialPayRecord.getRawMaterialProducerDiscount();
        return getTotalMoney() - getTotalDiscount() + discount.getOtherIncoming();
    }
}
