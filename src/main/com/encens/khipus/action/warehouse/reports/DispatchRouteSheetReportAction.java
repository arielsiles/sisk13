package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.warehouse.DispatchRoute;
import com.encens.khipus.model.warehouse.DispatchState;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchDetail;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el reporte "Hoja de Ruta" en PDF a partir de un
 * {@link WarehouseVoucherDispatch} en estado APROBADO o FINALIZADO.
 *
 * Datos del despacho directamente: lote, factura, fecha, transportadora,
 * conductor (nombre+licencia), vehiculo (color/marca/placa), lugares
 * origen/destino, observacion y firmas.
 *
 * Datos provenientes del catalogo {@link DispatchRoute}: paradas (waypoints)
 * e imagen del mapa. La imagen se pasa como InputStream al JR.
 *
 * Datos por linea de detalle (concatenados): descripcion tecnica del producto
 * (campo productDescription.description con fallback a productItem.name) y
 * cantidad. Si hay multiples lineas con la misma unidad de medida, se suma.
 */
@Name("dispatchRouteSheetReportAction")
@Scope(ScopeType.PAGE)
public class DispatchRouteSheetReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;

    @Restrict("#{s:hasPermission('WAREHOUSEDISPATCHROUTESHEET','VIEW')}")
    public void generateRouteSheet(WarehouseVoucherDispatch dispatch) {
        log.debug("Generating Hoja de Ruta for id=" + dispatch.getId());

        setReportFormat(ReportFormat.PDF);

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(buildDispatchParams(dispatch));
        params.putAll(buildCompanyParams());
        params.put("warehouseSignatureBlock",
                buildWarehouseSignature(dispatch, (String) params.get("companyName")));
        params.put("driverSignatureBlock", buildDriverSignature(dispatch));
        addCargoSubReport(params, dispatch);

        String fileName = "HojaRuta_" +
                (dispatch.getDeliveryOrderNumber() != null
                        ? String.format("%06d", dispatch.getDeliveryOrderNumber())
                        : dispatch.getId());

        super.generateReport("dispatchRouteSheet",
                "/warehouse/reports/dispatchRouteSheetReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                fileName,
                params);
    }

    private Map<String, Object> buildDispatchParams(WarehouseVoucherDispatch d) {
        Map<String, Object> p = new HashMap<String, Object>();

        p.put("salesLotCode", paramAsString(d.getSalesLotCode()));
        p.put("invoiceNumber", paramAsString(d.getInvoiceNumber()));
        p.put("dispatchDate", d.getDispatchDate());
        p.put("validityDays", d.getValidityDays() != null ? d.getValidityDays() : Integer.valueOf(0));
        p.put("maximumValidityDays",
                d.getMaximumValidityDays() != null ? d.getMaximumValidityDays() : Integer.valueOf(0));

        // FinancesEntity.getFullName concatena NIT + acronym (sale "0 ASOCIACION..."),
        // por eso usamos getEntity().getAcronym() - mismo patron del Certificado.
        p.put("transportCompany",
                (d.getTransportCompany() != null && d.getTransportCompany().getEntity() != null)
                        ? paramAsString(d.getTransportCompany().getEntity().getAcronym()) : "");

        p.put("driverName",    d.getDriver()  != null ? paramAsString(d.getDriver().getName())    : "");
        p.put("driverLicense", d.getDriver()  != null ? paramAsString(d.getDriver().getLicense()) : "");
        p.put("driverPhone",   d.getDriver()  != null ? paramAsString(d.getDriver().getPhone())   : "");

        p.put("vehicleColor", d.getVehicle() != null ? paramAsString(d.getVehicle().getColor()) : "");
        p.put("vehicleBrand", d.getVehicle() != null ? paramAsString(d.getVehicle().getBrand()) : "");
        p.put("vehiclePlate", d.getVehicle() != null ? paramAsString(d.getVehicle().getPlate()) : "");

        // Sin el prefijo de codigo ("01 - "): solo la descripcion del lugar.
        p.put("originPlace",      d.getOriginPlace()      != null ? paramAsString(d.getOriginPlace().getDescription())      : "");
        p.put("destinationPlace", d.getDestinationPlace() != null ? paramAsString(d.getDestinationPlace().getDescription()) : "");

        DispatchRoute r = d.getRoute();
        p.put("routeWaypoints", r != null ? paramAsString(r.getWaypoints()) : "");
        p.put("routeMapImage",
                (r != null && r.getMapImage() != null && r.getMapImage().length > 0)
                        ? (InputStream) new ByteArrayInputStream(r.getMapImage())
                        : null);

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
     * Compila el subreporte de "DATOS DE LA CARGA" y lo registra con su
     * datasource. Una fila por linea de detalle del despacho:
     *   description = productDescription.description (fallback productItem.name)
     *   quantity    = detail.quantity
     *   unit        = detail.measureUnit.name
     * Si no hay detalles, se pasa una lista vacia y el subreporte no muestra
     * filas (gracias a whenNoDataType="AllSectionsNoDetail").
     */
    private void addCargoSubReport(Map<String, Object> mainReportParams,
                                   WarehouseVoucherDispatch dispatch) {
        TypedReportData subReportData = super.generateSubReport(
                "CARGOSUBREPORT",
                "/warehouse/reports/dispatchRouteSheetCargoSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                new HashMap<String, Object>());

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        List<WarehouseVoucherDispatchDetail> details = dispatch.getDetails();
        if (details != null) {
            for (WarehouseVoucherDispatchDetail det : details) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("description", buildLineDescription(det));
                BigDecimal qty = det.getQuantity() != null ? det.getQuantity() : BigDecimal.ZERO;
                String unitName = det.getMeasureUnit() != null ? det.getMeasureUnit().getName() : "";
                Object[] converted = convertKilogramToTon(qty, unitName);
                row.put("quantity", converted[0]);
                row.put("unit", converted[1]);
                rows.add(row);
            }
        }
        JRDataSource ds = new JRMapCollectionDataSource(rows);

        mainReportParams.put("CARGOSUBREPORT", subReportData.getJasperReport());
        mainReportParams.put("CARGOSUBREPORT_DATASOURCE", ds);
    }

    /**
     * Convierte cantidad+unidad para la celda de la Hoja de Ruta:
     *   - Si la unidad es KG/KILOGRAMO/KILOGRAMOS -> divide entre 1000 y
     *     pone "Toneladas" (KILOGRAMOS no entra en la celda de UNIDAD).
     *   - Si no, formatea la unidad con title-case ("Bolsas", "Litros").
     * Devuelve un array [BigDecimal cantidad, String unidad].
     */
    private Object[] convertKilogramToTon(BigDecimal quantity, String unitName) {
        String upper = unitName != null ? unitName.trim().toUpperCase() : "";
        boolean isKilogram = upper.equals("KG") || upper.equals("KGS")
                || upper.startsWith("KILOGRAM");
        if (isKilogram) {
            BigDecimal qtyTons = quantity.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
            return new Object[]{qtyTons, "Toneladas"};
        }
        return new Object[]{quantity, titleCase(unitName)};
    }

    /**
     * Title case simple: primera letra mayuscula, resto minuscula.
     * Ej: "KILOGRAMOS" -> "Kilogramos", "Bolsas" -> "Bolsas".
     */
    private String titleCase(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String t = s.trim().toLowerCase();
        if (t.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    /**
     * Texto de DESCRIPCION para una linea: usa productDescription.description
     * (catalogo de descripciones tecnicas) o fallback a productItem.name.
     */
    private String buildLineDescription(WarehouseVoucherDispatchDetail det) {
        if (det.getProductDescription() != null
                && det.getProductDescription().getDescription() != null) {
            return det.getProductDescription().getDescription().trim();
        }
        if (det.getProductItem() != null) {
            return paramAsString(det.getProductItem().getName());
        }
        return "";
    }

    /**
     * Bloque firma "RESP. DE ALMACEN Y COMPRAS" reutilizando el patron de
     * Nota de Remision: single styled textField con &lt;b&gt;.
     */
    private String buildWarehouseSignature(WarehouseVoucherDispatch d, String companyName) {
        String name = "", phone = "", ci = "";
        if (d.getResponsible() != null) {
            name = paramAsString(d.getResponsible().getFullName());
            phone = paramAsString(d.getResponsible().getCellphone());
            ci = paramAsString(d.getResponsible().getIdNumber());
        }
        StringBuilder s = new StringBuilder();
        if (name.length() > 0) {
            s.append(name);
        }
        s.append("\n<b>RESP. DE ALMACEN Y COMPRAS</b>");
        StringBuilder idLine = new StringBuilder();
        if (ci.length() > 0) {
            idLine.append("C.I.: ").append(ci);
        }
        if (phone.length() > 0) {
            if (idLine.length() > 0) idLine.append(" / ");
            idLine.append("CEL.: ").append(phone);
        }
        if (idLine.length() > 0) {
            s.append("\n").append(idLine);
        }
        if (companyName != null && companyName.length() > 0) {
            s.append("\n<b>").append(companyName).append("</b>");
        }
        return s.toString();
    }

    /**
     * Bloque firma "CONDUCTOR / TRANSPORTISTA".
     */
    private String buildDriverSignature(WarehouseVoucherDispatch d) {
        String name = "", license = "", phone = "";
        if (d.getDriver() != null) {
            name = paramAsString(d.getDriver().getName());
            license = paramAsString(d.getDriver().getLicense());
            phone = paramAsString(d.getDriver().getPhone());
        }
        StringBuilder s = new StringBuilder();
        if (name.length() > 0) {
            s.append(name);
        }
        if (license.length() > 0) {
            if (s.length() > 0) s.append("\n");
            s.append(license);
        }
        s.append("\n<b>CONDUCTOR / TRANSPORTISTA</b>");
        if (phone.length() > 0) {
            s.append("\nCEL.: ").append(phone);
        }
        return s.toString();
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
