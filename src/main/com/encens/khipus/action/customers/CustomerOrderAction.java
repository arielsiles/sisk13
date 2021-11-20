package com.encens.khipus.action.customers;

import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.CancellationReason;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Movement;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.warehouse.InventoryService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.io.IOException;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("customerOrderAction")
@Scope(ScopeType.CONVERSATION)
public class CustomerOrderAction extends GenericAction<CustomerOrder> {

    @In(create = true)
    private BillControllerAction billControllerAction;
    @In
    private SalesAction salesAction;

    @In
    private SaleService saleService;
    @In
    private InventoryService inventoryService;

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

    public void annulOrder(CustomerOrder customerOrder) {
        System.out.println("--->> " +   customerOrder.getCode() + " - " + customerOrder.getClient().getFullName() + " - " +
                                        customerOrder.getTotalAmount() + " - " + cancellationReason.getDescription());

        try {
            billControllerAction.cancelBill(customerOrder, cancellationReason.getCode());
        } catch (IOException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Invoice.messages.errorAnnulOrder");
            return;
        }

        customerOrder.setState(SaleStatus.ANULADO);
        inventoryService.updateInventoryForSalesAnnuled(customerOrder);

        saleService.updateCustomerOrder(customerOrder);
        cleanAnnulOrder();

        //facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Invoice.messages.annnulOk");
    }

    public void executeBilling(List<CustomerOrder> customerOrderList) {

        for (CustomerOrder customerOrder : customerOrderList){
            System.out.println("--->> " +   customerOrder.getCode() + " - " + customerOrder.getClient().getFullName() + " - " + customerOrder.getTotalAmount());

            if (customerOrder.getMovement() == null) {
                Movement movement = salesAction.createInvoice(customerOrder);
                customerOrder.setMovement(movement);
                saleService.updateCustomerOrder(customerOrder);
            }

            try {
                System.out.println("--->>> !hasInvoice ???" +  !billControllerAction.hasInvoice(customerOrder));
                if (!billControllerAction.hasInvoice(customerOrder)){
                    billControllerAction.createBill(customerOrder);
                }
            } catch (IOException e) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Invoice.messages.errorExecuteBilling");
            }
        }
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
