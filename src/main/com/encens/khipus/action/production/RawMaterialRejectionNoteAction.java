package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.RawMaterialRejectionNote;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.Date;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("rawMaterialRejectionNoteAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialRejectionNoteAction extends GenericAction<RawMaterialRejectionNote> {

    @In("extendedGenericService")
    private GenericService extendedGenericService;

    @Override
    protected GenericService getService() {
        return extendedGenericService;
    }

    @Factory(value = "rawMaterialRejectionNote", scope = ScopeType.STATELESS)
    public RawMaterialRejectionNote initRawMaterialRejectionNote() {
        return getInstance();
    }

    @Override
    protected Object getDisplayPropertyValue() {
        RawMaterialRejectionNote rawMaterialRejectionNote = getInstance();
        String rawMaterialProducer = rawMaterialRejectionNote.getRawMaterialProducer().getFullName();
        Date date = rawMaterialRejectionNote.getDate();
        return String.format("%s - %td/%tm/%tY", rawMaterialProducer, date, date, date);
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String startNew() {
        return Outcome.SUCCESS;
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

    public boolean isPending() {
        return ProductionCollectionState.PENDING.equals(getInstance().getState());
    }
}
