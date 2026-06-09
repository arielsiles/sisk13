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
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatchEnvelope;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el reporte "Detalle de Envases Carguio" en PDF a partir de un
 * {@link WarehouseVoucherDispatch} en estado APROBADO o FINALIZADO.
 * <p>
 * Reutiliza el patron del Certificado: el reporte principal lleva el
 * encabezado (logo + datos del despacho/conductor/vehiculo) y delega la
 * tabla de envases a un subreport alimentado por un
 * JRBeanCollectionDataSource sobre los WarehouseVoucherDispatchEnvelope del
 * despacho.
 */
@Name("dispatchEnvelopeReportAction")
@Scope(ScopeType.PAGE)
public class DispatchEnvelopeReportAction extends GenericReportAction {

    @In
    private CompanyConfigurationService companyConfigurationService;

    @Restrict("#{s:hasPermission('WAREHOUSEDISPATCH','VIEW')}")
    public void generateEnvelopeReport(WarehouseVoucherDispatch dispatch) {
        log.debug("Generating envelope detail report for dispatch id=" + dispatch.getId());

        // setReportFormat DEBE invocarse antes de addEnvelopeSubReport (lee
        // getReportFormat().getFormat() y de lo contrario NPE).
        setReportFormat(ReportFormat.PDF);

        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(buildDispatchParams(dispatch));
        params.putAll(buildCompanyParams());
        addEnvelopeSubReport(params, dispatch);

        String fileName = "DetalleEnvases_" +
                (dispatch.getDeliveryOrderNumber() != null
                        ? String.format("%06d", dispatch.getDeliveryOrderNumber())
                        : dispatch.getId());

        super.generateReport("dispatchEnvelopeReport",
                "/warehouse/reports/dispatchEnvelopeReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                fileName,
                params);
    }

    private Map<String, Object> buildDispatchParams(WarehouseVoucherDispatch d) {
        Map<String, Object> p = new HashMap<String, Object>();

        p.put("deliveryOrderNumber", d.getDeliveryOrderNumber());
        p.put("salesLotCode", paramAsString(d.getSalesLotCode()));
        p.put("dispatchDate", d.getDispatchDate());
        p.put("truckDispatchNumber", d.getTruckDispatchNumber());

        p.put("driverName",   d.getDriver()  != null ? paramAsString(d.getDriver().getName())    : "");
        p.put("driverLicense",d.getDriver()  != null ? paramAsString(d.getDriver().getLicense()) : "");
        p.put("driverPhone",  d.getDriver()  != null ? paramAsString(d.getDriver().getPhone())   : "");
        p.put("vehiclePlate", d.getVehicle() != null ? paramAsString(d.getVehicle().getPlate())  : "");
        p.put("vehicleBrand", d.getVehicle() != null ? paramAsString(d.getVehicle().getBrand())  : "");
        p.put("vehicleColor", d.getVehicle() != null ? paramAsString(d.getVehicle().getColor())  : "");

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
     * Construye y registra el subreport con la tabla de envases. Aplana todos
     * los envases del despacho (todos los detalles) en una lista linear que
     * alimenta el detail band del subreport. El orden global es: por detalle
     * (orden de captura) y dentro de cada detalle por correlativo ascendente
     * (garantizado por @OrderBy en la entidad).
     */
    private void addEnvelopeSubReport(Map<String, Object> mainReportParams,
                                      WarehouseVoucherDispatch dispatch) {
        TypedReportData subReportData = super.generateSubReport(
                "ENVELOPESUBREPORT",
                "/warehouse/reports/dispatchEnvelopeTableSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                new HashMap<String, Object>());

        List<WarehouseVoucherDispatchEnvelope> all = new ArrayList<WarehouseVoucherDispatchEnvelope>();
        if (dispatch.getDetails() != null) {
            for (WarehouseVoucherDispatchDetail det : dispatch.getDetails()) {
                if (det.getEnvelopes() != null) {
                    all.addAll(det.getEnvelopes());
                }
            }
        }
        JRDataSource ds = new JRBeanCollectionDataSource(all);

        mainReportParams.put("ENVELOPESUBREPORT", subReportData.getJasperReport());
        mainReportParams.put("ENVELOPESUBREPORT_DATASOURCE", ds);
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
