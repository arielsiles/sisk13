package com.encens.khipus.action.customers;

import com.encens.khipus.action.billing.BillControllerAction;
import com.encens.khipus.action.billing.SendMessageAction;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.customers.CancellationReason;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Movement;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.VoucherState;
import com.encens.khipus.model.rest.CancelBillResponsePOJO;
import com.encens.khipus.model.rest.ReversionCancelBillResponsePOJO;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.DosageService;
import com.encens.khipus.service.customers.MovementService;
import com.encens.khipus.service.customers.SaleService;
import com.encens.khipus.service.warehouse.InventoryService;
import com.encens.khipus.util.Constants;
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
    @In(create = true)
    private SendMessageAction sendMessageAction;

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

    public void cancelOrderAndInvoice(CustomerOrder customerOrder){

        if ( customerOrder.getMovement() != null ) {
            // Anular Factura
            // Si Fact ANL,   Anular Pedido
            // Si Pedido ANL, Anular Asiento
        } else {
            // Anular Pedido
            // Anular Asiento
        }

    }



    public void cancelOrderInvoice(CustomerOrder customerOrder){

        /** Anular Factura En Linea **/
        if (customerOrder.getMovement() != null){

            verifyInvoiceCancelDates(customerOrder); // Verifica fechas de anulacion (Cambiar fechas en configuracion)
            if (customerOrder.getMovement().getStateDescription().equals("VALIDADA")){

                CancelBillResponsePOJO cancelResponse = null;
                try {
                    cancelResponse = billControllerAction.cancelBill(customerOrder, cancellationReason.getCode());

                } catch (IOException e) {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Invoice.messages.errorAnnulOrder");
                    return;
                }

                if ( cancelResponse != null ) {
                    if (cancelResponse.getCodigoDescripcion().equals("ANULACION CONFIRMADA")) {
                        // Anular Factura
                        Movement movement = customerOrder.getMovement();
                        movement.setState("A");
                        movement.setCodigoEstado(cancelResponse.getCodigoEstado().toString());
                        movement.setStateDescription(cancelResponse.getCodigoDescripcion());
                        movementService.updateMovement(movement);
                        // Anular Pedido
                        customerOrder.setState(SaleStatus.ANULADO);
                        customerOrder.setCancellationReason(cancellationReason);
                        inventoryService.updateInventoryForSalesAnnuled(customerOrder);
                        saleService.updateCustomerOrder(customerOrder);
                        // Anular Asiento
                        if (customerOrder.getVoucher() != null) {
                            customerOrder.getVoucher().setState(VoucherState.ANL.toString());
                            voucherAccoutingService.annulVoucher(customerOrder.getVoucher());
                        }
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "ANULACION CONFIRMADA ...");
                        sendMessageAnnulledInvoice(customerOrder);
                        cleanAnnulOrder();
                    } else {
                        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, cancelResponse.getCodigoDescripcion());
                    }
                } else  {
                    facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FALLA DE CONEXION ...");
                    cleanAnnulOrder();
                }

                /*if (cancelResponse != null) {
                    Movement movement = customerOrder.getMovement();
                    movement.setState("A");
                    movement.setCodigoEstado(cancelResponse.getCodigoEstado().toString());
                    movement.setStateDescription(cancelResponse.getCodigoDescripcion());
                    movementService.updateMovement(movement);

                    sendMessageAnnulledInvoice(customerOrder);
                }*/

            }
        }

    }

    public void verifyInvoiceCancelDates(CustomerOrder customerOrder){
        CompanyConfiguration companyConfiguration = billControllerAction.getCompanyConfiguration();

        Date controlAnnulDate = DateUtils.removeTime(companyConfiguration.getInvoiceAnnulDate());
        Date currentDate = DateUtils.toDay();

        Integer day   = DateUtils.getDay(companyConfiguration.getInvoiceAnnulDate());
        Integer month = DateUtils.getCurrentMonth(DateUtils.addMonth(customerOrder.getOrderDate(), 1));
        Integer year  = DateUtils.getCurrentYear(DateUtils.addMonth(customerOrder.getOrderDate(), 1));
        Date maxAnnulDate = DateUtils.getDate(year, month, day);

        if (currentDate.compareTo(maxAnnulDate) > 0){
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No es posible anular, se encuentra fuera de la fecha valida.");
            return;
        }
    }

    public void reversionCancelBill (CustomerOrder customerOrder){

        ReversionCancelBillResponsePOJO reversionResponse = null;
        try {
            reversionResponse = billControllerAction.reversionCancelBill(customerOrder);
        } catch (IOException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"Invoice.messages.errorReversionCancelBill");
            return;
        }

        if ( reversionResponse != null ){
            if (reversionResponse.getCodigoDescripcion().equals("REVERSION DE ANULACION CONFIRMADA")) {
                /** Movimiento **/
                Movement movement = customerOrder.getMovement();
                movement.setState("V");
                movement.setStateDescription("VALIDADA");
                movement.setCodigoEstado(reversionResponse.getCodigoEstado().toString());

                String gloss =  "codigoDescripcion:" + reversionResponse.getCodigoDescripcion() + "|" +
                                "codigoEstado:" + reversionResponse.getCodigoEstado() + "|" +
                                "codigoRecepcion:" + reversionResponse.getCodigoRecepcion();
                movement.setGloss(gloss);
                movementService.updateMovement(movement);

                /** Revertir pedido, asiento **/
                if (customerOrder.getVoucher() != null) {
                    customerOrder.setState(SaleStatus.CONTABILIZADO);
                    saleService.updateCustomerOrder(customerOrder);

                    customerOrder.getVoucher().setState(VoucherState.APR.toString());
                    voucherAccoutingService.approveVoucher(customerOrder.getVoucher());
                } else {
                    customerOrder.setState(SaleStatus.PENDIENTE);
                    saleService.updateCustomerOrder(customerOrder);
                }

                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "REVERSION DE ANULACION CONFIRMADA ...");
                sendMessageReversionCancelInvoice(customerOrder);

            } else {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, reversionResponse.getCodigoDescripcion());
            }


        } else  {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "FALLA DE CONEXION ...");
            cleanAnnulOrder();
        }



    }

    public void annulOrder0(CustomerOrder customerOrder) {
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
            movement.setStateDescription(cancelResponse.getCodigoDescripcion());
            movementService.updateMovement(movement);

            if (customerOrder.getVoucher() != null) {
                customerOrder.getVoucher().setState(VoucherState.ANL.toString());
                voucherAccoutingService.annulVoucher(customerOrder.getVoucher());
            }

            sendMessageAnnulledInvoice(customerOrder);

            cleanAnnulOrder();
        }

    }

    public void sendMessageAnnulledInvoice(CustomerOrder customerOrder){
        Movement movement = customerOrder.getMovement();
        String text = "La Factura Nro. "  + movement.getNumber() + " de fecha "
                + DateUtils.format(movement.getDate(), "dd/MM/yyyy")
                + " emitida por " + Constants.EMAIL_BUSINESS_NAME + "\n"
                + "con razón social " + movement.getName() + ", NIT/CI " + movement.getNit()
                + " y Código de Autorización " + movement.getCuf() + " ha sido ANULADA.\n"
                + "Motivo de Anulación: " + (customerOrder.getCancellationReason() != null ? customerOrder.getCancellationReason().getDescription() : "No especificado") + "\n"
                + (customerOrder.getObservation() != null ? customerOrder.getObservation() + "\n" : "")
                + "Por favor tomar las consideraciones necesarias.\n\n"
                + "Atte.\n"
                + "COOPERATIVA AGROPECUARIA INTEGRAL DE SERVICIOS COCHABAMBA R.L.\n"
                + "INDUSTRIAS LACTEAS DEL VALLE ALTO - ILVA";

        sendMessageAction.sendEmail(customerOrder, Constants.EMAIL_SUBJECT_ANNULLED, text);
        System.out.println("................" + Constants.EMAIL_SUBJECT_ANNULLED + "................");
    }

    public void sendMessageReversionCancelInvoice(CustomerOrder customerOrder){
        Movement movement = customerOrder.getMovement();
        String text = "La Factura Nro. "  + movement.getNumber() + " de fecha "
                + DateUtils.format(movement.getDate(), "dd/MM/yyyy")
                + " emitida por " + Constants.EMAIL_BUSINESS_NAME + "\n"
                + "con razón social " + movement.getName() + ", NIT/CI " + movement.getNit()
                + " y Código de Autorización " + movement.getCuf() + " ha sido REVERTIDA DE SU ANULACIÓN\n"
                + "Por favor tomar las consideraciones necesarias.\n\n"
                + "Atte.\n"
                + "COOPERATIVA AGROPECUARIA INTEGRAL DE SERVICIOS COCHABAMBA R.L.\n"
                + "INDUSTRIAS LACTEAS DEL VALLE ALTO - ILVA";

        sendMessageAction.sendEmail(customerOrder, Constants.EMAIL_SUBJECT_REVERSION_CANCEL_BILL, text);
        System.out.println("................" + Constants.EMAIL_SUBJECT_REVERSION_CANCEL_BILL + "................");
    }

    public void executeBilling(List<CustomerOrder> customerOrderList) {

        for (CustomerOrder customerOrder : customerOrderList){
            System.out.println("--->> " +   customerOrder.getCode() + " - " + customerOrder.getClient().getFullName() + " - " + customerOrder.getTotalAmount());

            if (customerOrder.getMovement() == null) {
                Movement movement = salesAction.createInvoice(customerOrder); // Se crea la factura (Movimiento) para ser validada
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
