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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Genera la "Nota de Remision - Detalle de Transbordo" en PDF a partir de un
 * {@link WarehouseVoucherDispatch}. Reutiliza el subreporte de detalle del
 * Certificado para la tabla de productos.
 */
@Name("dispatchRemisionNoteReportAction")
@Scope(ScopeType.PAGE)
public class DispatchRemisionNoteReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;

    @Restrict("#{s:hasPermission('WAREHOUSEDISPATCH','VIEW')}")
    public void generateRemisionNote(WarehouseVoucherDispatch dispatch) {
        log.debug("Generating Nota de Remision for id=" + dispatch.getId());

        // setReportFormat debe llamarse antes de addDetailSubReport (lee
        // getReportFormat().getFormat() y de lo contrario NPE).
        setReportFormat(ReportFormat.PDF);

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(buildDispatchParams(dispatch));
        params.putAll(buildCompanyParams());
        params.put("warehouseSignatureBlock",
                buildSignatureBlock(dispatch, (String) params.get("companyName")));
        addDetailSubReport(params, dispatch);
        addWeightSubReport(params, dispatch);

        String fileName = "NotaRemision_" +
                (dispatch.getDeliveryOrderNumber() != null
                        ? String.format("%06d", dispatch.getDeliveryOrderNumber())
                        : dispatch.getId());

        super.generateReport("dispatchRemisionNote",
                "/warehouse/reports/dispatchRemisionNoteReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                fileName,
                params);
    }

    private Map<String, Object> buildDispatchParams(WarehouseVoucherDispatch d) {
        Map<String, Object> p = new HashMap<String, Object>();

        p.put("deliveryOrderNumber", d.getDeliveryOrderNumber());
        p.put("dispatchDate", d.getDispatchDate());
        p.put("salesLotCode", paramAsString(d.getSalesLotCode()));
        p.put("truckDispatchNumber", d.getTruckDispatchNumber());

        // Conductor / Vehiculo
        p.put("driverName",   d.getDriver() != null  ? paramAsString(d.getDriver().getName())    : "");
        p.put("driverLicense",d.getDriver() != null  ? paramAsString(d.getDriver().getLicense()) : "");
        p.put("driverPhone",  d.getDriver() != null  ? paramAsString(d.getDriver().getPhone())   : "");
        p.put("vehiclePlate", d.getVehicle() != null ? paramAsString(d.getVehicle().getPlate())  : "");
        p.put("vehicleBrand", d.getVehicle() != null ? paramAsString(d.getVehicle().getBrand())  : "");
        p.put("vehicleColor", d.getVehicle() != null ? paramAsString(d.getVehicle().getColor())  : "");

        // Transportadora: solo razon social (acronym), sin el NIT.
        p.put("transportCompany",
                d.getTransportCompany() != null && d.getTransportCompany().getEntity() != null
                        ? paramAsString(d.getTransportCompany().getEntity().getAcronym()) : "");

        // DETALLE EN PESO
        // CANTIDAD izq: "{bagCount} Bolsas {nombreEnvase1}" (envase del primer
        //   detalle con tipo definido; si no hay envase, solo "{bagCount} Bolsas").
        String firstPackagingName = "";
        if (d.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail line : d.getDetails()) {
                if (line.getPackaging() != null && line.getPackaging().getName() != null) {
                    firstPackagingName = line.getPackaging().getName();
                    break;
                }
            }
        }
        Integer bagCount = d.getBagCount();
        String bagCountText = (bagCount != null ? bagCount : 0) + " Bolsas"
                + (firstPackagingName.isEmpty() ? "" : " " + firstPackagingName);
        p.put("bagCountText", bagCountText);

        // CANTIDAD der: peso neto formateado en "es" (28.000,00 Kg. En Peso Neto)
        BigDecimal netKg = d.getNetWeightKg();
        DecimalFormat df = new DecimalFormat("#,##0.00",
                new DecimalFormatSymbols(new Locale("es", "ES")));
        String netWeightText = (netKg != null ? df.format(netKg) : "") + " Kg. En Peso Neto";
        p.put("netWeightText", netWeightText);

        // PRODUCTO (DETALLE EN PESO): lista de productos en una linea con coma.
        StringBuilder sb = new StringBuilder();
        if (d.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail line : d.getDetails()) {
                if (line.getProductItem() != null) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(line.getProductItem().getName());
                }
            }
        }
        p.put("productListDescription", sb.toString());

        // OBSERVACION (texto fijo configurable via messages_app.properties).
        p.put("observacionBruto",
                paramAsString(MessageUtils.getMessage("NotaRemision.observacionBruto")));

        // Estado del despacho (para el pageFooter). Antes faltaba y salia
        // "Estado: " vacio.
        p.put("state", d.getState() != null
                ? paramAsString(MessageUtils.getMessage(d.getState().getResourceKey()))
                : "");
        p.put("annulled", Boolean.valueOf(d.getState() == DispatchState.ANULADO));

        return p;
    }

    private Map<String, Object> buildCompanyParams() {
        Map<String, Object> p = new HashMap<String, Object>();
        try {
            CompanyConfiguration cc = companyConfigurationService.findCompanyConfiguration();
            p.put("companyName", cc != null ? cc.getCompanyName() : "");
        } catch (CompanyConfigurationNotFoundException e) {
            p.put("companyName", "");
        }
        return p;
    }

    /**
     * Reutiliza el subreporte de productos del Certificado (mismas columnas y
     * filas COLUMN_1..COLUMN_5). Asi mantenemos un solo subreporte y formato
     * uniforme entre los dos reportes.
     */
    private void addDetailSubReport(Map<String, Object> mainReportParams,
                                    WarehouseVoucherDispatch dispatch) {
        TypedReportData subReportData = super.generateSubReport(
                "DETAILSUBREPORT",
                "/warehouse/reports/dispatchCertificateDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                new HashMap<String, Object>());

        // NUMERACION BOLSAS: dato de cabecera (mismo para todas las lineas).
        String bagRange = "";
        if (dispatch.getBagsFromNumber() != null && dispatch.getBagsToNumber() != null) {
            bagRange = dispatch.getBagsFromNumber() + " al " + dispatch.getBagsToNumber();
        }

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        List<WarehouseVoucherDispatchDetail> details = dispatch.getDetails();
        if (details != null) {
            for (WarehouseVoucherDispatchDetail det : details) {
                Map<String, Object> row = new HashMap<String, Object>();
                String cantidadTon = "";
                if (det.getQuantity() != null) {
                    BigDecimal tons = BigDecimalUtil.divide(det.getQuantity(), new BigDecimal(1000), 3);
                    cantidadTon = tons.stripTrailingZeros().toPlainString() + " Toneladas";
                }
                row.put("COLUMN_1", cantidadTon);
                row.put("COLUMN_2", det.getProductItem() != null ? det.getProductItem().getName() : "");
                row.put("COLUMN_3", buildDescripcionDetallada(det));
                row.put("COLUMN_4", bagRange);
                row.put("COLUMN_5", buildTotalDelivered(det));
                rows.add(row);
            }
        }
        JRDataSource ds = new JRBeanCollectionDataSource(rows);

        mainReportParams.put("DETAILSUBREPORT", subReportData.getJasperReport());
        mainReportParams.put("DETAILSUBREPORT_DATASOURCE", ds);
    }

    /**
     * Bloque de firma de "DESPACHADO POR" como string multi-linea con
     * markup="styled" (negrillas via &lt;b&gt;...&lt;/b&gt;). Las lineas se
     * omiten si su dato no esta cargado (asi el celular vacio no deja una
     * linea en blanco entre el rol y la empresa). Orden:
     *   nombre / C.I. / RESP. DE ALMACEN Y COMPRAS / celular / EMPRESA
     */
    private String buildSignatureBlock(WarehouseVoucherDispatch d, String companyName) {
        String name = "", cellphone = "", ci = "";
        if (d.getResponsible() != null) {
            name = paramAsString(d.getResponsible().getFullName());
            cellphone = paramAsString(d.getResponsible().getCellphone());
            ci = paramAsString(d.getResponsible().getIdNumber());
        }
        StringBuilder sig = new StringBuilder();
        if (name.length() > 0) {
            sig.append(name);
        }
        if (ci.length() > 0) {
            if (sig.length() > 0) sig.append("\n");
            sig.append("C.I.: ").append(ci);
        }
        if (sig.length() > 0) sig.append("\n");
        sig.append("<b>RESP. DE ALMACEN Y COMPRAS</b>");
        if (cellphone.length() > 0) {
            sig.append("\n").append(cellphone);
        }
        if (companyName != null && companyName.length() > 0) {
            sig.append("\n<b>").append(companyName).append("</b>");
        }
        return sig.toString();
    }

    /**
     * Subreporte de la tabla DETALLE EN PESO: una fila por linea de detalle
     * del despacho. Columnas:
     *   COLUMN_1 = "{bagsCount} Bolsas {nombreEnvase}"  (linea de detalle)
     *   COLUMN_2 = "{peso en Kg formateado es-ES} Kg. En Peso Neto"
     *   COLUMN_3 = nombre del producto
     *   COLUMN_4 = observacion (texto fijo configurable)
     */
    private void addWeightSubReport(Map<String, Object> mainReportParams,
                                    WarehouseVoucherDispatch dispatch) {
        TypedReportData subReportData = super.generateSubReport(
                "WEIGHT_SUBREPORT",
                "/warehouse/reports/dispatchRemisionNoteWeightSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                new HashMap<String, Object>());

        DecimalFormat df = new DecimalFormat("#,##0.00",
                new DecimalFormatSymbols(new Locale("es", "ES")));

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        List<WarehouseVoucherDispatchDetail> details = dispatch.getDetails();
        if (details != null) {
            for (WarehouseVoucherDispatchDetail det : details) {
                Map<String, Object> row = new HashMap<String, Object>();

                // CANTIDAD izq: "{bagsCount} Bolsas {nombreEnvase}"
                String packagingName = (det.getPackaging() != null
                        && det.getPackaging().getName() != null)
                        ? det.getPackaging().getName() : "";
                Integer bags = det.getBagsCount();
                String cantidad = (bags != null ? bags : 0) + " Bolsas"
                        + (packagingName.isEmpty() ? "" : " " + packagingName);
                row.put("COLUMN_1", cantidad);

                // CANTIDAD der: peso de la linea formateado + sufijo
                String peso = "";
                if (det.getQuantity() != null) {
                    peso = df.format(det.getQuantity()) + " Kg. En Peso Neto";
                }
                row.put("COLUMN_2", peso);

                // PRODUCTO: nombre del producto de la linea
                row.put("COLUMN_3", det.getProductItem() != null
                        ? det.getProductItem().getName() : "");

                // OBSERVACION: arma "Cada Bolsa {name} tiene un Peso Bruto
                // Promedio de {peso} Kg." con el peso bruto promedio del tipo
                // de envase. Si no se configuro, queda vacio.
                String observacion = "";
                if (det.getPackaging() != null
                        && det.getPackaging().getAverageGrossWeightKg() != null
                        && !packagingName.isEmpty()) {
                    observacion = "Cada Bolsa " + packagingName
                            + " tiene un Peso Bruto Promedio de "
                            + df.format(det.getPackaging().getAverageGrossWeightKg())
                            + " Kg.";
                }
                row.put("COLUMN_4", observacion);

                rows.add(row);
            }
        }
        JRDataSource ds = new JRBeanCollectionDataSource(rows);

        mainReportParams.put("WEIGHT_SUBREPORT", subReportData.getJasperReport());
        mainReportParams.put("WEIGHT_SUBREPORT_DATASOURCE", ds);
    }

    private String buildTotalDelivered(WarehouseVoucherDispatchDetail det) {
        if (det.getPackaging() == null || det.getBagsCount() == null) {
            return "";
        }
        int n = det.getBagsCount();
        String unit = (n == 1) ? " bolsa " : " bolsas ";
        return n + unit + paramAsString(det.getPackaging().getName())
                + " de " + paramAsString(det.getPackaging().getCapacityLabel());
    }

    /**
     * Texto de la columna DESCRIPCION DETALLADA: amplia TOTAL ENTREGADO con
     * " de capacidad de {producto}". Vacio si falta envase/bolsas/producto.
     */
    private String buildDescripcionDetallada(WarehouseVoucherDispatchDetail det) {
        String total = buildTotalDelivered(det);
        if (total.isEmpty() || det.getProductItem() == null) {
            return "";
        }
        return total + " de capacidad de " + paramAsString(det.getProductItem().getName());
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
