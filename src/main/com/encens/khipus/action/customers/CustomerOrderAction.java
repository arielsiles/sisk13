package com.encens.khipus.action.customers;

import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.CancellationReason;
import com.encens.khipus.model.customers.CustomerOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.io.IOException;

/**
 * @author
 * @version 2.2
 */

@Name("customerOrderAction")
@Scope(ScopeType.CONVERSATION)
public class CustomerOrderAction extends GenericAction<CustomerOrder> {

    @In(create = true)
    private BillControllerAction billControllerAction;

    private CancellationReason cancellationReason;
    private String observationCancel;

    @Factory(value = "customerOrder", scope = ScopeType.STATELESS)
    public CustomerOrder initClient() {
        return getInstance();
    }

    @Override
    @Begin(join=true, ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String select(CustomerOrder instance) {
        String outCome = super.select(instance);
        return outCome;
    }

    public void annulOrder(CustomerOrder customerOrder) throws IOException {
        System.out.println("--->> " +   customerOrder.getCode() + " - " +
                                        customerOrder.getClient().getFullName() + " - " +
                                        customerOrder.getTotalAmount() + " - " +
                                        cancellationReason.getDescription());

        billControllerAction.cancelBill(customerOrder, cancellationReason.getCode());
        cleanAnnulOrder();
    }

    public void cleanAnnulOrder(){
        setCancellationReason(null);
        setObservationCancel(null);
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getObservationCancel() {
        return observationCancel;
    }

    public void setObservationCancel(String observationCancel) {
        this.observationCancel = observationCancel;
    }
}
