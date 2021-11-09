package com.encens.khipus.action.billing;

import com.encens.khipus.action.restful.Json;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.rest.DetallePedidoPOJO;
import com.encens.khipus.model.rest.PedidoPOJO;
import com.encens.khipus.util.BigDecimalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.List;

@Name("billControllerAction")
@Scope(ScopeType.CONVERSATION)
public class BillControllerAction {


    public void testBillController(CustomerOrder customerOrder) throws JsonProcessingException {
        PedidoPOJO pedidoPOJO = createPedidoPojo(customerOrder);
        System.out.println(pedidoToJson(pedidoPOJO));
    }

    public PedidoPOJO createPedidoPojo(CustomerOrder customerOrder){

        PedidoPOJO pedidoPOJO = new PedidoPOJO();
        pedidoPOJO.setNombreRazonSocial(customerOrder.getClient().getBusinessName());
        pedidoPOJO.setCodigoTipoDocumentoIdentidad(5);
        pedidoPOJO.setNumeroDocumento(customerOrder.getClient().getNitNumber());
        pedidoPOJO.setCodigoCliente(customerOrder.getClient().getIdNumber());
        pedidoPOJO.setCodigoMetodoPago(1);
        pedidoPOJO.setUsuario("admin");
        pedidoPOJO.setCodigoDocumentoSector(1);
        pedidoPOJO.setCodigoMoneda(1);
        pedidoPOJO.setTipoCambio(1);
        pedidoPOJO.setMontoTotal(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()));
        pedidoPOJO.setMontoTotalSujetoIva(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()));
        pedidoPOJO.setMontoTotalMoneda(BigDecimalUtil.toBigDecimal(customerOrder.getTotalAmount()));

        List<DetallePedidoPOJO> detallePedidoPOJOList = new ArrayList<DetallePedidoPOJO>();

        for (ArticleOrder articleOrder : customerOrder.getArticleOrderList()){
            DetallePedidoPOJO detalle = new DetallePedidoPOJO();
            detalle.setActividadEconomica("105000"); /** todo **/
            detalle.setCodigoProductoSin(articleOrder.getCodArt()); /** todo **/
            detalle.setCodigoProducto(articleOrder.getCodArt());
            detalle.setUnidadMedida(57);
            detalle.setDescripcion(articleOrder.getProductItem().getName());
            detalle.setCantidad(articleOrder.getQuantity());
            detalle.setPrecioUnitario(articleOrder.getPrice());
            detalle.setMontoDescuento(0.0);
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
        System.out.println(result);

        return result;
    }

}
