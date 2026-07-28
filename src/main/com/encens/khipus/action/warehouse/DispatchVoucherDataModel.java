package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DataModel paginado para listar Vales de Despacho.
 * Filtros: numero de orden de entrega, lote-venta, cliente, transportadora,
 * placa, estado, rango de fecha de despacho.
 */
@Name("dispatchVoucherDataModel")
@Scope(ScopeType.PAGE)
public class DispatchVoucherDataModel extends QueryDataModel<Long, WarehouseVoucherDispatch> {

    private Date startDate;
    private Date endDate;
    private String plateFilter;

    private static final String[] RESTRICTIONS = {
            "dispatch.deliveryOrderNumber = #{dispatchVoucherDataModel.criteria.deliveryOrderNumber}",
            "lower(dispatch.salesLotCode) like concat('%', concat(lower(#{dispatchVoucherDataModel.criteria.salesLotCode}), '%'))",
            "lower(dispatch.vehicle.plate) like concat('%', concat(lower(#{dispatchVoucherDataModel.plateFilter}), '%'))",
            "dispatch.state = #{dispatchVoucherDataModel.criteria.state}",
            "dispatch.client = #{dispatchVoucherDataModel.criteria.client}",
            "dispatch.transportCompany = #{dispatchVoucherDataModel.criteria.transportCompany}",
            "dispatch.dispatchDate >= #{dispatchVoucherDataModel.startDateFilter}",
            "dispatch.dispatchDate <= #{dispatchVoucherDataModel.endDateFilter}"
    };

    @Create
    public void init() {
        sortProperty = "dispatch.dispatchDate";
        sortAsc = false;
        // Rango por defecto: 01 de enero del anio actual hasta hoy. Al crearse el
        // componente la lista ya arranca filtrada por ese rango.
        startDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(new Date()));
        endDate = DateUtils.toDay();
    }

    /**
     * QueryDataModel.initEntityQuery() recrea el criteria via newInstance()
     * la primera vez que se ejecuta la query, sobreescribiendo cualquier
     * cambio hecho en init(). Sobrescribimos createInstance() para limpiar
     * el state heredado del field initializer (DispatchState.BORRADOR),
     * y asi el filtro arranca mostrando todos los estados.
     */
    @Override
    public WarehouseVoucherDispatch createInstance() {
        WarehouseVoucherDispatch instance = super.createInstance();
        if (instance != null) {
            instance.setState(null);
        }
        return instance;
    }

    @Override
    public String getEjbql() {
        return "select dispatch from WarehouseVoucherDispatch dispatch";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Valor normalizado de startDate para el filtro: 00:00:00 del dia elegido.
     * Lo usan tanto las RESTRICTIONS de esta lista como el reporte Excel.
     */
    public Date getStartDateFilter() {
        return DateUtils.removeTime(startDate);
    }

    /**
     * Valor normalizado de endDate para el filtro: 23:59:59.999 del dia elegido.
     * dispatchDate es TIMESTAMP; sin esto los despachos del propio dia "hasta"
     * (con hora distinta de 00:00) quedarian fuera del rango.
     */
    public Date getEndDateFilter() {
        if (endDate == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        DateUtils.toMaxHours(calendar);
        return calendar.getTime();
    }

    public String getPlateFilter() {
        return plateFilter;
    }

    public void setPlateFilter(String plateFilter) {
        this.plateFilter = plateFilter;
    }
}
