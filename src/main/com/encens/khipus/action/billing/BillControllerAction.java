package com.encens.khipus.action.billing;

import com.encens.khipus.action.restful.Json;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.rest.*;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.service.customers.DosageService;
import com.encens.khipus.service.customers.MovementService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ServerResponse;
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
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Name("billControllerAction")
@Scope(ScopeType.CONVERSATION)
public class BillControllerAction {

    private CheckBillingModeResponsePOJO checkBillingModeResponse;

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

    public Boolean connectionTest() throws IOException {

        /** TODO Probar **/
        if (!checkBillingMode()) {
            System.out.println(">>>>> FUERA DE LINEA => CONEXION EXITOSA!!!");
            return true;
        }

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String url_ping = companyConfiguration.getConnectionTestURL();

        System.out.println("---->----> URL PING: " + url_ping);

        HttpURLConnection connection = null;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        ServerResponse serverResponse = null;

        try {
            URL url = new URL(url_ping);
            connection = (HttpURLConnection) url.openConnection();

            // Request Setup
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int status = connection.getResponseCode();

            if (status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
                serverResponse = new ServerResponse(Boolean.FALSE, status, responseContent.toString());
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
                serverResponse = new ServerResponse(Boolean.TRUE, status, responseContent.toString());
            }


        } catch (SocketTimeoutException e) {
            System.out.println(".............. EXCEPTION: SocketTimeoutException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println(".............. EXCEPTION: ConnectException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println(".............. EXCEPTION: MalformedURLException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(".............. EXCEPTION: IOException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        System.out.println("-------------- TEST CONNECTION GET -----------");
        System.out.println("--> Connection: " + serverResponse.getConnection());
        System.out.println("--> Response Code: " + serverResponse.getResponseCode());
        System.out.println("--> Response: " + serverResponse.getResponseJson());

        return serverResponse.getConnection();
    }

    public ServerResponse doGetHttpConnection(String urlEndpoint) {

        HttpURLConnection connection = null;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        ServerResponse serverResponse = null;

        try {
            URL url = new URL(urlEndpoint);
            connection = (HttpURLConnection) url.openConnection();

            // Request Setup
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int status = connection.getResponseCode();

            if (status > 299){
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
                serverResponse = new ServerResponse(Boolean.FALSE, status, responseContent.toString());
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
                serverResponse = new ServerResponse(Boolean.TRUE, status, responseContent.toString());
            }


        } catch (SocketTimeoutException e) {
            System.out.println(".............. EXCEPTION: SocketTimeoutException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println(".............. EXCEPTION: ConnectException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println(".............. EXCEPTION: MalformedURLException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(".............. EXCEPTION: IOException");
            serverResponse = new ServerResponse(Boolean.FALSE, null, "");
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        System.out.println("-------------- TEST CONNECTION GET -----------");
        System.out.println("--> Connection: " + serverResponse.getConnection());
        System.out.println("--> Response Code: " + serverResponse.getResponseCode());
        System.out.println("--> Response: " + serverResponse.getResponseJson());

        return serverResponse;
    }

    /**
     *
     * @param urlEndpoint
     * @param jsonParam
     * @return
     */
    public ServerResponse doPostHttpConnection(String urlEndpoint, String jsonParam) {

        ServerResponse serverResponse = null;

        try {
            URL url = new URL(urlEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(7000);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = jsonParam.getBytes("utf-8");
            os.write(input, 0, input.length);

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println("======> Response Post: " + response.toString());
            serverResponse = new ServerResponse(Boolean.TRUE, con.getResponseCode(), response.toString());
            System.out.println("....------> doPostHttpConnection: serverResponse: " + serverResponse);

        } catch (IOException e){
            System.out.println("---------------> Exception doPostHttpConnection: -1, Json:null" );
            return new ServerResponse(Boolean.FALSE, -1, null);
        }


        return serverResponse;
    }

    public void createBill(CustomerOrder customerOrder) throws IOException {

        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        String url_createbill = companyConfiguration.getCreatebillURL();
        System.out.println("---------- BILLING ----------");

        PedidoPOJO pedidoPOJO = createPedidoPojo(customerOrder);
        String jsonPedido = pedidoToJson(pedidoPOJO);
        System.out.println(jsonPedido);

        if (connectionTest()) {
            System.out.println(">>>>> CONEXION EXITOSA!!!");
            ServerResponse serverResponse = doPostHttpConnection(url_createbill, jsonPedido);
            System.out.println("---------- SERVER RESPONSE ----------");
            System.out.println("--> Connection: " + serverResponse.getConnection());
            System.out.println("--> Response Code: " + serverResponse.getResponseCode());
            System.out.println("--> Response Json: " + serverResponse.getResponseJson());

            if (serverResponse.getResponseCode() == -1){
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Facturacion SIN CONEXION, -1");
                return;
            }

            JsonNode jsonNode = Json.parse(serverResponse.getResponseJson()); /** **/
            String result = Json.prettyPrint(jsonNode);
            System.out.println(result);

            /** NULL cuando la facturacion es offline **/
            printResponseObject(serverResponse.getResponseJson());

            BillResponsePOJO billResponsePOJO = Json.fromJson(jsonNode, BillResponsePOJO.class);

            Movement movement = customerOrder.getMovement();
            movement.setCuf(billResponsePOJO.getCuf());
            movement.setFechaSin(billResponsePOJO.getFecha().toString());
            movement.setLeyenda(billResponsePOJO.getLeyenda());

            if (billResponsePOJO.getRespuestaRecepcion() != null) {
                movement.setDescri(billResponsePOJO.getRespuestaRecepcion().getCodigoDescripcion());
                movement.setCodigoEstado(billResponsePOJO.getRespuestaRecepcion().getCodigoEstado().toString());
                movement.setCodigoRecepcion(billResponsePOJO.getRespuestaRecepcion().getCodigoRecepcion());
            }

            movement.setFactura(billResponsePOJO.getFactura());
            movement.setEmissionType(billResponsePOJO.getTipo());

            movementService.updateMovement(movement);

        } else {
            System.out.println(">>>>> SIN CONEXION!!!");
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Facturacion SIN CONEXION...");
        }


    }

    public void cancelBill(CustomerOrder customerOrder, Integer reasonCode) throws IOException {
        System.out.println("---------- CANCEL BILL ----------");
        User user = getUser(currentUser.getId()); //
        Dosage dosage = dosageService.findDosageByOffice(user.getBranchOffice().getId());

        if (customerOrder.getMovement() != null){
            CompanyConfiguration companyConfiguration = getCompanyConfiguration();

            CancelBillPOJO cancelBillPOJO = new CancelBillPOJO(
                    dosage.getBranchOffice().getOfficeCode(),
                    dosage.getBranchOffice().getPosCode(),reasonCode,
                    customerOrder.getMovement().getCuf());

            String jsonCancelBill = Json.prettyPrint(Json.toJson(cancelBillPOJO));
            System.out.println(jsonCancelBill);

            if (connectionTest()) {
                System.out.println(">>>>> CONEXION EXITOSA!!!");
                ServerResponse serverResponse = doPostHttpConnection(companyConfiguration.getCancelbillURL(), jsonCancelBill);
                System.out.println("---------- RESPONSE CANCEL BILL ----------");
                //JsonNode jsonNode = Json.parse(responseJsonString);
                JsonNode jsonNode = Json.parse(serverResponse.getResponseJson());
                String result = Json.prettyPrint(jsonNode);
                System.out.println(result);

                Movement movement = customerOrder.getMovement();
                movement.setState("A");
                movement.setCodigoEstado(jsonNode.get("codigoEstado").asText());
                movement.setDescri(jsonNode.get("codigoDescripcion").asText());
                movementService.updateMovement(movement);
            } else {
                System.out.println(">>>>> SIN CONEXION!!!");
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"No es posible anular por el momento...");
            }
        }
    }


    public String nitVerification(Long nit) throws IOException{

        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        NitVerificationPOJO nitVerificationPOJO = new NitVerificationPOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode(), nit);
        String jsonString = Json.prettyPrint(Json.toJson(nitVerificationPOJO));
        System.out.println("-----------------------CheckBillingModePOJO-------------------");
        System.out.println("----------> URL: " + companyConfiguration.getNitVerificationURL());
        System.out.println(jsonString);

        ServerResponse serverResponse = doPostHttpConnection(companyConfiguration.getNitVerificationURL(), jsonString);
        if (serverResponse.getResponseJson() != null) {
            JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
            NitVerificationResponsePOJO responsePOJO = Json.fromJson(jsonNodeResponse, NitVerificationResponsePOJO.class);

            System.out.println("---====> Mensaje: " + responsePOJO.getMensaje());
            System.out.println("---====> NIT: " + responsePOJO.getNitParaVerificacion());
            return responsePOJO.getMensaje();
        }else {
            return "No se pudo validar!";
        }
    }

    /**
     *
     * @return true en linea, false fuera de linea
     * @throws IOException
     */
    public Boolean checkBillingMode() throws IOException{

        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        CheckBillingModePOJO checkBillingModePOJO = new CheckBillingModePOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode());
        String jsonString = Json.prettyPrint(Json.toJson(checkBillingModePOJO));
        System.out.println("-----------------------CheckBillingModePOJO-------------------");
        System.out.println("----------> URL: " + companyConfiguration.getCheckBillingModeURL());
        System.out.println(jsonString);

        ServerResponse serverResponse = doPostHttpConnection(companyConfiguration.getCheckBillingModeURL() , jsonString);

        JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
        String result = Json.prettyPrint(jsonNodeResponse);
        System.out.println(result);

        CheckBillingModeResponsePOJO responsePOJO = Json.fromJson(jsonNodeResponse, CheckBillingModeResponsePOJO.class);
        this.checkBillingModeResponse = responsePOJO; // ***

        System.out.println("----> Response isOnline: " + responsePOJO.getIsOnline());

        if (responsePOJO.getIsOnline()) {

            System.out.println("---> ok true");
        }
        else {
            System.out.println("----> Fail true");
        }

        return responsePOJO.getIsOnline();
    }


    public void changeToOnlineBillingMode() throws IOException {
        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        SetOnlineModePOJO setOnlineModePOJO = new SetOnlineModePOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode());
        String jsonString = Json.prettyPrint(Json.toJson(setOnlineModePOJO));
        System.out.println("-----------------------changeToOnlineBillingMode-------------------");
        System.out.println("----------> URL: " + companyConfiguration.getOnlineModeURL());
        System.out.println(jsonString);

        ServerResponse serverResponse = doPostHttpConnection(companyConfiguration.getOnlineModeURL() , jsonString);

        if (serverResponse.getResponseJson() != null) {
            JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
            String result = Json.prettyPrint(jsonNodeResponse);
            System.out.println(result);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"MODO DE FACTURACION: FUERA DE LINEA!!!");
        }else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No es posible en este momento, intentelo mas tarde.");
        }
    }

    public void changeToOfflineBillingMode(SignificantEventSIN significantEventSIN, String cafcCode) throws IOException {
        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        SetOfflineModePOJO setOfflineModePOJO = new SetOfflineModePOJO(user.getBranchOffice().getOfficeCode(),user.getBranchOffice().getPosCode(), cafcCode, significantEventSIN.getCode());
        String jsonString = Json.prettyPrint(Json.toJson(setOfflineModePOJO));
        System.out.println("-----------------------changeToOfflineBillingMode-------------------");
        System.out.println("----------> URL: " + companyConfiguration.getOfflineModeURL());
        System.out.println(jsonString);

        ServerResponse serverResponse = doPostHttpConnection(companyConfiguration.getOfflineModeURL() , jsonString);

        if (serverResponse.getResponseJson() != null) {
            JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
            String result = Json.prettyPrint(jsonNodeResponse);
            System.out.println(result);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"MODO DE FACTURACION: EN LINEA!!!");
        }else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"No es posible en este momento, intentelo mas tarde.");
        }
    }

    public void prepareOfflineBillPackages() throws IOException {
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        ServerResponse serverResponse = doGetHttpConnection(companyConfiguration.getPrepareOfflineBillPackagesURL());

        System.out.println("-----------------------GET RESPONSE prepareOfflineBillPackages-------------------");
        JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
        String result = Json.prettyPrint(jsonNodeResponse);
        System.out.println(result);
    }

    public void processOfflineBillPackages() throws IOException {
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        ServerResponse serverResponse = doGetHttpConnection(companyConfiguration.getProcessOfflineBillPackagesURL());

        System.out.println("-----------------------GET RESPONSE processOfflineBillPackages-------------------");
        JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
        String result = Json.prettyPrint(jsonNodeResponse);
        System.out.println(result);
    }

    public void validateOfflineBillPackages() throws IOException {
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();
        ServerResponse serverResponse = doGetHttpConnection(companyConfiguration.getValidateOfflineBillPackagesURL());

        System.out.println("-----------------------GET RESPONSE validateOfflineBillPackages-------------------");
        JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
        String result = Json.prettyPrint(jsonNodeResponse);
        System.out.println(result);
    }

    // No usado
    public List<SignificantEventCodePOJO> querySignificantEvents() throws IOException {
        User user = getUser(currentUser.getId());
        CompanyConfiguration companyConfiguration = getCompanyConfiguration();

        SignificantEventPOJO significantEventPOJO = new SignificantEventPOJO(user.getBranchOffice().getOfficeCode(), user.getBranchOffice().getPosCode());
        String jsonString = Json.prettyPrint(Json.toJson(significantEventPOJO));
        ServerResponse serverResponse = doPostHttpConnection(companyConfiguration.getSignificantEventURL(), jsonString);

        JsonNode jsonNodeResponse = Json.parse(serverResponse.getResponseJson());
        String result = Json.prettyPrint(jsonNodeResponse);
        System.out.println("-----------------querySignificantEvents------------");
        System.out.println(result);

        SignificantEventResponsePOJO objectResponse = Json.fromJson(jsonNodeResponse, SignificantEventResponsePOJO.class);
        System.out.println("-----------------SignificantEvents Object------------");
        System.out.println("----> transaccion: " + objectResponse.getTransaccion());
        System.out.println("----> listaCodigos: " + objectResponse.getListaCodigos().size());

        return objectResponse.getListaCodigos();
    }

    public void printResponseObject(String responseJsonString) throws IOException {
        JsonNode nodeResponse = Json.parse(responseJsonString);
        BillResponsePOJO billResponsePOJO = Json.fromJson(nodeResponse, BillResponsePOJO.class);

        System.out.println("---------------OBJETC-------------------");
        System.out.println("numeroFactura: " + billResponsePOJO.getNumeroFactura());
        System.out.println("cuf: " + billResponsePOJO.getCuf());
        System.out.println("direccion: " + billResponsePOJO.getDireccion());
        System.out.println("fecha: " + billResponsePOJO.getFecha());
        System.out.println("leyenda: " + billResponsePOJO.getLeyenda());

        System.out.println("-------> respuestaRecepcion <-------");
        ReceptionResponsePOJO receptionResponsePOJO = billResponsePOJO.getRespuestaRecepcion();
        System.out.println("===> " + billResponsePOJO.getRespuestaRecepcion());
        if (billResponsePOJO.getRespuestaRecepcion() != null) {

            System.out.println("codigoDescripcion: " + receptionResponsePOJO.getCodigoDescripcion());
            System.out.println("codigoEstado: " + receptionResponsePOJO.getCodigoEstado());
            System.out.println("codigoRecepcion: " + receptionResponsePOJO.getCodigoRecepcion());
            System.out.println("transaccion: " + receptionResponsePOJO.getTransaccion());
            System.out.println("mensajesList: " + receptionResponsePOJO.getMensajesList());
        }

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

        BigDecimal totalDiscount = BigDecimalUtil.sum(customerOrder.getProductDiscountValue(), customerOrder.getAdditionalDiscountValue());
        BigDecimal amountValue = BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount());

        /** todo parametrizar **/
        pedidoPOJO.setCodigoSucursal(dosage.getBranchOffice().getOfficeCode());
        pedidoPOJO.setCodigoPuntoVenta(dosage.getBranchOffice().getPosCode());

        System.out.println("----------> getClient: " + customerOrder.getClient().getFullName());
        System.out.println("----------> getInvoiceDocumentType.getSinCode:" + customerOrder.getClient().getInvoiceDocumentType().getSinCode());

        pedidoPOJO.setCodigoTipoDocumentoIdentidad(customerOrder.getClient().getInvoiceDocumentType().getSinCode());
        pedidoPOJO.setNumeroDocumento(customerOrder.getClient().getNitNumber());
        pedidoPOJO.setCodigoCliente(clientCode);
        pedidoPOJO.setCodigoMetodoPago(customerOrder.getClient().getPaymentMethodSin().getCode());
        pedidoPOJO.setUsuario(user.getUsername());
        pedidoPOJO.setCodigoDocumentoSector(dosage.getSectorDocumentCode());
        pedidoPOJO.setCodigoMoneda(Constants.CODIGO_MONEDA_SIN); /** todo **/
        pedidoPOJO.setTipoCambio(Constants.TIPO_CAMBIO_SIN); /** todo **/
        pedidoPOJO.setMontoTotal(amountValue);
        pedidoPOJO.setMontoTotalSujetoIva(amountValue);
        pedidoPOJO.setMontoTotalMoneda(amountValue);
        pedidoPOJO.setDescuentoAdicional(customerOrder.getAdditionalDiscountValue());

        List<DetallePedidoPOJO> detallePedidoPOJOList = new ArrayList<DetallePedidoPOJO>();

        for (ArticleOrder articleOrder : customerOrder.getArticleOrderList()){
            DetallePedidoPOJO detalle = new DetallePedidoPOJO();
            //detalle.setActividadEconomica(dosage.getEconomicActivityCode());
            detalle.setActividadEconomica(articleOrder.getProductItem().getProductService().getActivityCode());

            //detalle.setCodigoProductoSin(articleOrder.getProductItem().getProductItemCodeSin());
            detalle.setCodigoProductoSin(articleOrder.getProductItem().getProductService().getProductCode().toString());

            detalle.setCodigoProducto(articleOrder.getCodArt());
            detalle.setUnidadMedida(articleOrder.getProductItem().getMeasureUnit().getNumber());
            detalle.setDescripcion(articleOrder.getProductItem().getName());
            detalle.setCantidad(articleOrder.getQuantity());
            detalle.setPrecioUnitario(articleOrder.getPrice());
            detalle.setMontoDescuento(articleOrder.getDiscount());
            detalle.setSubTotal(articleOrder.getAmount());

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
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");
            return null;
        }
    }

    public CheckBillingModeResponsePOJO getCheckBillingModeResponse() {
        return checkBillingModeResponse;
    }

    public void setCheckBillingModeResponse(CheckBillingModeResponsePOJO checkBillingModeResponse) {
        this.checkBillingModeResponse = checkBillingModeResponse;
    }
}
