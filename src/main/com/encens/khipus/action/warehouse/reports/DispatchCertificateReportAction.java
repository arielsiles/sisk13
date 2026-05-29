package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.DispatchState;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el "Certificado de Recepcion de Carguio - Orden de Entrega" en PDF
 * a partir de un {@link WarehouseVoucherDispatch}.
 */
@Name("dispatchCertificateReportAction")
@Scope(ScopeType.PAGE)
public class DispatchCertificateReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;

    @Restrict("#{s:hasPermission('WAREHOUSEDISPATCH','VIEW')}")
    public void generateCertificate(WarehouseVoucherDispatch dispatch) {
        log.debug("Generating dispatch certificate for id=" + dispatch.getId());

        // setReportFormat DEBE invocarse antes de addDetailSubReport, porque
        // generateSubReport lee getReportFormat().getFormat() y de lo contrario
        // lanza NullPointerException.
        setReportFormat(ReportFormat.PDF);

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(buildDispatchParams(dispatch));
        params.putAll(buildCompanyParams());
        addDetailSubReport(params, dispatch);

        String fileName = "Despacho_" +
                (dispatch.getDeliveryOrderNumber() != null
                        ? String.format("%06d", dispatch.getDeliveryOrderNumber())
                        : dispatch.getId());

        super.generateReport("dispatchCertificate",
                "/warehouse/reports/dispatchCertificateReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                fileName,
                params);
    }

    private Map<String, Object> buildDispatchParams(WarehouseVoucherDispatch d) {
        Map<String, Object> p = new HashMap<String, Object>();

        p.put("deliveryOrderNumber", d.getDeliveryOrderNumber());
        p.put("dispatchDate", d.getDispatchDate());
        p.put("dispatchTime", d.getLoadingEndTime());
        p.put("salesLotCode", paramAsString(d.getSalesLotCode()));
        p.put("bagCount", d.getBagCount());
        p.put("invoiceNumber", paramAsString(d.getInvoiceNumber()));
        p.put("clientName", d.getClient() != null ? d.getClient().getFullName() : "");
        // clientCode = codigo CLIENTE / TRANSBORDO del DESPACHO (varia por vale).
        // Va impreso en la celda "CLIENTE / TRANSBORDO" del certificado.
        p.put("clientCode", paramAsString(d.getClientTransbordoCode()));

        // Vendedor (JobContract -> Contract -> Employee)
        if (d.getDeliverySeller() != null
                && d.getDeliverySeller().getContract() != null
                && d.getDeliverySeller().getContract().getEmployee() != null) {
            p.put("sellerName", d.getDeliverySeller().getContract().getEmployee().getFullName());
            p.put("sellerCi", paramAsString(d.getDeliverySeller().getContract().getEmployee().getIdNumber()));
        } else {
            p.put("sellerName", "");
            p.put("sellerCi", "");
        }
        if (d.getDeliverySeller() != null
                && d.getDeliverySeller().getJob() != null
                && d.getDeliverySeller().getJob().getCharge() != null) {
            p.put("sellerCharge", paramAsString(d.getDeliverySeller().getJob().getCharge().getName()));
        } else {
            p.put("sellerCharge", "");
        }
        p.put("sellerPhone", "");

        // Transportadora / conductor / vehiculo
        p.put("transportCompany", d.getTransportCompany() != null && d.getTransportCompany().getEntity() != null
                ? d.getTransportCompany().getEntity().getFullName() : "");
        p.put("driverName", d.getDriver() != null ? paramAsString(d.getDriver().getName()) : "");
        p.put("driverLicense", d.getDriver() != null ? paramAsString(d.getDriver().getLicense()) : "");
        p.put("driverPhone", d.getDriver() != null ? paramAsString(d.getDriver().getPhone()) : "");
        p.put("vehiclePlate", d.getVehicle() != null ? paramAsString(d.getVehicle().getPlate()) : "");
        p.put("vehicleBrand", d.getVehicle() != null ? paramAsString(d.getVehicle().getBrand()) : "");
        p.put("vehicleColor", d.getVehicle() != null ? paramAsString(d.getVehicle().getColor()) : "");

        // Carguio
        SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm");
        p.put("loadingStart", d.getLoadingStartTime() != null ? hhmm.format(d.getLoadingStartTime()) : "");
        p.put("loadingEnd", d.getLoadingEndTime() != null ? hhmm.format(d.getLoadingEndTime()) : "");
        p.put("truckDispatchNumber", d.getTruckDispatchNumber());

        // Pesaje
        p.put("weighingTicket", paramAsString(d.getWeighingTicketNumber()));
        p.put("tareWeight", d.getTareWeightKg());
        p.put("grossWeight", d.getGrossWeightKg());
        p.put("netWeight", d.getNetWeightKg());
        BigDecimal netTons = d.getNetWeightKg() != null
                ? BigDecimalUtil.divide(d.getNetWeightKg(), new BigDecimal(1000), 3)
                : BigDecimal.ZERO;
        p.put("netWeightTons", netTons);

        // Bolsas
        p.put("bagsFromNumber", d.getBagsFromNumber());
        p.put("bagsToNumber", d.getBagsToNumber());

        // Lugares y turno
        p.put("originPlace", d.getOriginPlace() != null ? d.getOriginPlace().getDescription() : "");
        p.put("destinationPlace", d.getDestinationPlace() != null ? d.getDestinationPlace().getDescription() : "");
        p.put("productionTurn", d.getProductionTurn() != null ? d.getProductionTurn().getName() : "");

        // Responsable de almacen (firma izquierda)
        if (d.getResponsible() != null) {
            p.put("warehouseResponsible", d.getResponsible().getFullName());
            p.put("warehouseResponsibleCi", paramAsString(d.getResponsible().getIdNumber()));
        } else {
            p.put("warehouseResponsible", "");
            p.put("warehouseResponsibleCi", "");
        }

        // Estado y marca de agua si esta anulado
        p.put("state", d.getState() != null
                ? MessageUtils.getMessage(d.getState().getResourceKey()) : "");
        p.put("annulled", Boolean.valueOf(d.getState() == DispatchState.ANULADO));

        // Lista de productos: un renglon por producto (separados por salto de
        // linea) para la fila "PRODUCTO" de la tabla DATOS DEL DESPACHO.
        StringBuilder sb = new StringBuilder();
        if (d.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail line : d.getDetails()) {
                if (line.getProductItem() != null) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line.getProductItem().getFullName());
                }
            }
        }
        p.put("productListDescription", sb.toString());

        return p;
    }

    private Map<String, Object> buildCompanyParams() {
        Map<String, Object> p = new HashMap<String, Object>();
        try {
            CompanyConfiguration cc = companyConfigurationService.findCompanyConfiguration();
            p.put("companyName", cc != null ? cc.getCompanyName() : "");
            p.put("locationName", cc != null ? cc.getLocationName() : "");
            p.put("systemName", cc != null ? cc.getSystemName() : "");
        } catch (CompanyConfigurationNotFoundException e) {
            p.put("companyName", "");
            p.put("locationName", "");
            p.put("systemName", "");
        }
        return p;
    }

    /**
     * Construye el subreport de productos. Columnas del certificado (COLUMN_1..5):
     *   1 = CANTIDAD (cantidad del detalle convertida a toneladas)
     *   2 = PRODUCTO / SERVICIO (nombre del producto, sin codigo)
     *   3 = DESCRIPCION DETALLADA (observacion de la linea)
     *   4 = NUMERACION BOLSAS (rango de bolsas de la cabecera: "desde al hasta")
     *   5 = TOTAL ENTREGADO (en blanco por ahora; se mantiene el formato)
     */
    private void addDetailSubReport(Map<String, Object> mainReportParams,
                                    WarehouseVoucherDispatch dispatch) {
        Map<String, Object> subReportParams = new HashMap<String, Object>();

        TypedReportData subReportData = super.generateSubReport(
                "DETAILSUBREPORT",
                "/warehouse/reports/dispatchCertificateDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                subReportParams);

        // NUMERACION BOLSAS es dato de cabecera (mismo para todas las lineas).
        String bagRange = "";
        if (dispatch.getBagsFromNumber() != null && dispatch.getBagsToNumber() != null) {
            bagRange = dispatch.getBagsFromNumber() + " al " + dispatch.getBagsToNumber();
        }

        // Datos como JRBeanCollectionDataSource (no via EJBQL).
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        List<WarehouseVoucherDispatchDetail> details = dispatch.getDetails();
        if (details != null) {
            for (WarehouseVoucherDispatchDetail det : details) {
                Map<String, Object> row = new HashMap<String, Object>();
                // CANTIDAD: cantidad del detalle a toneladas (asume unidad base Kg)
                String cantidadTon = "";
                if (det.getQuantity() != null) {
                    BigDecimal tons = BigDecimalUtil.divide(det.getQuantity(), new BigDecimal(1000), 3);
                    cantidadTon = tons.toString() + " Toneladas";
                }
                row.put("COLUMN_1", cantidadTon);
                row.put("COLUMN_2", det.getProductItem() != null ? det.getProductItem().getName() : "");
                row.put("COLUMN_3", paramAsString(det.getObservation()));
                row.put("COLUMN_4", bagRange);
                row.put("COLUMN_5", "");
                rows.add(row);
            }
        }
        JRDataSource ds = new JRBeanCollectionDataSource(rows);

        mainReportParams.put("DETAILSUBREPORT", subReportData.getJasperReport());
        mainReportParams.put("DETAILSUBREPORT_DATASOURCE", ds);
    }

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    private String paramAsString(Object v) {
        return v != null ? v.toString() : "";
    }
}
