package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.WarehouseVoucherDispatch;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
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

    private static final String[] RESTRICTIONS = {
            "dispatch.deliveryOrderNumber = #{dispatchVoucherDataModel.criteria.deliveryOrderNumber}",
            "lower(dispatch.salesLotCode) like concat('%', concat(lower(#{dispatchVoucherDataModel.criteria.salesLotCode}), '%'))",
            "lower(dispatch.vehiclePlate) like concat('%', concat(lower(#{dispatchVoucherDataModel.criteria.vehiclePlate}), '%'))",
            "dispatch.state = #{dispatchVoucherDataModel.criteria.state}",
            "dispatch.client = #{dispatchVoucherDataModel.criteria.client}",
            "dispatch.transportCompany = #{dispatchVoucherDataModel.criteria.transportCompany}",
            "dispatch.dispatchDate >= #{dispatchVoucherDataModel.startDate}",
            "dispatch.dispatchDate <= #{dispatchVoucherDataModel.endDate}"
    };

    @Create
    public void init() {
        sortProperty = "dispatch.dispatchDate";
        sortAsc = false;
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
}
