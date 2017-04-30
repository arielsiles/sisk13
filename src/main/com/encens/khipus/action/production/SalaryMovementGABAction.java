package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.SalaryMovementGAB;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("salaryMovementGABAction")
@Scope(ScopeType.CONVERSATION)
public class SalaryMovementGABAction extends GenericAction<SalaryMovementGAB> {

    private boolean readonly;

    @Factory(value = "salaryMovementGAB", scope = ScopeType.STATELESS)
    public SalaryMovementGAB initSalaryMovementGAB() {
        return getInstance();
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            getInstance().setProductiveZone(productiveZone);
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearProductiveZone() {
        getInstance().setProductiveZone(null);
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
