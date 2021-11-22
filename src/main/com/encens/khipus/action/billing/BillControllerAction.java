package com.encens.khipus.action.billing;

import com.encens.khipus.action.restful.Json;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.customers.Dosage;
import com.encens.khipus.model.customers.Movement;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.rest.*;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.DosageService;
import com.encens.khipus.service.customers.MovementService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Name("billControllerAction")
@Scope(ScopeType.CONVERSATION)
public class BillControllerAction {

    @In(required = false)
    private User currentUser;

    @In
    private UserService userService;

    @In
    private DosageService dosageService;

    @In
    private MovementService movementService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private FacesMessages facesMessages;

    public void testBillController(CustomerOrder customerOrder) throws JsonProcessingException {
        PedidoPOJO pedidoPOJO = createPedidoPojo(customerOrder);
        String jsonPedido = pedidoToJson(pedidoPOJO);
        System.out.println("---------- BILLING ----------");
        System.out.println(jsonPedido);

        //createBill(jsonPedido);
    }

    public void createBill(CustomerOrder customerOrder) throws IOException {
        //if (!hasInvoice(customerOrder)) {
            CompanyConfiguration companyConfiguration = getCompanyConfiguration();
            String url_createbill = companyConfiguration.getCreatebillURL();
            System.out.println("---------- BILLING ----------");
            URL url = new URL(url_createbill);

            PedidoPOJO pedidoPOJO = createPedidoPojo(customerOrder);
            String jsonPedido = pedidoToJson(pedidoPOJO);
            System.out.println(jsonPedido);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = jsonPedido.getBytes("utf-8");
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String responseJsonString = response.toString();
            System.out.println("---------- RESPONSE ----------");

            JsonNode jsonNode = Json.parse(responseJsonString);
            String result = Json.prettyPrint(jsonNode);
            System.out.println(result);

            createResponseObject(responseJsonString);

            BillResponsePOJO billResponsePOJO = Json.fromJson(jsonNode, BillResponsePOJO.class);

            Movement movement = customerOrder.getMovement();
            movement.setCuf(billResponsePOJO.getCuf());
            movement.setFechaSin(billResponsePOJO.getFecha().toString());
            movement.setLeyenda(billResponsePOJO.getLeyenda());
            movement.setDescri(billResponsePOJO.getRespuestaRecepcion().getCodigoDescripcion());
            movement.setCodigoEstado(billResponsePOJO.getRespuestaRecepcion().getCodigoEstado().toString());
            movement.setCodigoRecepcion(billResponsePOJO.getRespuestaRecepcion().getCodigoRecepcion());
            movement.setFactura(billResponsePOJO.getFactura());

            movementService.updateMovement(movement);
        //}
    }

    public void cancelBill(CustomerOrder customerOrder, Integer reasonCode) throws IOException {
        System.out.println("---------- CANCEL BILL ----------");
        if (customerOrder.getMovement() != null){
            CompanyConfiguration companyConfiguration = getCompanyConfiguration();
            URL url = new URL (companyConfiguration.getCancelbillURL());
            CancelBillPOJO cancelBillPOJO = new CancelBillPOJO();
            cancelBillPOJO.setCancellationReasonCode(reasonCode);
            cancelBillPOJO.setCuf(customerOrder.getMovement().getCuf());

            String jsonCancelBill = Json.prettyPrint(Json.toJson(cancelBillPOJO));
            System.out.println(jsonCancelBill);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = jsonCancelBill.getBytes("utf-8");
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String responseJsonString = response.toString();
            System.out.println("---------- RESPONSE CANCEL BILL ----------");

            JsonNode jsonNode = Json.parse(responseJsonString);
            String result = Json.prettyPrint(jsonNode);
            System.out.println(result);

            Movement movement = customerOrder.getMovement();
            movement.setState("A");
            movement.setCodigoEstado(jsonNode.get("codigoEstado").asText());
            movement.setDescri(jsonNode.get("codigoDescripcion").asText());
            movementService.updateMovement(movement);
        }
    }

    public void createResponseObject(String responseJsonString) throws IOException {
        JsonNode nodeResponse = Json.parse(responseJsonString);
        BillResponsePOJO billResponsePOJO = Json.fromJson(nodeResponse, BillResponsePOJO.class);

        System.out.println("---------------OBJETC-------------------");
        System.out.println("numeroFactura: " + billResponsePOJO.getNumeroFactura());
        System.out.println("cuf: " + billResponsePOJO.getCuf());
        System.out.println("direccion: " + billResponsePOJO.getDireccion());
        System.out.println("fecha: " + billResponsePOJO.getFecha());
        System.out.println("leyenda: " + billResponsePOJO.getLeyenda());
        System.out.println("- respuestaRecepcion -");
        ReceptionResponsePOJO receptionResponsePOJO = billResponsePOJO.getRespuestaRecepcion();
        System.out.println("codigoDescripcion: " + receptionResponsePOJO.getCodigoDescripcion());
        System.out.println("codigoEstado: " + receptionResponsePOJO.getCodigoEstado());
        System.out.println("codigoRecepcion: " + receptionResponsePOJO.getCodigoRecepcion());
        System.out.println("transaccion: " + receptionResponsePOJO.getTransaccion());
        System.out.println("mensajesList: " + receptionResponsePOJO.getMensajesList());

    }

    public PedidoPOJO createPedidoPojo(CustomerOrder customerOrder){

        User user = getUser(currentUser.getId()); //
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId());

        PedidoPOJO pedidoPOJO = new PedidoPOJO();
        pedidoPOJO.setNumeroFactura(customerOrder.getMovement().getNumber());
        pedidoPOJO.setNombreRazonSocial(customerOrder.getClient().getBusinessName());

        String clientCode = customerOrder.getClient().getNitNumber();
        if (clientCode == null)
            clientCode = customerOrder.getClient().getNitNumber();

        BigDecimal amountValue = BigDecimalUtil.subtract(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()), BigDecimalUtil.toBigDecimal(customerOrder.getCommissionValue()));

        /** todo parametrizar **/
        pedidoPOJO.setCodigoSucursal(dosage.getBranchOffice().getOfficeCode());
        pedidoPOJO.setCodigoPuntoVenta(dosage.getBranchOffice().getPosCode());

        pedidoPOJO.setCodigoTipoDocumentoIdentidad(customerOrder.getClient().getInvoiceDocumentType().getSinCode());
        pedidoPOJO.setNumeroDocumento(customerOrder.getClient().getNitNumber());
        pedidoPOJO.setCodigoCliente(clientCode);
        pedidoPOJO.setCodigoMetodoPago(customerOrder.getClient().getPaymentMethodSin().getCode());
        pedidoPOJO.setUsuario(user.getUsername());
        pedidoPOJO.setCodigoDocumentoSector(dosage.getSectorDocumentCode());
        pedidoPOJO.setCodigoMoneda(Constants.CODIGO_MONEDA_SIN);
        pedidoPOJO.setTipoCambio(Constants.TIPO_CAMBIO_SIN);
        pedidoPOJO.setMontoTotal(amountValue);
        pedidoPOJO.setMontoTotalSujetoIva(amountValue);
        pedidoPOJO.setMontoTotalMoneda(amountValue);

        List<DetallePedidoPOJO> detallePedidoPOJOList = new ArrayList<DetallePedidoPOJO>();

        for (ArticleOrder articleOrder : customerOrder.getArticleOrderList()){
            DetallePedidoPOJO detalle = new DetallePedidoPOJO();
            detalle.setActividadEconomica(dosage.getEconomicActivityCode());
            //detalle.setCodigoProductoSin(articleOrder.getProductItem().getProductItemCodeSin());
            detalle.setCodigoProductoSin(articleOrder.getProductItem().getProductService().getProductCode().toString());
            detalle.setCodigoProducto(articleOrder.getCodArt());
            detalle.setUnidadMedida(articleOrder.getProductItem().getMeasureUnitSin());
            detalle.setDescripcion(articleOrder.getProductItem().getName());
            detalle.setCantidad(articleOrder.getQuantity());
            detalle.setPrecioUnitario(articleOrder.getPrice());
            detalle.setMontoDescuento(articleOrder.getDiscount());
            detalle.setSubTotal(articleOrder.getAmount()-articleOrder.getDiscount());

            detallePedidoPOJOList.add(detalle);
        }

        pedidoPOJO.setDetalle(detallePedidoPOJOList);
        return pedidoPOJO;
    }

    public String pedidoToJson(PedidoPOJO pedidoPOJO) throws JsonProcessingException {
        String result = "";

        JsonNode node = Json.toJson(pedidoPOJO);
        result = Json.prettyPrint(node);

        return result;
    }

    private User getUser(Long id) {
        try {
            return userService.findById(User.class, id);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    public boolean hasInvoice(CustomerOrder customerOrder){
        boolean result = false;

        if (customerOrder.getMovement() != null){
            if (customerOrder.getMovement().getCuf() != null)
                result = true;
        }
        return result;
    }

    private CompanyConfiguration getCompanyConfiguration(){
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
            return companyConfiguration;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;
            return null;
        }
    }

}
