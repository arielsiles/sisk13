package com.encens.khipus.action.customers;

import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.CancellationReason;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Movement;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.VoucherState;
import com.encens.khipus.model.rest.CancelBillResponsePOJO;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.DosageService;
import com.encens.khipus.service.customers.MovementService;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.io.IOException;
import java.util.Date;
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
    private UserService userService;
    @In
    private DosageService dosageService;
    @In
    private SaleService saleService;
    @In
    private InventoryService inventoryService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private MovementService movementService;

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

        CompanyConfiguration companyConfiguration = billControllerAction.getCompanyConfiguration();

        Date controlAnnulDate = DateUtils.removeTime(companyConfiguration.getInvoiceAnnulDate());
        Date currentDate = DateUtils.toDay();

        Integer day   = DateUtils.getDay(companyConfiguration.getInvoiceAnnulDate());
        Integer month = DateUtils.getCurrentMonth(DateUtils.addMonth(customerOrder.getOrderDate(), 1));
        Integer year  = DateUtils.getCurrentYear(DateUtils.addMonth(customerOrder.getOrderDate(), 1));
        Date maxAnnulDate = DateUtils.getDate(year, month, day);


        System.out.println("-------------------------------------------------");
        System.out.println("------> Fecha Pedido: " + DateUtils.format(customerOrder.getOrderDate(), "dd/MM/yyyy"));
        System.out.println("------> Fecha Max Anular: " + DateUtils.format(maxAnnulDate, "dd/MM/yyyy"));
        System.out.println("-------------------------------------------------");
        System.out.println("------> Fecha control: " + DateUtils.format(controlAnnulDate, "dd/MM/yyyy"));
        System.out.println("------> Fecha actual: " + DateUtils.format(currentDate, "dd/MM/yyyy"));
        System.out.println("------> Fecha Mes sgte: " + DateUtils.format(DateUtils.addMonth(currentDate, 1), "dd/MM/yyyy"));
        System.out.println("------> Fecha Mes sgte: " + DateUtils.format(DateUtils.addMonth(currentDate, 2), "dd/MM/yyyy"));
        System.out.println("------> Fecha Mes sgte: " + DateUtils.format(DateUtils.addMonth(currentDate, 3), "dd/MM/yyyy"));


        System.out.println("----------------> currentDate.compareTo(maxAnnulDate): " + currentDate.compareTo(maxAnnulDate));
        System.out.println("====> currentDate: " + currentDate);
        System.out.println("====> maxAnnulDate:" + maxAnnulDate);

        if (currentDate.compareTo(maxAnnulDate) > 0){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No es posible anular, se encuentra fuera de la fecha valida.");
            return;
        }


        CancelBillResponsePOJO cancelResponse = null;
        try {
            cancelResponse = billControllerAction.cancelBill(customerOrder, cancellationReason.getCode());
        } catch (IOException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Invoice.messages.errorAnnulOrder");
            return;
        }

        if (cancelResponse != null) {

            customerOrder.setState(SaleStatus.ANULADO);
            inventoryService.updateInventoryForSalesAnnuled(customerOrder);
            saleService.updateCustomerOrder(customerOrder);

            Movement movement = customerOrder.getMovement();
            movement.setState("A");
            movement.setCodigoEstado(cancelResponse.getCodigoEstado().toString());
            movement.setDescri(cancelResponse.getCodigoDescripcion());
            movementService.updateMovement(movement);

            if (customerOrder.getVoucher() != null) {
                customerOrder.getVoucher().setState(VoucherState.ANL.toString());
                voucherAccoutingService.annulVoucher(customerOrder.getVoucher());
            }

            cleanAnnulOrder();
        }
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
                //facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Invoice.messages.errorExecuteBilling");
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Error en facturacion, venta a crédito...");
                return;
            }
        }
    }


    public void printInvoices(List<CustomerOrder> customerOrderList){

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
