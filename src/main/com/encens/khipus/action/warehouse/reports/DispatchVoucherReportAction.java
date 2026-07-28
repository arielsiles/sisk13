package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;

/**
 * Exporta a Excel el listado de Vales de Despacho aplicando exactamente los
 * mismos filtros del panel de busqueda: las restricciones apuntan a
 * {@code dispatchVoucherDataModel}, que es el componente PAGE al que estan
 * ligados los campos de la barra de filtros. Asi el Excel siempre refleja lo
 * que el usuario tiene cargado en pantalla al presionar Exportar.
 * <p>
 * El numero de bolsas no es una columna persistente (WarehouseVoucherDispatch
 * lo deriva de la suma del detalle), por eso la consulta hace LEFT JOIN al
 * detalle, suma bagsCount y agrupa por la cabecera.
 * <p>
 * Todos los joins son LEFT: transportadora, conductor y vehiculo son opcionales
 * y con join implicito (INNER) se perderian los despachos que no los tengan.
 */
@Name("dispatchVoucherReportAction")
@Scope(ScopeType.PAGE)
public class DispatchVoucherReportAction extends GenericReportAction {

    @Create
    public void init() {
        restrictions = new String[]{
                "dispatch.deliveryOrderNumber = #{dispatchVoucherDataModel.criteria.deliveryOrderNumber}",
                "lower(dispatch.salesLotCode) like concat('%', concat(lower(#{dispatchVoucherDataModel.criteria.salesLotCode}), '%'))",
                "lower(vehicle.plate) like concat('%', concat(lower(#{dispatchVoucherDataModel.plateFilter}), '%'))",
                "dispatch.state = #{dispatchVoucherDataModel.criteria.state}",
                "dispatch.client = #{dispatchVoucherDataModel.criteria.client}",
                "dispatch.transportCompany = #{dispatchVoucherDataModel.criteria.transportCompany}",
                "dispatch.dispatchDate >= #{dispatchVoucherDataModel.startDateFilter}",
                "dispatch.dispatchDate <= #{dispatchVoucherDataModel.endDateFilter}"
        };

        sortProperty = "dispatch.dispatchDate, dispatch.deliveryOrderNumber";

        // Al agregar sum(detail.bagsCount) hay que agrupar por todas las columnas
        // no agregadas. Se incluye dispatch.id para que dos despachos con los
        // mismos datos de cabecera no colapsen en una sola fila.
        groupByProperty = "dispatch.id, " +
                "dispatch.dispatchDate, " +
                "dispatch.deliveryOrderNumber, " +
                "dispatch.salesLotCode, " +
                "dispatch.invoiceNumber, " +
                "entity.acronym, " +
                "dispatch.truckDispatchNumber, " +
                "driver.name, " +
                "driver.license, " +
                "driver.phone, " +
                "vehicle.plate, " +
                "vehicle.color, " +
                "dispatch.weighingTicketNumber, " +
                "dispatch.state";
    }

    @Override
    protected String getEjbql() {
        return " SELECT dispatch.dispatchDate as dispatchDate, " +
               "        dispatch.salesLotCode as salesLotCode, " +
               "        dispatch.invoiceNumber as invoiceNumber, " +
               "        entity.acronym as transportCompany, " +
               "        dispatch.truckDispatchNumber as truckNumber, " +
               "        driver.name as driverName, " +
               "        driver.license as driverLicense, " +
               "        driver.phone as driverPhone, " +
               "        sum(detail.bagsCount) as bagCount, " +
               "        vehicle.plate as plate, " +
               "        vehicle.color as color, " +
               "        dispatch.weighingTicketNumber as weighingTicket, " +
               "        dispatch.state as state " +
               "   FROM WarehouseVoucherDispatch dispatch " +
               "        LEFT JOIN dispatch.transportCompany provider " +
               "        LEFT JOIN provider.entity entity " +
               "        LEFT JOIN dispatch.driver driver " +
               "        LEFT JOIN dispatch.vehicle vehicle " +
               "        LEFT JOIN dispatch.details detail ";
    }

    @Restrict("#{s:hasPermission('WAREHOUSEDISPATCH','VIEW')}")
    public void generateReport() {
        log.debug("Generating dispatch voucher list report...................");

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        setReportFormat(ReportFormat.XLSX);
        super.generateReport(
                "dispatchVoucherReport",
                "/warehouse/reports/dispatchVoucherReport.jrxml",
                PageFormat.CUSTOM,
                PageOrientation.LANDSCAPE,
                messages.get("WarehouseDispatch.report.fileName"),
                reportParameters);
    }
}
